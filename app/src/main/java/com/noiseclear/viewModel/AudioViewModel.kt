package com.noiseclear.viewModel

import android.app.Application
import android.media.AudioFormat
import android.media.AudioRecord
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.noiseclear.playback.AudioPlayer
import com.noiseclear.recorder.AudioRecorder
import java.io.File


class AudioViewModel(application: Application) : ViewModel() {

    private val recorder = AudioRecorder(application)
    private val player = AudioPlayer(application)

    private val _isRecording = MutableLiveData(false)
    var isRecording: LiveData<Boolean> = _isRecording

    private val _isAudioPlaying = MutableLiveData(false)
    val isAudioPlaying: LiveData<Boolean> = _isAudioPlaying

    private val _noiseLevel = MutableLiveData(0.0)
    val noiseLevel: LiveData<Double> = _noiseLevel

    private val _audioFiles = MutableLiveData<List<File>>()
    val audioFiles: LiveData<List<File>> = _audioFiles

    private val cacheDir = application.cacheDir
    private val bufferSize = AudioRecord.getMinBufferSize(
        16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
    )

    private var currentRecordingFile: File? = null

    init {
        loadAudioFiles()
    }

    fun startRecording() {
        try {
            val outputFile = File(cacheDir, "audio_${System.currentTimeMillis()}.mp3")
            recorder.start(outputFile)
            currentRecordingFile = outputFile
            _isRecording.postValue(true)
        } catch (e: Exception) {
            Log.e("AudioRecord", "Exception: Failed to start recording", e)
        }
    }

    fun stopRecording() {
        recorder.stop()
        currentRecordingFile = null
        _isRecording.postValue(false)
        loadAudioFiles()
    }

    fun playAudio(file: File) {
        try {
            stopAudio()
            player.playFile(file)
            _isAudioPlaying.postValue(true)
        } catch (e: Exception) {
            Log.e("AudioPlay", "Exception: Failed to play audio", e)
        }
    }

    fun stopAudio() {
        player.stop()
        _isAudioPlaying.postValue(false)
    }

    private fun loadAudioFiles() {
        val files = cacheDir.listFiles()?.filter { it.extension == "mp3" }?.sortedByDescending { it.lastModified() }
            ?: emptyList()
        _audioFiles.postValue(files)
    }
}
