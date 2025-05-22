package com.example.guitarapp.view_model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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

class TutorialSearchViewModel(application: Application) :  AndroidViewModel(application){

    init {
        RetrofitInstanceWithToken.init(getApplication())
    }

    private val tutorialApi: SongTutorialApi = RetrofitInstanceWithToken.songTutorialApi

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
            _tutorialPageState.value = Resource.Loading
            try {
                val response = tutorialApi.getSongTutorials(
                    page = page,
                    size = size,
                    sortField = sortField,
                    songTitle = songTitle
                )

                if(response.isSuccessful && response.body() != null) {
                    _tutorialPageState.value = Resource.Success(response.body()!!)
                } else {
                    _tutorialPageState.value = Resource.Error("Failed to fetch tutorials")
                }
            } catch (e: MalformedJsonException) {
                _tutorialPageState.value = Resource.NotAuthenticated
            } catch (e: SocketTimeoutException) {
                _tutorialPageState.value = Resource.Error("Connection timeout")
            } catch (e: IOException) {
                _tutorialPageState.value = Resource.Error("Network unavailable")
            } catch (e: Exception) {
                _tutorialPageState.value = Resource.Error("Error: ${e.localizedMessage}")
            }
        }
    }
}