package com.example.guitarapp.presentation.ui.login

import android.app.DownloadManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.guitarapp.data.remote.AuthApi
import com.example.guitarapp.data.remote.RetrofitInstanceNoToken
import com.example.guitarapp.utils.CookieUtils.extractSessionId
import com.example.guitarapp.utils.CookieUtils.extractUserId
import com.example.guitarapp.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val authApi: AuthApi = RetrofitInstanceNoToken.authApi

    private val _loginState = MutableStateFlow<Resource<Pair<String, Int>>>(Resource.Idle)
    val loginState: StateFlow<Resource<Pair<String, Int>>> = _loginState

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = Resource.Loading
            try {
                val response = authApi.login(username, password)
                if (response.isSuccessful) {
                    val sessionId = extractSessionId(response)
                    val userId = extractUserId(response)?.toIntOrNull()

                    if (sessionId != null && userId != null) {
                        _loginState.value = Resource.Success(Pair(sessionId, userId))
                    } else {
                        _loginState.value = Resource.Error("Wrong username or password")
                    }
                } else {
                    _loginState.value = Resource.Error("Login failed: ${response.message()}")
                }
            } catch (e: Exception) {
                _loginState.value = Resource.Error("An error occurred: ${e.localizedMessage}")
            }
        }
    }
}
