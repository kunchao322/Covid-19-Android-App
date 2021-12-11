package com.example.practicecovidapp;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiInterface {
//    String BASE_URL = "https://corona.lmao.ninja/v2/";
    String BASE_URL = "https://disease.sh/v3/covid-19/";
    //disease.sh/v3/covid-19/
//    @Headers("Content-Type: application/json")
    @GET("countries")
    //Get array of all countries
    Call<List<ModelClass>> getCountryData();
}
