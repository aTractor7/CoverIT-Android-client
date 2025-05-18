package com.example.guitarapp.data.remote

import com.example.guitarapp.data.model.PersonalLibrary
import com.example.guitarapp.data.model.PersonalLibraryCreate
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface PersonalLibraryApi {

    @GET("personal-library")
    suspend fun getPersonalLibraries(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("sortField") sortField: String? = null,
        @Query("userId") userId: Int? = null
    ): Response<List<PersonalLibrary>>

    @POST("personal-library")
    suspend fun createPersonalLibraries(
        @Body personalLibrary: PersonalLibraryCreate
    ): Response<PersonalLibraryCreate>

    @DELETE("personal-library")
    suspend fun deletePersonalLibrary(
        @Query("tutorialId") tutorialId: Int
    ): Response<Map<String, String>>
}