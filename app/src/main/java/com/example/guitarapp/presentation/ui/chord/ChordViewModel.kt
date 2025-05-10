package com.example.guitarapp.presentation.ui.chord

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.guitarapp.data.model.Chord
import com.example.guitarapp.data.model.SongShort
import com.example.guitarapp.data.remote.ChordApi
import com.example.guitarapp.data.remote.RetrofitInstanceWithToken
import com.example.guitarapp.data.remote.SongApi
import com.example.guitarapp.utils.Resource
import com.google.gson.stream.MalformedJsonException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.SocketTimeoutException

class ChordViewModel(application: Application) :  AndroidViewModel(application){
    init {
        RetrofitInstanceWithToken.init(getApplication())
    }

    private val chordApi: ChordApi = RetrofitInstanceWithToken.chordApi

    private val _chordState = MutableStateFlow<Resource<List<Chord>>>(Resource.Idle)
    val chordState: StateFlow<Resource<List<Chord>>> = _chordState

    fun fetchChords(name: String) {
        viewModelScope.launch {
            _chordState.value = Resource.Loading
            try {
                val response = chordApi.getChords(name = name)
                if (response.isSuccessful && response.body() != null) {
                    var chords: List<Chord> = response.body()!!

                    _chordState.value = Resource.Success(chords)
                } else {
                    _chordState.value = Resource.Error("Failed to fetch chords")
                }
            } catch (e: MalformedJsonException) {
                _chordState.value = Resource.NotAuthenticated
            } catch (e: SocketTimeoutException) {
                _chordState.value = Resource.Error("Connection timeout")
            } catch (e: IOException) {
                _chordState.value = Resource.Error("Network unavailable")
            } catch (e: Exception) {
                _chordState.value = Resource.Error("Error: ${e.localizedMessage}")
            }
        }
    }
}