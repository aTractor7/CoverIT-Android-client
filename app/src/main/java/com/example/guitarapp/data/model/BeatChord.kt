package com.example.guitarapp.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BeatChord(
    val id: Int,
    val songBeatId: Int,
    val chord: Chord,
    var recommendedFingering: Fingering?
): Parcelable