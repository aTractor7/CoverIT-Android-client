package com.example.guitarapp.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SongTutorial(
    val id: Int,
    val difficulty: String,
    val description: String?,
    val backtrack: String?,
    val createdAt: String,
    val recommendedStrumming: String?,
    val tutorialAuthor: UserDto,
    val song: SongShort,
    val beats: List<SongBeat>,
    val comments: List<Comment>
) : Parcelable