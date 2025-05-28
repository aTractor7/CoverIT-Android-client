package com.example.guitarapp.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.guitarapp.data.model.RegisterRequest
import com.example.guitarapp.data.remote.AuthApi
import com.example.guitarapp.data.remote.RetrofitInstanceNoToken
import com.example.guitarapp.utils.CookieUtils
import com.example.guitarapp.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val authApi: AuthApi = RetrofitInstanceNoToken.authApi

    private val _loginState = MutableStateFlow<Resource<Pair<String, Int>>>(Resource.Idle)
    val loginState: StateFlow<Resource<Pair<String, Int>>> = _loginState

    private val _registerState = MutableStateFlow<Resource<RegisterRequest>>(Resource.Idle)
    val registerState: StateFlow<Resource<RegisterRequest>> = _registerState

    fun login(username: String, password: String) {
        viewModelScope.launch {
            handleApiCallWithTransform(
                stateFlow = _loginState,
                apiCall = { authApi.login(username, password) },
                transform = { response ->
                    val sessionId = CookieUtils.extractSessionId(response)
                    val userId = CookieUtils.extractUserId(response)?.toIntOrNull()

                    if (sessionId != null && userId != null) {
                        Pair(sessionId, userId)
                    } else {
                        throw IllegalStateException("Wrong username or password")
                    }
                },
                errorMessage = "Login failed"
            )
        }
    }

    fun register(request: RegisterRequest) {
        viewModelScope.launch {
            handleApiCall(
                stateFlow = _registerState,
                apiCall = { authApi.register(request) },
                errorMessage = "Registration failed"
            )
        }
    }
}