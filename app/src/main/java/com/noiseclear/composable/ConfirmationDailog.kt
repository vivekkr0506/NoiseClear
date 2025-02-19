package com.noiseclear.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.io.File


@Composable
fun ConfirmationDialogue(
    recordingFile: File,
    onDismissRequest: () -> Unit,
    onSave: (String,File) -> Unit
) {
    var audioName by remember { mutableStateOf("audio_${System.currentTimeMillis()}") }
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Rename Audio") },
        text = {
            Column {
                Text("Enter a name for your recording:")
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = audioName,
                    onValueChange = { audioName = it },
                    placeholder = { Text("Audio name") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (audioName.isNotBlank()) {
                        onSave(audioName,recordingFile)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ConfirmationDialogue1(
    onDismissRequest: () -> Unit
) {
    var audioName by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Rename Audio") },
        text = {
            Column {
                Text("Enter a name for your recording:")
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = audioName,
                    onValueChange = { audioName = it },
                    placeholder = { Text("Audio name") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}