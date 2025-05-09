package com.example.guitarapp.data.remote

import com.example.guitarapp.data.model.Song
import com.example.guitarapp.data.model.SongShort
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SongApi {

    @POST("songs")
    suspend fun createSongs(
        @Body song: Song
    ): Response<Song>

    @GET("songs")
    suspend fun getSongs(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("sortField") sortField: String? = null,
        @Query("songTitle") songTitle: String? = null
    ): Response<List<SongShort>>
}