package com.example.guitarapp.data.model

data class Song (
    val id: Int,
    val title: String,
    val genre: String,
    val releaseDate: String,
    val creatorId: Int?,
    val songAuthors: List<ArtistShort>
)