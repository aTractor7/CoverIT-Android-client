package com.example.guitarapp.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChordShort(
    val id: Int,
    val name: String
): Parcelable
