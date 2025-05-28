package com.example.guitarapp.data.remote

import com.example.guitarapp.data.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface AuthApi {
    @FormUrlEncoded
    @POST("/process_login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<Unit>

    @POST("/auth/registration")
    suspend fun register(
        @Body registerRequest: RegisterRequest
    ): Response<RegisterRequest>
}