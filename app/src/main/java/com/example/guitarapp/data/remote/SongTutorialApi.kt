package com.example.guitarapp.data.remote

import com.example.guitarapp.data.model.SongTutorial
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface SongTutorialApi {
    @GET("tutorials/{id}")
    suspend fun getSongTutorial(
        @Path("id") tutorialId: Int,
    ): Response<SongTutorial>
}