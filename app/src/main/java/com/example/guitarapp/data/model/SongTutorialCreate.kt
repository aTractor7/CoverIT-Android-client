package com.example.guitarapp.data.model

data class SongTutorialCreate(
    val difficulty: String,
    val description: String?,
    val backtrack: String?,
    val recommendedStrumming: String?,
    val song: SongShort,
    val beats: List<SongBeatCreate>,
)
