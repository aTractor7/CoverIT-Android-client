package com.example.guitarapp.data.model

data class SongBeatCreate(
    var text: String?,
    var beat: Int,
    var comment: String?,
    var beatChords: MutableList<BeatChord>
)
