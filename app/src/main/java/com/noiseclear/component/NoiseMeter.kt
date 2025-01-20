package com.noiseclear.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun NoiseMeter(noiseLevel: Double) {
    val maxNoiseLevel = 100f
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Noise Meter", style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(16.dp))

        NoiseMeterView(noiseLevel = noiseLevel, maxLevel = maxNoiseLevel)
    }
}

@Composable
fun NoiseMeterView(noiseLevel: Double, maxLevel: Float) {
    val normalizedLevel = (noiseLevel / maxLevel).coerceIn(0.0, 1.0)

    Canvas(modifier = Modifier.size(200.dp, 20.dp)) {
        drawRoundRect(
            color = Color.Gray.copy(alpha = 0.3f),
            size = size.copy(width = size.width, height = size.height),
            cornerRadius = CornerRadius.Zero
        )
        drawRoundRect(
            color = Color.Green.copy(alpha = 0.8f),
            size = size.copy(width = (size.width * normalizedLevel).toFloat(), height = size.height),
            cornerRadius = CornerRadius.Zero
        )
        drawRoundRect(
            color = Color.Black,
            size = size.copy(width = size.width, height = size.height),
            cornerRadius = CornerRadius.Zero,
            style = Stroke(.2.dp.toPx())
        )
    }
}
