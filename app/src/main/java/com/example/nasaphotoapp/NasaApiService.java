package com.example.nasaphotoapp;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NasaApiService {
    @GET("planetary/apod")
    Call<NasaPhoto> getPhotoOfTheDay(
            @Query("api_key") String apiKey,
            @Query("date") String date
    );
}
