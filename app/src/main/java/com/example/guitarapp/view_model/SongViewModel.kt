package com.example.guitarapp.view_model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.guitarapp.data.model.ArtistShort
import com.example.guitarapp.data.model.Song
import com.example.guitarapp.data.model.SongShort
import com.example.guitarapp.data.remote.RetrofitInstanceWithToken
import com.example.guitarapp.data.remote.SongApi
import com.example.guitarapp.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SongViewModel (application: Application) :  AndroidViewModel(application){
    init {
        RetrofitInstanceWithToken.init(getApplication())
    }

    private val songApi: SongApi = RetrofitInstanceWithToken.songApi

    private val _songState = MutableStateFlow<Resource<List<SongShort>>>(Resource.Idle)
    val songState: StateFlow<Resource<List<SongShort>>> = _songState

    fun fetchSongs(title: String) {
        viewModelScope.launch {
            handleApiCallForList(
                stateFlow = _songState,
                apiCall = { songApi.getSongs(songTitle = title) },
                errorMessage = "Failed to fetch artists"
            )
        }
    }

    fun createSong(song: Song) {
        viewModelScope.launch {
            handleApiCallWithTransform(
                stateFlow = _songState,
                apiCall = { songApi.createSongs(song) },
                transform = { response ->
                    val createdSong = response.body()!!
                    listOf(SongShort(createdSong.id, createdSong.title))
                },
                errorMessage = "Failed to create song"
            )
        }
    }
}