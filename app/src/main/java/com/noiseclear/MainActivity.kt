package com.noiseclear

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import com.noiseclear.composable.AudioList
import com.noiseclear.composable.AudioRecordAppUI
import com.noiseclear.composable.ConfirmationDialogue
import com.noiseclear.composable.NoiseMeter
import com.noiseclear.composable.TopAppBar
import com.noiseclear.composable.WaveAnimation
import com.noiseclear.playback.AudioPlayer
import com.noiseclear.recorder.AudioRecordManager
import com.noiseclear.recorder.AudioRecorder
import com.noiseclear.util.AudioViewModelFactory
import com.noiseclear.viewModel.AudioViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@UnstableApi
class MainActivity : ComponentActivity() {

    private var isRecording = false
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (!isGranted) {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }

    private val recorder by lazy {
        AudioRecorder(applicationContext)
    }

    private val player by lazy {
        AudioPlayer(applicationContext)
    }

    private var audioFile: File? = null
    private var audioFiles = mutableListOf<File>()

   lateinit var  audioViewModel: AudioViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermission()
        audioFiles =  getAudioFiles().toMutableList()

        val audioRecordManager = AudioRecordManager()
        val viewModelFactory = AudioViewModelFactory(audioRecordManager)

        audioViewModel = ViewModelProvider(this, viewModelFactory).get(AudioViewModel::class.java)


        val audioDir = File(filesDir, "audio")
        if (!audioDir.exists()) {
            audioDir.mkdir()
        }
        setContent {
            MainComponent(
                audioViewModel = audioViewModel,
                isRecording = isRecording,
                filesList = audioFiles,
                startRecording = { startRecording() },
                stopRecording = {
                    stopRecording()
                    audioFiles = getAudioFiles().toMutableList()
                },
                onPlayAudio = {playAudio(it) },
                onDeleteAudio = { DeleteAudio(it) },
                currentFile = audioFile,
                onSaveRecording = {  name,recordingFile ->
                    val renamedFile = File(recordingFile.parent,"$name.mp3")
                    audioFile?.renameTo(renamedFile)
                    audioFiles.add(renamedFile)
                })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        recorder.stop()
    }

    private fun checkPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
            }

            else -> requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun startRecording() {
        try {
            if (ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            File(cacheDir, "audio_${System.currentTimeMillis()}.mp3").also {
                recorder.start(it)
                audioFile = it
            }

            audioViewModel.startRecordingNew()
            isRecording = true
            lifecycleScope.launch(Dispatchers.IO) {
                while (isRecording) {
                    setContent {
                        var filesList by remember { mutableStateOf(getAudioFiles()) }
                        MainComponent(
                            audioViewModel = audioViewModel,
                            isRecording = isRecording,
                            filesList = audioFiles,
                            startRecording = { startRecording() },
                            stopRecording = {
                                stopRecording()
                                filesList = getAudioFiles()
                            },
                            onPlayAudio = { playAudio(it) },
                            onDeleteAudio = { DeleteAudio(it) },
                            currentFile = audioFile,
                            onSaveRecording = {  name,recordingFile ->
                                val renamedFile = File(recordingFile.parent,"$name.mp3")
                                audioFile?.renameTo(renamedFile)
                                audioFiles.add(renamedFile)
                            }
                        )
                    }

                }
            }
        } catch (e: Exception) {
            Toast.makeText(
                applicationContext, "Failed to start recording ${e.message}", Toast.LENGTH_SHORT
            ).show()
            Log.e("AudioRecord", "Exception: Failed to start recording", e)
        }
    }

    fun playAudio(file: File) {
        try {
            player.playFile(file)
        } catch (e: Exception) {
            Log.e("AudioPlay", "Exception: Failed to play audio", e)
        }
    }
    private fun stopRecording() {
        setContent {
            audioFile?.let{ file ->
                NoiseMeter(100.0)
                MainComponent(
                    audioViewModel = audioViewModel,
                    isRecording = false,
                    filesList = audioFiles,
                    startRecording = { startRecording() },
                    stopRecording = {
                        stopRecording()
                        audioFiles = getAudioFiles().toMutableList()
                    },
                    onPlayAudio = { playAudio(it) },
                    onDeleteAudio = { DeleteAudio(it) },
                    currentFile = file,
                    onSaveRecording = {  name,recordingFile ->
                        val renamedFile = File(recordingFile.parent,"$name.mp3")
                        file.renameTo(renamedFile)
                        audioFiles.add(renamedFile)
                    })
            }

        }
        Toast.makeText(applicationContext, "Recording Stopped", Toast.LENGTH_SHORT).show()
        isRecording = false
        recorder.stop()
        audioViewModel.stopRecordingNew()
    }


    private fun getAudioFiles(): List<File> {
        return cacheDir.listFiles()?.filter { it.extension == "mp3" }
            ?.sortedByDescending { it.lastModified() } ?: emptyList()
    }


    private fun DeleteAudio(file: File) {
        try {
            if (file.exists()) {
                file.delete()
                Toast.makeText(this, "Audio Deleted", Toast.LENGTH_SHORT).show()
                audioFiles = getAudioFiles().toMutableList()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Unable to Delete Audio ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("DeleteAudio", "Exception: Failed to Delete Audio", e)
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainComponent(
    audioViewModel : AudioViewModel,
    isRecording: Boolean,
    filesList: List<File>,
    startRecording: () -> Unit,
    stopRecording: () -> Unit,
    onPlayAudio: (File) -> Unit,
    onDeleteAudio: (File) -> Unit,
    currentFile: File?,
    onSaveRecording: ((String,File) -> Unit)?) {
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    val noiseLevel by audioViewModel.noiseLevel.collectAsState()
    val isNoiseHigh by audioViewModel.isNoiseHigh.collectAsState()
    LaunchedEffect(currentFile, isRecording) {
        if (currentFile != null && !isRecording) {
            showSaveDialog = true
        }
    }
    if(showSaveDialog && currentFile != null){
        ConfirmationDialogue(
            recordingFile = currentFile,
            onDismissRequest = { showSaveDialog = false },
            onSave = { name,recordingFile ->
                if (onSaveRecording != null) {
                    onSaveRecording(name,recordingFile)
                    showSaveDialog =  false
                }
            },
            onDelete = { onDeleteAudio(currentFile) })


    }
    TopAppBar()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 140.dp),
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        WaveAnimation(isRecording)
        NoiseMeter(noiseLevel = noiseLevel)
        AudioRecordAppUI(isRecording = isRecording,
            onStartClick = { startRecording() },
            onStopClick = { stopRecording() })

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            showBottomSheet = true
        }) {
            Text("Recording List")
        }
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showBottomSheet = false
                }, sheetState = sheetState, shape = RoundedCornerShape(
                    topStart = 20.dp, topEnd = 20.dp
                ), scrimColor = Color.Unspecified
            ) {
                AudioList(filesList, onPlayAudio, onDeleteAudio)
            }
        }
    }
}