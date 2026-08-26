package com.sahil.tracker.service

import com.sahil.tracker.data.models.TypingEvent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface TrackerApi {
    @POST("/api/events")
    suspend fun syncEvents(@Body events: List<TypingEvent>)
}

object ApiClient {
    fun create(baseUrl: String): TrackerApi {
        val url = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TrackerApi::class.java)
    }
}
