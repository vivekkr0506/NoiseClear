package com.noiseclear.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.noiseclear.recorder.AudioRecordManager
import com.noiseclear.viewModel.AudioViewModel

class AudioViewModelFactory(
    private val audioRecordManager: AudioRecordManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AudioViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AudioViewModel(audioRecordManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
