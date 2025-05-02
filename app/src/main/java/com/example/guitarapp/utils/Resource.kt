package com.example.guitarapp.utils

sealed class Resource<out T> {
    data object Idle : Resource<Nothing>()
    data object Loading : Resource<Nothing>()
    data object NotAuthenticated: Resource<Nothing>()
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val message: String?) : Resource<Nothing>()
}