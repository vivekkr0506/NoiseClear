package com.noiseclear.component

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
import androidx.compose.material.icons.filled.PlayArrow
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
fun AudioList(filesList: List<File>, onPlayAudio: (File) -> Unit, onDeleteAudio: (File) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxHeight(0.5f)) {
        items(filesList) { file ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = file.name)
                Box(
                    modifier = Modifier.background(
                        shape = CircleShape, color = Color.LightGray
                    )
                ) {
                    IconButton(onClick = { onPlayAudio(file) }) {
                        Icon(
                            Icons.Filled.PlayArrow,
                            contentDescription = "Play Audio",
                            tint = Color.Red
                        )
                    }
                }

                Box(
                    modifier = Modifier.background(
                        shape = CircleShape, color = Color.LightGray
                    )
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