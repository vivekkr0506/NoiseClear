package com.noiseclear.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.noiseclear.R
import java.io.File
import java.util.Locale

@Composable
fun AudioList(
    filesList: List<File>,
    onPlayAudio: (File) -> Unit,
    onPauseAudio: () -> Unit,
    onDeleteAudio: (File) -> Unit,
    isPlaying: Boolean,
    currentPlayingFile: File?,
    onUpdatePlayingFile: (File?) -> Unit,
    audioProgress: Float,
    audioDuration: Float
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var fileToDelete by remember { mutableStateOf<File?>(null) }
    if (filesList.isNotEmpty()) {
        LazyColumn(modifier = Modifier.fillMaxHeight(0.5f).padding(start = 8.dp, end = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filesList) { file ->
                Box(
                    modifier = Modifier
                        .padding(10.dp) // Outer padding for the border space
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color.Red, Color.Yellow, Color.Green), // Gradient colors
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(2.dp) // Padding between the gradient and the column content
                ) {
                }
                Column(modifier = Modifier.border(2.dp, color = Color.DarkGray, shape = RoundedCornerShape(8.dp)).padding(10.dp)){
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = file.name, modifier = Modifier.weight(2f))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (currentPlayingFile?.absolutePath == file.absolutePath && isPlaying) {
                                // Pause button if this file is playing
                                Box(
                                    modifier = Modifier
                                        .background(shape = CircleShape, color = Color.LightGray)
                                        .padding(4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    IconButton(onClick = {
                                        onPauseAudio()
                                        // onUpdatePlayingFile(null) // Reset the current playing file
                                    }) {
                                        val icon : Painter = painterResource(id = R.drawable.pause)
                                        Icon(
                                            painter =  icon,
                                            contentDescription = stringResource(R.string.pause_audi),
                                            tint = Color.Black
                                        )
                                    }
                                }
                            } else {
                                // Play button for this file
                                Box(
                                    modifier = Modifier
                                        .background(shape = CircleShape, color = Color.LightGray)
                                        .padding(4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    IconButton(onClick = {
                                        onUpdatePlayingFile(file) // Update the current playing file
                                        onPlayAudio(file) // Play the selected file
                                    }) {
                                        val icon : Painter = painterResource(id = R.drawable.play)
                                        Icon(
                                            painter = icon,
                                            contentDescription = stringResource(R.string.play_audio),
                                            tint = Color.Black
                                        )
                                    }
                                }
                            }

                            // Delete button
                            Box(
                                modifier = Modifier
                                    .background(shape = CircleShape, color = Color.LightGray)
                                    .padding(4.dp)
                            ) {
                                IconButton(onClick = {
                                    fileToDelete = file
                                    showDeleteDialog = true
                                }) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = stringResource(R.string.delete_audio)
                                    )
                                }
                            }
                        }
                    }
                    if (currentPlayingFile?.absolutePath == file.absolutePath && isPlaying) {
                        Column(modifier = Modifier.padding(6.dp)) {
                            LinearProgressIndicator(
                                progress = { audioProgress },
                                modifier = Modifier.fillMaxWidth(),
                                color = Color.Green
                            )
                            Text(
                                text = String.format(Locale("en"), "%02d:%02d / %02d:%02d", (audioProgress * audioDuration).toInt() / 60, (audioProgress * audioDuration).toInt() % 60, audioDuration.toInt() / 60, audioDuration.toInt() % 60),
                                modifier = Modifier.align(Alignment.End)
                            )
                        }}
                }
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxHeight(0.5f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = stringResource(R.string.no_recording))
        }
    }
    DeleteConfirmationDialog(
        showDialog = showDeleteDialog,
        onDismiss = { showDeleteDialog = false },
        onConfirm = {
            fileToDelete?.let { onDeleteAudio(it) }
            fileToDelete = null
        },
        title = stringResource(R.string.delete_confirmation),
        message = stringResource(R.string.are_you_sure)
    )
}




