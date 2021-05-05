package com.mami.cowin.cowinapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.time.LocalDateTime;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class MainActivity extends AppCompatActivity implements Observer {
    private TextView textViewResult;
    private EditText editText;
    private static final String CHANNEL_ID = "vaccine_alert";
    private static final String CHANNEL_NAME = "Vaccine Availability Alert";
    private static final String CHANNEL_DESC = "CoWin Vaccine Availability Alert";
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewResult = findViewById(R.id.text_view_result);

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription(CHANNEL_DESC);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);

        ObservableObject.getInstance().addObserver(this);
        scheduleAlarm();
    }

    private void scheduleAlarm() {
        Log.d(TAG, "Alarm Scheduling called");
        // time at which alarm will be scheduled here alarm is scheduled at 1 day from current time,
        // we fetch  the current time in milliseconds and added 1 day time
        // i.e. 24*60*60*1000= 86,400,000   milliseconds in a day
        long time = new GregorianCalendar().getTimeInMillis();

        // create an Intent and set the class which will execute when Alarm triggers, here we have
        // given AlarmReciever in the Intent, the onRecieve() method of this class will execute when
        // alarm triggers and
        //we call the method inside onRecieve() method of Alarmreciever class
        Intent intentAlarm = new Intent(this, AlarmReciever.class);

        // create the object
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        //set the alarm for particular time
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,time,AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                PendingIntent.getBroadcast(this,1,
                        intentAlarm, PendingIntent.FLAG_CANCEL_CURRENT));
        Log.d(TAG, "Alarm Scheduled for every 15 minutes");
    }

    @Override
    public void update(Observable o, Object arg) {
        textViewResult.setText(arg.toString());
    }

    public void OnCustomDateSubmit(View v) {
        editText = (EditText) findViewById(R.id.customDate);
        String dateStr = editText.getText().toString();
        final WorkerThread runnable = new WorkerThread(this, dateStr);
        runnable.run();
    }
}
