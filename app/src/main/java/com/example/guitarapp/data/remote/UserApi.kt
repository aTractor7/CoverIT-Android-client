package com.example.guitarapp.data.remote

import com.example.guitarapp.data.model.UserDto
import retrofit2.Response
import retrofit2.http.Body

import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface UserApi {
    @GET("users/{id}")
    suspend fun getUserProfile(
        @Path("id") userId: Int,
    ): Response<UserDto>


    @GET("users/authenticated")
    suspend fun getAuthenticatedUser(
    ): Response<UserDto>

    @PUT("users/{id}")
    suspend fun updateUserProfile(
        @Path("id") userId: Int,
        @Body updatedUser: UserDto
    ): Response<UserDto>
}