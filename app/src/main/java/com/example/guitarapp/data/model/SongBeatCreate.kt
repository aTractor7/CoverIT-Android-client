package com.example.guitarapp.data.model

data class SongBeatCreate(
    val text: String?,
    val beat: Int,
    val comment: String?,
    val beatChords: List<BeatChord>
)
