package com.example.guitarapp.view_model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.guitarapp.data.model.Song
import com.example.guitarapp.data.model.SongShort
import com.example.guitarapp.data.remote.RetrofitInstanceWithToken
import com.example.guitarapp.data.remote.SongApi
import com.example.guitarapp.utils.Resource
import com.google.gson.stream.MalformedJsonException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.SocketTimeoutException

class SongViewModel (application: Application) :  AndroidViewModel(application){
    init {
        RetrofitInstanceWithToken.init(getApplication())
    }

    private val songApi: SongApi = RetrofitInstanceWithToken.songApi

    private val _songState = MutableStateFlow<Resource<List<SongShort>>>(Resource.Idle)
    val songState: StateFlow<Resource<List<SongShort>>> = _songState

    fun fetchSongs(title: String) {
        viewModelScope.launch {
            _songState.value = Resource.Loading
            try {
                val response = songApi.getSongs(songTitle = title)
                if (response.isSuccessful && response.body() != null) {
                    var artist: List<SongShort> = response.body()!!

                    _songState.value = Resource.Success(artist)
                } else {
                    _songState.value = Resource.Error("Failed to fetch songs")
                }
            } catch (e: MalformedJsonException) {
                _songState.value = Resource.NotAuthenticated
            } catch (e: SocketTimeoutException) {
                _songState.value = Resource.Error("Connection timeout")
            } catch (e: IOException) {
                _songState.value = Resource.Error("Network unavailable")
            } catch (e: Exception) {
                _songState.value = Resource.Error("Error: ${e.localizedMessage}")
            }
        }
    }

    fun createSong(song: Song) {
        viewModelScope.launch {
            _songState.value = Resource.Loading
            try {
                val response = songApi.createSongs(song)
                if (response.isSuccessful && response.body() != null) {
                    var song: Song = response.body()!!

                    _songState.value = Resource.Success(
                        listOf(SongShort(song.id, song.title)))
                } else {
                    _songState.value = Resource.Error("Failed to create song")
                }
            } catch (e: MalformedJsonException) {
                _songState.value = Resource.NotAuthenticated
            } catch (e: SocketTimeoutException) {
                _songState.value = Resource.Error("Connection timeout")
            } catch (e: IOException) {
                _songState.value = Resource.Error("Network unavailable")
            } catch (e: Exception) {
                _songState.value = Resource.Error("Error: ${e.localizedMessage}")
            }
        }
    }
}