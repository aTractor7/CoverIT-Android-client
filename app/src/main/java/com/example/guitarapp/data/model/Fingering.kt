package com.example.guitarapp.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Fingering(
    val id: Int,
    val imgPath: String,
    val chordShort: ChordShort
): Parcelable