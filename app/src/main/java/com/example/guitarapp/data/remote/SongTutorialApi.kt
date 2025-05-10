package com.example.guitarapp.data.remote

import com.example.guitarapp.data.model.SongTutorial
import com.example.guitarapp.data.model.SongTutorialCreate
import com.example.guitarapp.data.model.SongTutorialShort
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SongTutorialApi {
    @GET("tutorials/{id}")
    suspend fun getSongTutorial(
        @Path("id") tutorialId: Int,
    ): Response<SongTutorial>

    @GET("tutorials")
    suspend fun getSongTutorials(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("sortField") sortField: String? = null,
        @Query("songTitle") songTitle: String? = null
    ): Response<List<SongTutorialShort>>

    @POST("tutorials")
    suspend fun createSongTutorial(
        @Body songTutorialCreate: SongTutorialCreate
    ): Response<SongTutorialCreate>
}