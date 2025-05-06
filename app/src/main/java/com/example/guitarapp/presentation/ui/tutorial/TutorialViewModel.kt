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
import java.io.IOException
import java.net.SocketTimeoutException

class TutorialViewModel(application: Application) :  AndroidViewModel(application){
    init {
        RetrofitInstanceWithToken.init(getApplication())
    }

    private val tutorialApi: SongTutorialApi = RetrofitInstanceWithToken.songTutorialApi

    private val _tutorialState = MutableStateFlow<Resource<SongTutorial>>(Resource.Idle)
    val tutorialState: StateFlow<Resource<SongTutorial>> = _tutorialState

    fun fetchSongTutorial(tutorialId: Int) {
        viewModelScope.launch {
            _tutorialState.value = Resource.Loading
            try{
                val response = tutorialApi.getSongTutorial(tutorialId)
                if(response.isSuccessful && response.body() != null) {
                    var tutorial: SongTutorial = response.body()!!

                    _tutorialState.value = Resource.Success(tutorial)
                } else {
                    _tutorialState.value = Resource.Error("Failed to fetch tutorial")
                }
            } catch (e: MalformedJsonException) {
                _tutorialState.value = Resource.NotAuthenticated
            } catch (e: SocketTimeoutException) {
                _tutorialState.value = Resource.Error("Connection timeout")
            } catch (e: IOException) {
                _tutorialState.value = Resource.Error("Network unavailable")
            } catch (e: Exception) {
                _tutorialState.value = Resource.Error("Error: ${e.localizedMessage}")
            }
        }
    }
}