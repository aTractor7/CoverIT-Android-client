package com.example.guitarapp.view_model

import kotlinx.coroutines.flow.MutableStateFlow
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import com.google.gson.stream.MalformedJsonException
import com.example.guitarapp.utils.Resource
import com.example.guitarapp.utils.Resource.Loading
import com.example.guitarapp.utils.Resource.NotAuthenticated
import com.example.guitarapp.utils.Resource.Success
import com.example.guitarapp.utils.Resource.Error

suspend fun <T : Any> handleApiCall(
    stateFlow: MutableStateFlow<Resource<T>>,
    apiCall: suspend () -> Response<T>,
    onSuccess: (T) -> Unit = { data -> stateFlow.value = Success(data) },
    errorMessage: String = "Failed to fetch data"
) {
    stateFlow.value = Loading
    try {
        val response = apiCall()
        if (response.isSuccessful && response.body() != null) {
            onSuccess(response.body()!!)
        } else {
            stateFlow.value = Error(errorMessage)
        }
    } catch (e: MalformedJsonException) {
        stateFlow.value = NotAuthenticated
    } catch (e: SocketTimeoutException) {
        stateFlow.value = Error("Connection timeout")
    } catch (e: IOException) {
        stateFlow.value = Error("Network unavailable")
    } catch (e: Exception) {
        stateFlow.value = Error("Error: ${e.localizedMessage}")
    }
}

suspend fun <T : Any> handleApiCallForList(
    stateFlow: MutableStateFlow<Resource<List<T>>>,
    apiCall: suspend () -> Response<List<T>>,
    errorMessage: String = "Failed to fetch data"
) {
    handleApiCall(
        stateFlow = stateFlow,
        apiCall = apiCall,
        errorMessage = errorMessage
    )
}

suspend fun handleApiCallForMessage(
    stateFlow: MutableStateFlow<Resource<Map<String, String>>>,
    apiCall: suspend () -> Response<Map<String, String>>,
    errorMessage: String = "Operation failed"
) {
    stateFlow.value = Loading
    try {
        val response = apiCall()
        if (response.isSuccessful && response.body() != null) {
            stateFlow.value = Success(response.body()!!)
        } else {
            stateFlow.value = Error("$errorMessage: ${response.errorBody()?.string()}")
        }
    } catch (e: MalformedJsonException) {
        stateFlow.value = NotAuthenticated
    } catch (e: SocketTimeoutException) {
        stateFlow.value = Error("Connection timeout")
    } catch (e: IOException) {
        stateFlow.value = Error("Network unavailable")
    } catch (e: Exception) {
        stateFlow.value = Error("Error: ${e.localizedMessage}")
    }
}

suspend fun <T : Any, R : Any> handleApiCallWithTransform(
    stateFlow: MutableStateFlow<Resource<R>>,
    apiCall: suspend () -> Response<T>,
    transform: (Response<T>) -> R,
    errorMessage: String = "Operation failed"
) {
    stateFlow.value = Loading
    try {
        val response = apiCall()
        if (response.isSuccessful && response.body() != null) {
            stateFlow.value = Success(transform(response))
        } else {
            stateFlow.value = Error(errorMessage)
        }
    } catch (e: MalformedJsonException) {
        stateFlow.value = NotAuthenticated
    } catch (e: SocketTimeoutException) {
        stateFlow.value = Error("Connection timeout")
    } catch (e: IOException) {
        stateFlow.value = Error("Network unavailable")
    } catch (e: Exception) {
        stateFlow.value = Error("Error: ${e.localizedMessage}")
    }
}
