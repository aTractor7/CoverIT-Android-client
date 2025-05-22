package com.example.guitarapp.view_model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.guitarapp.data.model.Comment
import com.example.guitarapp.data.model.CommentCreate
import com.example.guitarapp.data.remote.CommentApi
import com.example.guitarapp.data.remote.RetrofitInstanceWithToken
import com.example.guitarapp.utils.Resource
import com.google.gson.stream.MalformedJsonException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.SocketTimeoutException

class CommentViewModel (application: Application) :  AndroidViewModel(application){
    init {
        RetrofitInstanceWithToken.init(getApplication())
    }

    private val commentApi: CommentApi = RetrofitInstanceWithToken.commentApi

    private val _commentState = MutableStateFlow<Resource<List<Comment>>>(Resource.Idle)
    val commentState: StateFlow<Resource<List<Comment>>> = _commentState

    fun fetchSongTutorialComment(tutorialId: Int) {
        viewModelScope.launch {
            _commentState.value = Resource.Loading
            try {
                val response = commentApi.getComments(tutorialId = tutorialId)
                if (response.isSuccessful && response.body() != null) {
                    var comments: List<Comment> = response.body()!!

                    _commentState.value = Resource.Success(comments)
                } else {
                    _commentState.value = Resource.Error("Failed to fetch comments")
                }
            } catch (e: MalformedJsonException) {
                _commentState.value = Resource.NotAuthenticated
            } catch (e: SocketTimeoutException) {
                _commentState.value = Resource.Error("Connection timeout")
            } catch (e: IOException) {
                _commentState.value = Resource.Error("Network unavailable")
            } catch (e: Exception) {
                _commentState.value = Resource.Error("Error: ${e.localizedMessage}")
            }
        }
    }

    fun createComment(commentCreate: CommentCreate) {
        viewModelScope.launch {
            _commentState.value = Resource.Loading
            try {
                val response = commentApi.createComment(commentCreate)
                if (response.isSuccessful && response.body() != null) {
                    fetchSongTutorialComment(commentCreate.songTutorialId)
                } else {
                    _commentState.value = Resource.Error("Failed to create comment")
                }
            } catch (e: MalformedJsonException) {
                _commentState.value = Resource.NotAuthenticated
            } catch (e: SocketTimeoutException) {
                _commentState.value = Resource.Error("Connection timeout")
            } catch (e: IOException) {
                _commentState.value = Resource.Error("Network unavailable")
            } catch (e: Exception) {
                _commentState.value = Resource.Error("Error: ${e.localizedMessage}")
            }
        }
    }
}