package com.noiseclear.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.noiseclear.ui.theme.Purple32

@Composable
fun MicAnimation(isRecording: Boolean, onStopClick: () -> Unit, onStartClick: () -> Unit) {
    val urlComposition by rememberLottieComposition(spec = LottieCompositionSpec.Url("https://lottie.host/f4ea85b1-07ab-4c82-aa64-46aca9509b07/CZCXtwOeIA.json"))
    val preloaderProgress by animateLottieCompositionAsState(
        urlComposition,
        iterations = LottieConstants.IterateForever,
        isPlaying = isRecording
    )
    LottieAnimation(
        composition = urlComposition,
        progress = if (!isRecording) 0f else preloaderProgress,
        modifier = Modifier
            .height(180.dp)
            .width(180.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(
                    bounded = false,
                    radius = 100.dp,
                    color = Purple32
                ),
            ) {
                if (isRecording) {
                    onStopClick()
                } else {
                    onStartClick()
                }
            },
        contentScale = ContentScale.FillBounds
    )
}