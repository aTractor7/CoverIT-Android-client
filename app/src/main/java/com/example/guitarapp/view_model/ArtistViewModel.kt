package com.example.guitarapp.view_model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.guitarapp.data.model.Artist
import com.example.guitarapp.data.model.ArtistShort
import com.example.guitarapp.data.remote.ArtistApi
import com.example.guitarapp.data.remote.RetrofitInstanceWithToken
import com.example.guitarapp.utils.Resource
import com.google.gson.stream.MalformedJsonException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.SocketTimeoutException

class ArtistViewModel (application: Application) :  AndroidViewModel(application){
    init {
        RetrofitInstanceWithToken.init(getApplication())
    }

    private val artistApi: ArtistApi = RetrofitInstanceWithToken.artistApi

    private val _artistState = MutableStateFlow<Resource<List<ArtistShort>>>(Resource.Idle)
    val artistState: StateFlow<Resource<List<ArtistShort>>> = _artistState

    fun fetchArtists(name: String) {
        viewModelScope.launch {
            _artistState.value = Resource.Loading
            try {
                val response = artistApi.getArtist(name = name)
                if (response.isSuccessful && response.body() != null) {
                    var artist: List<ArtistShort> = response.body()!!

                    _artistState.value = Resource.Success(artist)
                } else {
                    _artistState.value = Resource.Error("Failed to fetch artist")
                }
            } catch (e: MalformedJsonException) {
                _artistState.value = Resource.NotAuthenticated
            } catch (e: SocketTimeoutException) {
                _artistState.value = Resource.Error("Connection timeout")
            } catch (e: IOException) {
                _artistState.value = Resource.Error("Network unavailable")
            } catch (e: Exception) {
                _artistState.value = Resource.Error("Error: ${e.localizedMessage}")
            }
        }
    }

    fun createArtist(artist: Artist) {
        viewModelScope.launch {
            _artistState.value = Resource.Loading
            try {
                val response = artistApi.createArtist(artist)
                if (response.isSuccessful && response.body() != null) {
                    var artist: Artist = response.body()!!

                    _artistState.value = Resource.Success(
                        listOf(ArtistShort(artist.id, artist.name)))
                } else {
                    _artistState.value = Resource.Error("Failed to create artist")
                }
            } catch (e: MalformedJsonException) {
                _artistState.value = Resource.NotAuthenticated
            } catch (e: SocketTimeoutException) {
                _artistState.value = Resource.Error("Connection timeout")
            } catch (e: IOException) {
                _artistState.value = Resource.Error("Network unavailable")
            } catch (e: Exception) {
                _artistState.value = Resource.Error("Error: ${e.localizedMessage}")
            }
        }
    }
}