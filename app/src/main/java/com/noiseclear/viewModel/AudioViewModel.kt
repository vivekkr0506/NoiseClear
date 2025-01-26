package com.noiseclear.viewModel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.OptIn
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.google.firebase.analytics.FirebaseAnalytics
import com.noiseclear.playback.AudioPlayer
import com.noiseclear.recorder.AudioRecordManager
import com.noiseclear.recorder.AudioRecorder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File


class AudioViewModel(context: Context) : ViewModel() {
    private val audioRecordManager=AudioRecordManager()
    private val noiseThreshold = 70.0

    private val _noiseLevel = MutableStateFlow(0.0)
    val noiseLevel: StateFlow<Double> = _noiseLevel

    private val _isNoiseHigh = MutableStateFlow(false)
    val isNoiseHigh: StateFlow<Boolean> = _isNoiseHigh

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> get() = _isRecording

    private val _audioFile = MutableStateFlow<File?>(null)
    val audioFile: StateFlow<File?> = _audioFile

    private val _audioFiles = MutableStateFlow<List<File>>(emptyList())
    val audioFiles: StateFlow<List<File>> = _audioFiles

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> get() = _isPlaying

    init {
        updateAudioFiles(context)
    }

    fun checkPermission(
        context: Context,
        requestPermissionLauncher: ActivityResultLauncher<String>
    ) {
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
            }

            else -> {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    @OptIn(UnstableApi::class)
    fun startRecording(context: Context, recorder: AudioRecorder) {
        val firebaseAnalytics = FirebaseAnalytics.getInstance(context)
        val sizeLimit = 1 * 1024 * 1024 // 5 MB limit
        val params = Bundle()
        params.putString(FirebaseAnalytics.Param.SCREEN_NAME, "Hello" as String?)
        params.putString("Name", "Vivek" as String?)
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, params)

        try {
            if (ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            startRecordingNew()
            val newFile = File(context.cacheDir, "audio_${System.currentTimeMillis()}.mp3")
           // recorder.start(newFile)
            recorder.startAudio(newFile)
            _audioFile.value = newFile
            _isRecording.value = true
            updateAudioFiles(context)

            val monitorThread = Thread {
                while (_isRecording.value) {
                    try {
                        val fileSize = newFile.length()
                        if (fileSize > sizeLimit) {
                            _isRecording.value = false
                            Toast.makeText(context,"Size limit exceeded", Toast.LENGTH_LONG).show()
                            break
                        }
                        Thread.sleep(500)
                    } catch (e: InterruptedException) {
                        Log.e("AudioRecord", "Monitoring thread interrupted", e)
                    }
                }
            }
            monitorThread.start()
        } catch (e: Exception) {
            Log.e("AudioRecord", "Exception: Failed to start recording", e)
        }
    }

    @OptIn(UnstableApi::class)
    fun stopRecording(recorder: AudioRecorder) {
        recorder.stop()
        _isRecording.value = false
        stopRecordingNew()
    }

    private fun updateAudioFiles(context: Context) {
        val audioFiles = getAudioFiles(context)
        _audioFiles.value = audioFiles
    }

    private fun getAudioFiles(context: Context): List<File> {
        return context.cacheDir.listFiles()?.filter { it.extension == "mp3" }
            ?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    private fun startRecordingNew() {
        viewModelScope.launch {
            audioRecordManager.startRecording { db ->
                _noiseLevel.value = db
                _isNoiseHigh.value = db > noiseThreshold
            }
        }
    }

    private fun stopRecordingNew() {
        audioRecordManager.stopRecording()
        _noiseLevel.value = 0.0
    }

    fun saveRecording(context: Context,name: String, recordingFile: File) {
        val renamedFile = File(recordingFile.parent, "$name.mp3")
        recordingFile.renameTo(renamedFile)
        updateAudioFiles(context)
    }

    fun deleteAudio(context: Context,file: File) {
        try {
            if (file.exists()) {
                file.delete()
                updateAudioFiles(context)
            }
        } catch (e: Exception) {
            Log.e("DeleteAudio", "Exception: Failed to Delete Audio", e)
        }
    }

    fun playAudio(context: Context,file: File) {
        try {
            AudioPlayer(context).playAudio(file)
        } catch (e: Exception) {
            Log.e("AudioPlay", "Exception: Failed to play audio", e)
        }

        if (!_isPlaying.value) {
            AudioPlayer(context).pauseAudio()
            _isPlaying.value = true
        }
    }

    fun pauseAudio(context: Context) {
        try {
            Log.e("AudioPlay", "Exception: Failed to pause audio" )
            _isPlaying.value = false
            AudioPlayer(context).pauseAudio()
        } catch (e: Exception) {
            Log.e("AudioPlay", "Exception: Failed to pause audio", e)
        }
    }

    fun resumeAudio(context: Context,file: File) {
        try {
            _isPlaying.value = true
            Log.e("AudioPlay", "Exception: Failed to Resume audio" )
            AudioPlayer(context).playAudio(file)

        } catch (e: Exception) {
            Log.e("AudioPlay", "Exception: Failed to resume audio", e)
        }
    }

}
