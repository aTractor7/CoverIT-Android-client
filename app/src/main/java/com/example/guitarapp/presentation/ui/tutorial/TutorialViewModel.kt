package com.example.guitarapp.presentation.ui.tutorial

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.guitarapp.data.model.SongTutorial
import com.example.guitarapp.data.remote.RetrofitInstanceWithToken
import com.example.guitarapp.data.remote.SongTutorialApi
import com.example.guitarapp.utils.CookieUtils.extractSessionId
import com.example.guitarapp.utils.Resource
import com.google.gson.stream.MalformedJsonException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TutorialViewModel(application: Application) :  AndroidViewModel(application){
    init {
        RetrofitInstanceWithToken.init(getApplication())
    }

    private val tutorialApi: SongTutorialApi = RetrofitInstanceWithToken.songTutorialApi

    private val _profileState = MutableStateFlow<Resource<SongTutorial>>(Resource.Idle)
    val profileState: StateFlow<Resource<SongTutorial>> = _profileState

    fun fetchSongTutorial(tutorialId: Int) {
        viewModelScope.launch {
            _profileState.value = Resource.Loading
            try{
                val response = tutorialApi.getSongTutorial(tutorialId)
                if(response.isSuccessful && response.body() != null) {
                    var tutorial: SongTutorial = response.body()!!

                    _profileState.value = Resource.Success(tutorial)
                } else {
                    _profileState.value = Resource.Error("Failed to fetch tutorial")
                }
            } catch (e: MalformedJsonException) {
                _profileState.value = Resource.NotAuthenticated
            } catch (e: Exception) {
            _profileState.value = Resource.Error("Network error: ${e.message}")
            }
        }
    }
}