package com.example.guitarapp.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SongShort (
    val id: Int,
    val title: String
): Parcelable{
    companion object {
        fun empty() = SongShort(
            id = 0,
            title = ""
        )
    }
}