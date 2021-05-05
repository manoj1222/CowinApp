package com.mami.cowin.cowinapp;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface JsonPlaceHolderApi {

    @GET("v2/appointment/sessions/public/calendarByDistrict")
    Call<Centers> getAppointments(@Query("district_id") String districtId, @Query("date") String date);
}