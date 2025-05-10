package com.example.guitarapp.data.model

data class Chord(
    val id: Int,
    val name: String,
    val fingerings: List<Fingering>?
)