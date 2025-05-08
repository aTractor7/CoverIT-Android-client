package com.example.guitarapp.data.remote

import com.example.guitarapp.data.model.Comment
import com.example.guitarapp.data.model.CommentCreate
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface CommentApi {

    @GET("/comments")
    suspend fun getComments(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("sortField") sortField: String? = null,
        @Query("songTitle") songTitle: String? = null
    ): Response<List<Comment>>

    @POST("/comments")
    suspend fun createComment(
        @Body comment: CommentCreate
    ): Response<CommentCreate>
}