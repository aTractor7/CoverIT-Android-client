package com.example.guitarapp.data.model

data class PersonalLibrary(

    val id: Int,
    val addDate: String,
    val owner: UserDto,
    val songTutorial: SongTutorialShort
)
