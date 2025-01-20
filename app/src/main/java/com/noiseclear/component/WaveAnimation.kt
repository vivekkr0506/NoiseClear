package com.noiseclear.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

@Composable
fun WaveAnimation(isRecording : Boolean) {
    val urlComposition by rememberLottieComposition(spec = LottieCompositionSpec.Url("https://lottie.host/34933cfa-9e5b-45ab-9e8b-f8cedd9b79c3/fAnq5oXoa4.json"))
    val preloaderProgress by animateLottieCompositionAsState(
        urlComposition,
        iterations = LottieConstants.IterateForever,
        isPlaying = isRecording
    )
    LottieAnimation(
        composition = urlComposition,
        progress = if(!isRecording) 0f else preloaderProgress,
        modifier = Modifier.fillMaxWidth().height(100.dp),
        contentScale = ContentScale.FillBounds
    )
}