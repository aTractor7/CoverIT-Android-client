package com.example.guitarapp.data.remote

import com.example.guitarapp.data.model.Chord
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ChordApi {

    @GET("chords")
    suspend fun getChords(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("sortField") sortField: String? = null,
        @Query("name") name: String? = null
    ): Response<List<Chord>>
}