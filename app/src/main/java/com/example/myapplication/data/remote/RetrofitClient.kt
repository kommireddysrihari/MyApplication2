package com.example.myapplication.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://api.frankfurter.app/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    val api: FrankfurterApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FrankfurterApi::class.java)
    }

    // Default fallback exchange rates (To INR) if network fails
    private val DEFAULT_INR_RATES = mapOf(
        "INR" to 1.0,
        "USD" to 86.85,
        "EUR" to 90.50,
        "GBP" to 109.20,
        "JPY" to 0.57,
        "AUD" to 55.40,
        "CAD" to 61.20
    )

    suspend fun convertToInr(amount: Double, fromCurrency: String): Double {
        if (fromCurrency.equals("INR", ignoreCase = true) || amount <= 0) {
            return amount
        }
        return try {
            val response = api.getLatestRates(fromCurrency = fromCurrency, toCurrency = "INR")
            if (response.isSuccessful) {
                val rateMap = response.body()?.rates
                val inrRate = rateMap?.get("INR") ?: DEFAULT_INR_RATES[fromCurrency.uppercase()] ?: 1.0
                amount * inrRate
            } else {
                val fallbackRate = DEFAULT_INR_RATES[fromCurrency.uppercase()] ?: 1.0
                amount * fallbackRate
            }
        } catch (e: Exception) {
            val fallbackRate = DEFAULT_INR_RATES[fromCurrency.uppercase()] ?: 1.0
            amount * fallbackRate
        }
    }
}
