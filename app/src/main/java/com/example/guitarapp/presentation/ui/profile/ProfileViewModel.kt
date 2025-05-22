package com.example.guitarapp.presentation.ui.profile

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.guitarapp.data.model.UserDto
import com.example.guitarapp.data.remote.RetrofitInstanceWithToken
import com.example.guitarapp.data.remote.UserApi
import com.example.guitarapp.utils.CookieUtils.extractSessionId
import com.example.guitarapp.utils.Resource
import com.google.gson.stream.MalformedJsonException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
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
            _profileState.value = Resource.Loading
            try {
                val response = userApi.getUserProfile(userId)
                if (response.isSuccessful && response.body() != null) {
                    var userDto: UserDto = response.body()!!

                    _profileState.value = Resource.Success(userDto)
                } else {
                    _profileState.value = Resource.Error("Failed to fetch profile")
                }
            } catch (e: SocketTimeoutException) {
                _profileState.value = Resource.Error("Connection timeout")
            } catch (e: IOException) {
                _profileState.value = Resource.Error("Network unavailable")
            } catch (e: Exception) {
                _profileState.value = Resource.Error("Error: ${e.localizedMessage}")
            }
        }
    }

    fun fetchAuthenticatedUserProfile() {
        viewModelScope.launch {
            _profileState.value = Resource.Loading
            try {
                val response = userApi.getAuthenticatedUser()
                if (response.isSuccessful && response.body() != null) {
                    var userDto: UserDto = response.body()!!

                    _profileState.value = Resource.Success(userDto)
                } else {
                    _profileState.value = Resource.Error("Failed to fetch profile")
                }
            } catch (e: MalformedJsonException) {
                _profileState.value = Resource.NotAuthenticated
            } catch (e: SocketTimeoutException) {
                _profileState.value = Resource.Error("Connection timeout")
            } catch (e: IOException) {
                _profileState.value = Resource.Error("Network unavailable")
            } catch (e: Exception) {
                _profileState.value = Resource.Error("Error: ${e.localizedMessage}")
            }
        }
    }

    fun updateUserProfile(id: Int, userDto: UserDto) {
        viewModelScope.launch {
            _profileState.value = Resource.Loading
            try {
                val response = userApi.updateUserProfile(id, userDto)
                if (response.isSuccessful && response.body() != null) {
                    var userDto: UserDto = response.body()!!

                    _profileState.value = Resource.Success(userDto)
                } else {
                    _profileState.value = Resource.Error("Failed to update profile")
                }
            } catch (e: MalformedJsonException) {
                _profileState.value = Resource.NotAuthenticated
            } catch (e: SocketTimeoutException) {
                _profileState.value = Resource.Error("Connection timeout")
            } catch (e: IOException) {
                _profileState.value = Resource.Error("Network unavailable")
            } catch (e: Exception) {
                _profileState.value = Resource.Error("Error: ${e.localizedMessage}")
            }
        }
    }
}