package com.example.guitarapp.view_model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.guitarapp.data.model.PersonalLibrary
import com.example.guitarapp.data.model.PersonalLibraryCreate
import com.example.guitarapp.data.model.SongTutorialShort
import com.example.guitarapp.data.model.UserDto
import com.example.guitarapp.data.remote.PersonalLibraryApi
import com.example.guitarapp.data.remote.RetrofitInstanceWithToken
import com.example.guitarapp.utils.Resource
import com.google.gson.stream.MalformedJsonException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.SocketTimeoutException

class PersonalLibraryViewModel(application: Application):  AndroidViewModel(application){

    init {
        RetrofitInstanceWithToken.init(getApplication())
    }

    private val personalLibraryApi: PersonalLibraryApi = RetrofitInstanceWithToken.personalLibraryApi

    private val _personalLibraryPageState =
        MutableStateFlow<Resource<List<PersonalLibrary>>>(Resource.Idle)
    val personalLibraryPageState: StateFlow<Resource<List<PersonalLibrary>>> = _personalLibraryPageState

    fun fetchPersonalLibrary(
        page: Int = 0,
        size: Int = 20,
        sortField: String? = null,
        userId: Int? = null
    ) {
        viewModelScope.launch {
            _personalLibraryPageState.value = Resource.Loading
            try {
                val response = personalLibraryApi.getPersonalLibraries(
                    page = page,
                    size = size,
                    sortField = sortField,
                    userId = userId
                )

                if(response.isSuccessful && response.body() != null) {
                    _personalLibraryPageState.value = Resource.Success(response.body()!!)
                } else {
                    _personalLibraryPageState.value = Resource.Error("Failed to fetch tutorials")
                }
            } catch (e: MalformedJsonException) {
                _personalLibraryPageState.value = Resource.NotAuthenticated
            } catch (e: SocketTimeoutException) {
                _personalLibraryPageState.value = Resource.Error("Connection timeout")
            } catch (e: IOException) {
                _personalLibraryPageState.value = Resource.Error("Network unavailable")
            } catch (e: Exception) {
                _personalLibraryPageState.value = Resource.Error("Error: ${e.localizedMessage}")
            }
        }
    }

    fun createPersonalLibrary(personalLibraryCreate: PersonalLibraryCreate) {
        viewModelScope.launch {
            _personalLibraryPageState.value = Resource.Loading
            try {
                val response = personalLibraryApi.createPersonalLibraries(personalLibraryCreate)

                if(response.isSuccessful && response.body() != null) {
                    var songTutorial = SongTutorialShort.Companion.empty()
                    songTutorial.id = response.body()!!.tutorialId
                    _personalLibraryPageState.value = Resource.Success(
                        listOf(PersonalLibrary(0, "", UserDto.Companion.empty(), songTutorial)))
                } else {
                    _personalLibraryPageState.value = Resource.Error("Failed to create tutorials")
                }
            } catch (e: MalformedJsonException) {
                _personalLibraryPageState.value = Resource.NotAuthenticated
            } catch (e: SocketTimeoutException) {
                _personalLibraryPageState.value = Resource.Error("Connection timeout")
            } catch (e: IOException) {
                _personalLibraryPageState.value = Resource.Error("Network unavailable")
            } catch (e: Exception) {
                _personalLibraryPageState.value = Resource.Error("Error: ${e.localizedMessage}")
            }
        }
    }

    fun deletePersonalLibrary(tutorialId: Int) {
        viewModelScope.launch {
            _personalLibraryPageState.value = Resource.Loading
            try {
                val response = personalLibraryApi.deletePersonalLibrary(tutorialId = tutorialId)

                if(response.isSuccessful && response.body() != null) {
                    _personalLibraryPageState.value = Resource.Success(emptyList())
                } else {
                    _personalLibraryPageState.value = Resource.Error("Failed to delete tutorials")
                }
            } catch (e: MalformedJsonException) {
                _personalLibraryPageState.value = Resource.NotAuthenticated
            } catch (e: SocketTimeoutException) {
                _personalLibraryPageState.value = Resource.Error("Connection timeout")
            } catch (e: IOException) {
                _personalLibraryPageState.value = Resource.Error("Network unavailable")
            } catch (e: Exception) {
                _personalLibraryPageState.value = Resource.Error("Error: ${e.localizedMessage}")
            }
        }
    }
}