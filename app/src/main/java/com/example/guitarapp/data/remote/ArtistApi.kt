package com.example.guitarapp.data.remote

import com.example.guitarapp.data.model.Artist
import com.example.guitarapp.data.model.ArtistShort
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ArtistApi {

    @POST("artists")
    suspend fun createArtist(
        @Body artist: Artist
    ): Response<Artist>

    @GET("artists")
    suspend fun getArtist(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("sortField") sortField: String? = null,
        @Query("name") name: String? = null
    ): Response<List<ArtistShort>>
}