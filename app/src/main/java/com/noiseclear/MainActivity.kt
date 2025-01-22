package com.noiseclear

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi
import com.noiseclear.composable.MainComponent
import com.noiseclear.recorder.AudioRecordManager
import com.noiseclear.recorder.AudioRecorder
import com.noiseclear.util.AudioViewModelFactory
import com.noiseclear.viewModel.AudioViewModel

@UnstableApi
class MainActivity : ComponentActivity() {

    private val recorder by lazy { AudioRecorder(applicationContext) }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (!isGranted) {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    private lateinit var audioViewModel: AudioViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val audioRecordManager = AudioRecordManager()
        val viewModelFactory = AudioViewModelFactory(audioRecordManager)

        audioViewModel = ViewModelProvider(this, viewModelFactory).get(AudioViewModel::class.java)
        audioViewModel.checkPermission(this, requestPermissionLauncher)

        setContent {
            MainComponent(
                audioViewModel = audioViewModel,
                startRecording = { audioViewModel.startRecording(this, recorder) },
                stopRecording = { audioViewModel.stopRecording(recorder) },
                onPlayAudio = { audioViewModel.playAudio(this,it) },
                onDeleteAudio = { audioViewModel.deleteAudio(this,it) },
                onSaveRecording = { name, recordingFile ->
                    audioViewModel.saveRecording(this,
                        name,
                        recordingFile
                    )
                })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        recorder.stop()
    }
}