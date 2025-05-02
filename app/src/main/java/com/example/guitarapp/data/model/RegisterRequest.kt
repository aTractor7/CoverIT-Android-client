package com.example.guitarapp.data.model

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val skill: String,
    val instrument: String,
    val bio: String
)