package com.example.whatsapp;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface CurrencyService {

    @GET("/getPercentage")
    Call<Love> getPercent(@Header("X-Mashape-Key") String key, @Header("Accept") String accept, @Query("fname")
            String fname, @Query("sname") String sname);
}
