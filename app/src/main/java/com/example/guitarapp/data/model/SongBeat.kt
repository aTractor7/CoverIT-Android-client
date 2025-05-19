package com.example.guitarapp.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SongBeat(
    val id: Int,
    val text: String?,
    val beat: Int,
    val comment: String?,
    val songTutorialId: Int,
    val beatChords: List<BeatChord>
): Parcelable