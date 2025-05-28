package com.example.guitarapp.view_model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.guitarapp.data.model.UserDto
import com.example.guitarapp.data.remote.RetrofitInstanceWithToken
import com.example.guitarapp.data.remote.UserApi
import com.example.guitarapp.utils.Resource
import com.google.gson.stream.MalformedJsonException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.SocketTimeoutException

class ProfileViewModel(application: Application) :  AndroidViewModel(application){
    init {
        RetrofitInstanceWithToken.init(getApplication())
    }

    private val userApi: UserApi = RetrofitInstanceWithToken.userApi

    private val _profileState = MutableStateFlow<Resource<UserDto>>(Resource.Idle)
    val profileState: StateFlow<Resource<UserDto>> = _profileState

    fun fetchUserProfile(userId: Int) {
        viewModelScope.launch {
            handleApiCall(
                stateFlow = _profileState,
                apiCall = { userApi.getUserProfile(userId) },
                errorMessage = "Failed to fetch profile"
            )
        }
    }

    fun fetchAuthenticatedUserProfile() {
        viewModelScope.launch {
            handleApiCall(
                stateFlow = _profileState,
                apiCall = { userApi.getAuthenticatedUser() },
                errorMessage = "Failed to fetch profile"
            )
        }
    }

    fun updateUserProfile(id: Int, userDto: UserDto) {
        viewModelScope.launch {
            handleApiCall(
                stateFlow = _profileState,
                apiCall = { userApi.updateUserProfile(id, userDto) },
                errorMessage = "Failed to update profile"
            )
        }
    }
}