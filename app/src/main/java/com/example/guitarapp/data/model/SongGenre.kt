package com.example.guitarapp.data.model

enum class SongGenre(val displayName: String) {
    ROCK("Rock"),
    POP("Pop"),
    HIP_HOP("Hip Hop"),
    RAP("Rap"),
    JAZZ("Jazz"),
    BLUES("Blues"),
    CLASSICAL("Classical"),
    COUNTRY("Country"),
    REGGAE("Reggae"),
    METAL("Metal"),
    PUNK("Punk"),
    SOUL("Soul"),
    RNB("R&B"),
    FOLK("Folk"),
    INDIE("Indie"),
    ALTERNATIVE("Alternative"),
    LATIN("Latin"),
    GOSPEL("Gospel"),
    SOUNDTRACK("Soundtrack");

    companion object {
        fun fromDisplayName(displayName: String): SongGenre? {
            return SongGenre.entries.find { it.displayName == displayName }
        }
    }
}