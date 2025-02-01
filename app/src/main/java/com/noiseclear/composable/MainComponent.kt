package com.noiseclear.composable

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import com.noiseclear.R
import com.noiseclear.viewModel.AudioViewModel
import java.io.File

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainComponent(audioViewModel: AudioViewModel = hiltViewModel()) {
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    val noiseLevel by audioViewModel.noiseLevel.collectAsState()
    val isNoiseHigh by audioViewModel.isNoiseHigh.collectAsState()
    val isRecording by audioViewModel.isRecording.collectAsState()
    val fileList by audioViewModel.audioFiles.collectAsState(emptyList())
    val currentFile by audioViewModel.audioFile.collectAsState(null)
    var currentPlayingFile by remember { mutableStateOf<File?>(null) }
    val isPlaying by audioViewModel.isPlaying.collectAsState(false)

    LaunchedEffect(currentFile, isRecording) {
        if (currentFile != null && !isRecording) {
            showSaveDialog = true
        }
    }

    if (showSaveDialog && currentFile != null) {
        currentFile?.let {
            ConfirmationDialogue(recordingFile = it,
                onDismissRequest = {
                    (audioViewModel::deleteAudio)(it)
                    showSaveDialog = false
                },
                onSave = { name, recordingFile ->
                    audioViewModel::saveRecording.invoke(name, recordingFile)
                    showSaveDialog = false
                })
        }
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
        NoiseMeter(noiseLevel = noiseLevel, isNoiseHigh)

        MicAnimation(isRecording, audioViewModel::stopRecording,  audioViewModel::startRecording)

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            showBottomSheet = true
        }) {
            Text(stringResource(R.string.recording_list))
        }
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showBottomSheet = false
                },
                sheetState = sheetState,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                scrimColor = Color.Unspecified
            ) {
                AudioList(
                    filesList = fileList,
                    onPlayAudio = { file ->
                        currentPlayingFile = file
                        (audioViewModel::playAudio)(file)
                    },
                    onPauseAudio = {
                        currentPlayingFile = null
                        (audioViewModel::pauseAudio)()
                    },
                    onDeleteAudio = audioViewModel::deleteAudio,
                    isPlaying = isPlaying && currentPlayingFile != null,
                    currentPlayingFile = currentPlayingFile,
                    onUpdatePlayingFile = { file ->
                        currentPlayingFile = file
                    }
                )
            }
        }
    }
}
