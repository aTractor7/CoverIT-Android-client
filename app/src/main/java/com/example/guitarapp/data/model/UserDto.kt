package com.example.guitarapp.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
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
) : Parcelable{
    companion object {
        fun empty() = UserDto(
            id = 0,
            username = "",
            email = "",
            profileImg = null,
            joinDate = "",
            role = "",
            skill = "",
            instrument = null,
            bio = null
        )
    }
}
