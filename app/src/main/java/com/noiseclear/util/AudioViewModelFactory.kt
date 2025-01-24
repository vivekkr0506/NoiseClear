package com.noiseclear.util

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.noiseclear.recorder.AudioRecordManager
import com.noiseclear.viewModel.AudioViewModel

class AudioViewModelFactory(
    private val audioRecordManager: AudioRecordManager,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AudioViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AudioViewModel(audioRecordManager,context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
