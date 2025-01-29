package com.noiseclear.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.io.File

@Composable
fun AudioList(
    filesList: List<File>,
    onPlayAudio: (File) -> Unit,
    onPauseAudio: () -> Unit,
    onDeleteAudio: (File) -> Unit,
    isPlaying: Boolean,
    currentPlayingFile: File?,
    onUpdatePlayingFile: (File?) -> Unit
) {
    if (filesList.isNotEmpty()) {
        LazyColumn(modifier = Modifier.fillMaxHeight(0.5f)) {
            items(filesList) { file ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = file.name, modifier = Modifier.weight(1f))

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
                                    onUpdatePlayingFile(null) // Reset the current playing file
                                }) {
                                    Icon(
                                        Icons.Rounded.Clear,
                                        contentDescription = "Pause Audio",
                                        tint = Color.Red
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
                                    Icon(
                                        Icons.Rounded.PlayArrow,
                                        contentDescription = "Play Audio",
                                        tint = Color.Green
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
                            IconButton(onClick = { onDeleteAudio(file) }) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "Delete Audio"
                                )
                            }
                        }
                    }
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
            Text(text = "No recording found!!")
        }
    }
}


