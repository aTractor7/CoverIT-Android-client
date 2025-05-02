package com.example.guitarapp.data.model

data class BeatChord(
    val id: Int,
    val songBeatId: Int,
    val chord: Chord,
    val recommendedFingering: Fingering
)