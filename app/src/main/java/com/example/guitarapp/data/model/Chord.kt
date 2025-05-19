package com.example.guitarapp.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Chord(
    val id: Int,
    val name: String,
    val fingerings: List<Fingering>?
): Parcelable