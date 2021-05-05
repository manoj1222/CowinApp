package com.mami.cowin.cowinapp;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class AlarmReciever extends BroadcastReceiver {
        private static final String TAG = "AlarmReciever";

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Alarm Triggered: " + LocalDateTime.now().toString());
            Runnable runnable = new WorkerThread(context, null);
            Thread thread = new Thread(runnable);
            thread.start();
            Log.d(TAG, "Alarm Triggered Completed: " + LocalDateTime.now().toString());
        }


}
