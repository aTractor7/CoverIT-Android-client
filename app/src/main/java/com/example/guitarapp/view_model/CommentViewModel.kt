package com.example.guitarapp.view_model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.guitarapp.data.model.Artist
import com.example.guitarapp.data.model.ArtistShort
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
            handleApiCallForList(
                stateFlow = _commentState,
                apiCall = { commentApi.getComments(tutorialId = tutorialId) },
                errorMessage = "Failed to fetch comments"
            )
        }
    }

    fun createComment(commentCreate: CommentCreate) {
        viewModelScope.launch {
            handleApiCall(
                stateFlow = MutableStateFlow(Resource.Loading),
                apiCall = { commentApi.createComment(commentCreate) },
                onSuccess = { createdComment ->
                    fetchSongTutorialComment(createdComment.songTutorialId)
                },
                errorMessage = "Failed to create comment"
            )
        }
    }
}