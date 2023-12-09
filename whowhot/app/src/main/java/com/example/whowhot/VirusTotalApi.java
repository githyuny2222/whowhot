package com.example.whowhot;

import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface VirusTotalApi {
    @GET("url/report")
    Call<JsonObject> scanUrl(@Query("apikey") String apiKey, @Query("resource") String url);
}