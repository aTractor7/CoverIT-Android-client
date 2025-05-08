package com.example.guitarapp.data.model

data class SongShort (
    val id: Int,
    val title: String
){
    companion object {
        fun empty() = SongShort(
            id = 0,
            title = ""
        )
    }
}