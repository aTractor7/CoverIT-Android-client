package com.example.guitarapp.data.model

data class SongTutorialShort(
    val id: Int,
    val tutorialAuthor: UserDto,
    val song: SongShort
){
    companion object {
        fun empty() = SongTutorialShort(
            id = 0,
            tutorialAuthor = UserDto.empty(),
            song = SongShort.empty(),
        )
    }
}
