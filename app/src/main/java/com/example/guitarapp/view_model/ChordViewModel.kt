package com.example.guitarapp.view_model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.guitarapp.data.model.Chord
import com.example.guitarapp.data.remote.ChordApi
import com.example.guitarapp.data.remote.RetrofitInstanceWithToken
import com.example.guitarapp.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChordViewModel(application: Application) :  AndroidViewModel(application){
    init {
        RetrofitInstanceWithToken.init(getApplication())
    }

    private val chordApi: ChordApi = RetrofitInstanceWithToken.chordApi

    private val _chordState = MutableStateFlow<Resource<List<Chord>>>(Resource.Idle)
    val chordState: StateFlow<Resource<List<Chord>>> = _chordState

    fun fetchChords(name: String) {
        viewModelScope.launch {
            handleApiCallForList(
                stateFlow = _chordState,
                apiCall = { chordApi.getChords(name = name) },
                errorMessage = "Failed to fetch chords"
            )
        }
    }
}