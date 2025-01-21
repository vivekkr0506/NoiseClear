package com.noiseclear.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun AudioRecordAppUI(
    isRecording: Boolean, onStartClick: () -> Unit, onStopClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
//            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier) {
//                Box(modifier = Modifier.background(shape = CircleShape, color = Color.LightGray)) {
//                    IconButton(onClick = { if(isRecording) onStopClick() }) {
//                        Icon(Icons.Filled.Clear, contentDescription = "Localized description")
//                    }
//                }
//                Text(text = "Cancel")
//            }
            MicAnimation(isRecording, onStopClick, onStartClick)
//            Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                Box(modifier = Modifier.background(shape = CircleShape, color = Color.LightGray)) {
//                    IconButton(onClick = { /* doSomething() */ }) {
//                        Icon(Icons.Filled.Check, contentDescription = "Localized description")
//                    }
//                }
//                Text(text = "Save")
//            }


        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    AudioRecordAppUI(
        isRecording = false,
        onStartClick = {},
        onStopClick = {}
    )
}