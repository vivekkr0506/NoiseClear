package com.noiseclear.viewModel

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.noiseclear.playback.AudioPlayer
import com.noiseclear.recorder.AudioRecorder
import com.noiseclear.util.FILE_SIZE_LIMIT
import com.noiseclear.util.NOISE_THRESHOLD
import com.noiseclear.util.TIME_LIMIT
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject


@SuppressLint("UnsafeOptInUsageError")
@HiltViewModel
class AudioViewModel @Inject constructor(
    val recorder: AudioRecorder,
    @ApplicationContext private val context: Context
) : ViewModel() {

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

    private val _audioProgress = MutableStateFlow(0f)
    val audioProgress: StateFlow<Float> = _audioProgress

    private val _audioDuration = MutableStateFlow(0f)
    val audioDuration: StateFlow<Float> = _audioDuration

    private var pausedLength = 0

    init {
        updateAudioFiles(context)
    }


    @OptIn(UnstableApi::class)
     fun startRecording() {
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            val newFile = File(context.cacheDir, "audio_${System.currentTimeMillis()}.mp3")
            recorder.start(newFile)
            _audioFile.value = newFile
            _isRecording.value = true
            updateAudioFiles(context)

            viewModelScope.launch {
                recorder.getNoiseLabel{ db ->
                    _noiseLevel.value = db
                    _isNoiseHigh.value = db > NOISE_THRESHOLD
                }
            }
            val monitorThread = Thread {
                val startTime = System.currentTimeMillis()
                while (_isRecording.value) {
                    try {

                        val elapsedTime = System.currentTimeMillis() - startTime
                        val fileSize = newFile.length()

                        if (elapsedTime >= TIME_LIMIT) {
                            _isRecording.value = false
                            recorder.stop()
                            break
                        }
                        if (fileSize > FILE_SIZE_LIMIT) {
                            _isRecording.value = false
                            recorder.stop()
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
    fun stopRecording() {
        recorder.stop()
        _isRecording.value = false
        _noiseLevel.value = 0.0
    }

     fun updateAudioFiles(context: Context) {
        val audioFiles = getAudioFiles(context)
        _audioFiles.value = audioFiles
    }

    private fun getAudioFiles(context: Context): List<File> {
        return context.cacheDir.listFiles()?.filter { it.extension == "mp3" }
            ?.sortedByDescending { it.lastModified() } ?: emptyList()
    }


    fun saveRecording(name: String, recordingFile: File) {
        val renamedFile = File(recordingFile.parent, "$name.mp3")
        recordingFile.renameTo(renamedFile)
        updateAudioFiles(context)
    }

    fun deleteAudio(file: File) {
        try {
            if (file.exists()) {
                file.delete()
                updateAudioFiles(context)
            }
        } catch (e: Exception) {
            Log.e("DeleteAudio", "Exception: Failed to Delete Audio", e)
        }
    }

    fun playAudio(file: File) {
        try {
            AudioPlayer(context). playAudio(
                file = file,
                getDuration = { duration ->
                    _audioDuration.value = duration
                },
                getProgress = { progress ->
                    _audioProgress.value = progress
                    if (_audioProgress.value >= 0.92f) {
                        _isPlaying.value = false
                        updateStateToStarting()
                    }
                }
            )

        } catch (e: Exception) {
            Log.e("AudioPlay", "Exception: Failed to play audio", e)
        }

        if (!_isPlaying.value) {
            AudioPlayer(context).pauseResumeAudio(_isPlaying.value,0)
            _isPlaying.value = true
        }
    }

    private fun updateStateToStarting() {
        _audioProgress.value = 0f
        _isRecording.value = false

    }
    fun pauseAudio() {
        try {
            if (_isPlaying.value) {
                AudioPlayer(context).pauseResumeAudio(true, pausedLength)
                pausedLength = AudioPlayer(context).getCurrentPosition(true)?:0
                _isPlaying.value = false
            }
        } catch (e: Exception) {
            Log.e("AudioPlay", "Exception: Failed to pause audio", e)
        }
    }

    fun resumeAudio() {
        try {
            if (!_isPlaying.value) {
                AudioPlayer(context).pauseResumeAudio(false, pausedLength)
                _isPlaying.value = true
            }
        } catch (e: Exception) {
            Log.e("AudioPlay", "Exception: Failed to resume audio", e)
        }
    }

}
