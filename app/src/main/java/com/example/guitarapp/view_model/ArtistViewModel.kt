package com.example.guitarapp.view_model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.guitarapp.data.model.Artist
import com.example.guitarapp.data.model.ArtistShort
import com.example.guitarapp.data.remote.ArtistApi
import com.example.guitarapp.data.remote.RetrofitInstanceWithToken
import com.example.guitarapp.utils.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ArtistViewModel(application: Application) : AndroidViewModel(application) {
    init {
        RetrofitInstanceWithToken.init(getApplication())
    }

    private val artistApi: ArtistApi = RetrofitInstanceWithToken.artistApi

    private val _artistState = MutableStateFlow<Resource<List<ArtistShort>>>(Resource.Idle)
    val artistState: StateFlow<Resource<List<ArtistShort>>> = _artistState

    fun fetchArtists(name: String) {
        viewModelScope.launch {
            handleApiCallForList(
                stateFlow = _artistState,
                apiCall = { artistApi.getArtist(name = name) },
                errorMessage = "Failed to fetch artists"
            )
        }
    }

    fun createArtist(artist: Artist) {
        viewModelScope.launch {
            handleApiCallWithTransform(
                stateFlow = _artistState,
                apiCall = { artistApi.createArtist(artist) },
                transform = { response ->
                    val createdArtist = response.body()!!
                    listOf(ArtistShort(createdArtist.id, createdArtist.name))
                },
                errorMessage = "Failed to create artist"
            )
        }
    }
}