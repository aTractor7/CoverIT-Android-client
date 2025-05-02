package com.example.guitarapp.data.remote

import com.example.guitarapp.data.model.UserDto
import retrofit2.Response

import retrofit2.http.GET
import retrofit2.http.Path

interface UserApi {
    @GET("users/{id}")
    suspend fun getUserProfile(
        @Path("id") userId: Int,
    ): Response<UserDto>


    @GET("users/authenticated")
    suspend fun getAuthenticatedUser(
    ): Response<UserDto>
}