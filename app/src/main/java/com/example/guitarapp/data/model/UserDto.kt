package com.example.guitarapp.data.model

data class UserDto(
    val id: Int,
    val username: String,
    val email: String,
    val profileImg: ByteArray?,
    val joinDate: String,
    val role: String,
    val skill: String,
    val instrument: String?,
    val bio: String?
)
