package com.example.guitarapp.data.model

data class CommentCreate(
    val text: String,
    val idAnswerOn: Int,
    val songTutorialId: Int,
)
