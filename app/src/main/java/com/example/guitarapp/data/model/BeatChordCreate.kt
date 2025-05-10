package com.example.guitarapp.data.model

data class BeatChordCreate(
    val chord: Chord,
    val recommendedFingering: FingeringShort?
)
