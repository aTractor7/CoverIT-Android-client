package com.example.guitarapp.view_model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.guitarapp.data.model.SongTutorial
import com.example.guitarapp.data.model.SongTutorialCreate
import com.example.guitarapp.data.model.SongTutorialShort
import com.example.guitarapp.data.remote.RetrofitInstanceWithToken
import com.example.guitarapp.data.remote.SongTutorialApi
import com.example.guitarapp.utils.Resource
import com.google.gson.stream.MalformedJsonException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.SocketTimeoutException

class TutorialViewModel(application: Application) :  AndroidViewModel(application){
    init {
        RetrofitInstanceWithToken.init(getApplication())
    }

    private val tutorialApi: SongTutorialApi = RetrofitInstanceWithToken.songTutorialApi

    private val _tutorialState = MutableStateFlow<Resource<SongTutorial>>(Resource.Idle)
    val tutorialState: StateFlow<Resource<SongTutorial>> = _tutorialState

    private val _tutorialCreateState = MutableStateFlow<Resource<Int>>(Resource.Idle)
    val tutorialCreateState: StateFlow<Resource<Int>> = _tutorialCreateState

    private val _tutorialPageState =
        MutableStateFlow<Resource<List<SongTutorialShort>>>(Resource.Idle)
    val tutorialPageState: StateFlow<Resource<List<SongTutorialShort>>> = _tutorialPageState

    fun fetchSongTutorials(
        page: Int = 0,
        size: Int = 10,
        sortField: String? = null,
        songTitle: String? = null
    ) {
        viewModelScope.launch {
            handleApiCallForList(
                stateFlow = _tutorialPageState,
                apiCall = { tutorialApi.getSongTutorials(page, size, sortField, songTitle) },
                errorMessage = "Failed to fetch tutorials"
            )
        }
    }

    fun fetchSongTutorial(tutorialId: Int) {
        viewModelScope.launch {
            handleApiCall(
                stateFlow = _tutorialState,
                apiCall = { tutorialApi.getSongTutorial(tutorialId)},
                errorMessage = "Failed to fetch tutorial"
            )
        }
    }

    fun createSongTutorial(tutorialCreate: SongTutorialCreate) {
        viewModelScope.launch {
            handleApiCallWithTransform(
                stateFlow = _tutorialCreateState,
                apiCall = { tutorialApi.createSongTutorial(tutorialCreate) },
                transform = { response ->
                    val locationHeader = response.headers()["Location"]
                    val id = locationHeader?.substringAfterLast("tutorials/")?.toIntOrNull()
                    id ?: throw Exception("Failed to parse tutorial ID from Location header")
                },
                errorMessage = "Failed to create tutorial"
            )
        }
    }


    fun updateSongTutorial(tutorialCreate: SongTutorial) {
        viewModelScope.launch {
            handleApiCallWithTransform(
                stateFlow = _tutorialCreateState,
                apiCall = { tutorialApi.updateSongTutorial(tutorialCreate.id, tutorialCreate) },
                transform = { response ->
                    val songTutorial = response.body()!!
                    songTutorial.id
                },
                errorMessage = "Failed to update tutorial"
            )
        }
    }
}