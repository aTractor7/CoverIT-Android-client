package com.example.guitarapp.view_model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.guitarapp.data.model.SongTutorial
import com.example.guitarapp.data.model.SongTutorialCreate
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

    fun createSongTutorial(tutorialCreate: SongTutorialCreate) {
        viewModelScope.launch {
            _tutorialCreateState.value = Resource.Loading
            try{
                val response = tutorialApi.createSongTutorial(tutorialCreate)
                if(response.isSuccessful && response.body() != null) {
                    val cookieLocation = response.headers().values("Location")

                    val id =  cookieLocation.firstOrNull { it.contains("tutorials/") }
                        ?.substringAfter("tutorials/")
                    _tutorialCreateState.value = Resource.Success(id?.toIntOrNull()!!)
                } else {
                    _tutorialCreateState.value = Resource.Error("Failed to create tutorial")
                }
            } catch (e: MalformedJsonException) {
                _tutorialCreateState.value = Resource.NotAuthenticated
            } catch (e: SocketTimeoutException) {
                _tutorialCreateState.value = Resource.Error("Connection timeout")
            } catch (e: IOException) {
                _tutorialCreateState.value = Resource.Error("Network unavailable")
            } catch (e: Exception) {
                _tutorialCreateState.value = Resource.Error("Error: ${e.localizedMessage}")
            }
        }
    }

    fun updateSongTutorial(tutorialCreate: SongTutorial) {
        viewModelScope.launch {
            _tutorialCreateState.value = Resource.Loading
            try{
                val response = tutorialApi.updateSongTutorial(
                    tutorialId = tutorialCreate.id,
                    songTutorial = tutorialCreate)
                if(response.isSuccessful && response.body() != null) {
                    val tutorial = response.body()

                    _tutorialCreateState.value = Resource.Success(tutorial?.id ?: tutorialCreate.id)
                } else {
                    _tutorialCreateState.value = Resource.Error("Failed to update tutorial")
                }
            } catch (e: MalformedJsonException) {
                _tutorialCreateState.value = Resource.NotAuthenticated
            } catch (e: SocketTimeoutException) {
                _tutorialCreateState.value = Resource.Error("Connection timeout")
            } catch (e: IOException) {
                _tutorialCreateState.value = Resource.Error("Network unavailable")
            } catch (e: Exception) {
                _tutorialCreateState.value = Resource.Error("Error: ${e.localizedMessage}")
            }
        }
    }
}