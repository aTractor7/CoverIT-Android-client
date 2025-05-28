package com.example.guitarapp.view_model.factory

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.guitarapp.view_model.ChordViewModel

class ChordViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChordViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChordViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}