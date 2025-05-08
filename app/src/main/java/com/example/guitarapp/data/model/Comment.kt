package com.example.guitarapp.data.model

data class Comment(
    val id: Int,
    val text: String,
    val createdAt: String,
    val author: UserDto,
    val songTutorial: SongTutorialShort,
    val idAnswerOn: Int,
    val comments: List<Comment>
){
    companion object {
        fun empty() = Comment(
            id = 0,
            text = "",
            createdAt = "",
            author = UserDto.empty(),
            songTutorial = SongTutorialShort.empty(),
            idAnswerOn = 0,
            comments = emptyList()
        )
    }
}
