package com.example.guitarapp.view_model.factory

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.guitarapp.view_model.PersonalLibraryViewModel

class PersonalLibraryViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PersonalLibraryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PersonalLibraryViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}