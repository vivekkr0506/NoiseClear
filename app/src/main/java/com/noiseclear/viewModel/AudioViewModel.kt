package com.noiseclear.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noiseclear.recorder.AudioRecordManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File


class AudioViewModel(private val audioRecordManager: AudioRecordManager) : ViewModel() {
    private val _noiseLevel = MutableStateFlow(0.0)
    val noiseLevel: StateFlow<Double> = _noiseLevel

    private val _isNoiseHigh = MutableStateFlow(false)
    val isNoiseHigh: StateFlow<Boolean> = _isNoiseHigh

    private val noiseThreshold = 70.0

    private val _audioFiles = MutableStateFlow<List<File>>(emptyList())
    val audioFiles: StateFlow<List<File>> = _audioFiles
    fun startRecordingNew() {
        viewModelScope.launch {
            audioRecordManager.startRecording { db ->
                _noiseLevel.value = db
                _isNoiseHigh.value = db > noiseThreshold
            }
        }
    }

    fun stopRecordingNew() {
        audioRecordManager.stopRecording()
        _noiseLevel.value = 0.0
    }
}
