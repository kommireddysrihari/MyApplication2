package com.example.myapplication.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface FrankfurterApi {
    @GET("latest")
    suspend fun getLatestRates(
        @Query("from") fromCurrency: String,
        @Query("to") toCurrency: String = "INR"
    ): Response<CurrencyResponse>
}
