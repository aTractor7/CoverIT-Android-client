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

    private val _personalLibraryDeleteState =
        MutableStateFlow<Resource<Map<String, String>>>(Resource.Idle)
    val personalLibraryDeleteState: StateFlow<Resource<Map<String, String>>> = _personalLibraryDeleteState

    fun fetchPersonalLibrary(
        page: Int = 0,
        size: Int = 20,
        sortField: String? = null,
        userId: Int? = null
    ) {
        viewModelScope.launch {
            handleApiCallForList(
                stateFlow = _personalLibraryPageState,
                apiCall = {
                    personalLibraryApi.getPersonalLibraries(
                        page = page,
                        size = size,
                        sortField = sortField,
                        userId = userId
                    )
                },
                errorMessage = "Failed to fetch tutorials"
            )
        }
    }

    fun createPersonalLibrary(personalLibraryCreate: PersonalLibraryCreate) {
        viewModelScope.launch {
            handleApiCallWithTransform(
                stateFlow = _personalLibraryPageState,
                apiCall = { personalLibraryApi.createPersonalLibraries(personalLibraryCreate) },
                transform = { response ->
                    val tutorialId = response.body()!!.tutorialId
                    val songTutorial = SongTutorialShort.Companion.empty().apply {
                        id = tutorialId
                    }
                    listOf(PersonalLibrary(0, "", UserDto.Companion.empty(), songTutorial))
                },
                errorMessage = "Failed to create tutorials"
            )
        }
    }

    fun deletePersonalLibrary(tutorialId: Int) {
        viewModelScope.launch {
            handleApiCallForMessage(
                stateFlow = _personalLibraryDeleteState,
                apiCall = { personalLibraryApi.deletePersonalLibrary(tutorialId) },
                errorMessage = "Failed to delete tutorials"
            )
        }
    }
}