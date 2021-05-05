package com.mami.cowin.cowinapp;

import android.app.Notification;
import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class WorkerThread implements Runnable {

    private static final String COWIN_API_URL = "https://cdn-api.co-vin.in/api/";
    private static final String HEADER = "name, pincode, Fee, Date, available Capacity, vaccine name";
    private static final String PLUS_18 = "###########################18 PLUS##########################";
    private static final String PLUS_45 = "###########################45 PLUS##########################";
    private static final String CHANNEL_ID = "vaccine_alert";
    private static final String CHANNEL_NAME = "Vaccine Availability Alert";
    private static final String CHANNEL_DESC = "CoWin Vaccine Availability Alert";
    private static final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(COWIN_API_URL)
            .addConverterFactory(JacksonConverterFactory.create())
            .build();
    private static final String TAG = "WorkerThread";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    final private Context context;
    private String dateStr;

    public WorkerThread(Context context, String dateStr) {
        this.context = context;
        this.dateStr = dateStr;
    }

    @Override
    public void run() {
        callApi();
    }

    private void callApi() {
        if(Objects.isNull(dateStr)) {
            LocalDate localDate = LocalDate.now();
            dateStr = localDate.format(formatter);
        }
        final JsonPlaceHolderApi jsonPlaceHolderApi = retrofit.create(JsonPlaceHolderApi.class);
        final Call<Centers> call = jsonPlaceHolderApi.getAppointments("395", dateStr);

        call.enqueue(new Callback<Centers>() {
            @Override
            public void onResponse(Call<Centers> call, Response<Centers> response) {
                if (!response.isSuccessful()) {
                    ObservableObject.getInstance().updateValue("Code: " + response.code());
                    Log.e(TAG, "response unsuccessfully executed with response code: " + response.code());
                    return;
                }
                final Centers centers = response.body();
                final StringBuilder sb = new StringBuilder("");
                sb.append("Last Updated Time: ").append(LocalDateTime.now().toString());
                List<String> centers18Plus = findVaccineAvailabilityData(centers, 18);
                sb.append(PLUS_18).append("\n");
                sb.append(centers18Plus.toString()).append("\n");
                List<String> centers45Plus = findVaccineAvailabilityData(centers, 45);
                sb.append(PLUS_45).append("\n");
                sb.append(centers45Plus.toString()).append("\n");
                final String content = sb.toString();
                ObservableObject.getInstance().updateValue(content);
                displayNotification(centers18Plus.size(), centers45Plus.size());
                Log.d(TAG, "printing content: \n" + content);
            }
            @Override
            public void onFailure(Call<Centers> call, Throwable t) {
                ObservableObject.getInstance().updateValue(t.getMessage());
            }
        });
        Log.d(TAG, "HTTP call complete");
    }

    private List<String> findVaccineAvailabilityData(final Centers centers, final int age) {
        final List<String> result = new LinkedList<>();
        result.add(HEADER + "\n");
        final List<Center> listOfCenters = centers.getCenters();
        for(Center center: listOfCenters) {
            for(Session session: center.getSessions()) {
                if(session.getMinAgeLimit() == age && session.getAvailableCapacity() > 0) {
                    final String str = String.join("--", center.getName(), center.getPincode().toString(), center.getFeeType(),
                            session.getDate(), session.getAvailableCapacity().toString(), session.getVaccine());
                    result.add(str + "\n");
                }
            }
        }
        return result;
    }

    private void displayNotification(int size18, int size45) {
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALL);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Vaccine Available")
                .setContentText("18 = " + (size18 - 1) + ", \n" + "45 = " + (size45 - 1))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setSound(alarmSound);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(1, builder.build());
        Log.d(TAG, "Notification Displayed");
    }

}
