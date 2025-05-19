package com.example.guitarapp.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SongTutorialShort(
    var id: Int,
    val tutorialAuthor: UserDto,
    val song: SongShort
): Parcelable{
    companion object {
        fun empty() = SongTutorialShort(
            id = 0,
            tutorialAuthor = UserDto.empty(),
            song = SongShort.empty(),
        )
    }
}
