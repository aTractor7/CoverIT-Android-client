package com.example.guitarapp.data.model

data class SongBeat(
    val id: Int,
    val text: String,
    val beat: Int,
    val songTutorialId: Int,
    val beatChords: List<BeatChord>
)