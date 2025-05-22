package com.example.guitarapp.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.guitarapp.data.model.RegisterRequest
import com.example.guitarapp.data.remote.AuthApi
import com.example.guitarapp.data.remote.RetrofitInstanceNoToken
import com.example.guitarapp.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.SocketTimeoutException

class RegisterViewModel : ViewModel() {

    private val authApi: AuthApi = RetrofitInstanceNoToken.authApi

    private val _registerState = MutableStateFlow<Resource<Unit>>(Resource.Idle)
    val registerState: StateFlow<Resource<Unit>> = _registerState

    fun register(username: String, email: String, password: String, skill: String, instrument: String, bio: String) {
        viewModelScope.launch {
            _registerState.value = Resource.Loading
            try {
                val request = RegisterRequest(
                    username = username,
                    email = email,
                    password = password,
                    skill = skill,
                    instrument = instrument,
                    bio = bio
                )
                val response = authApi.register(request)
                if (response.isSuccessful) {
                    _registerState.value = Resource.Success(Unit)
                } else {
                    _registerState.value = Resource.Error("Registration failed: ${response.errorBody()?.string()}")
                }
            } catch (e: SocketTimeoutException) {
                _registerState.value = Resource.Error("Connection timeout")
            } catch (e: IOException) {
                _registerState.value = Resource.Error("Network unavailable")
            } catch (e: Exception) {
                _registerState.value = Resource.Error("Error: ${e.localizedMessage}")
            }
        }
    }
}