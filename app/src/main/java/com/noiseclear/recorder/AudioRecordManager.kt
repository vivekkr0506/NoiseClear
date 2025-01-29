package com.noiseclear.recorder

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.audiofx.NoiseSuppressor
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.log10

class AudioRecordManager {

    private val sampleRate = 44100
    private val bufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    private var audioRecord: AudioRecord? = null
    private var isRecording = false

    private var noiseSuppressor: NoiseSuppressor? = null

    @SuppressLint("MissingPermission")
    suspend fun startRecording(onNoiseLevelUpdate: (Double) -> Unit) {
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )
        enableNoiseCancellation()
        audioRecord?.startRecording()
        isRecording = true

        withContext(Dispatchers.IO) {
            val buffer = ShortArray(bufferSize)
            while (isRecording) {
                val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                if (read > 0) {
                    val rms = calculateRMS(buffer)
                    val db = 20 * log10(rms)
                    onNoiseLevelUpdate(db)
                }
            }
        }
    }
    private fun enableNoiseCancellation() {
        audioRecord?.audioSessionId?.let { sessionId ->
            if (NoiseSuppressor.isAvailable()) {
                noiseSuppressor = NoiseSuppressor.create(sessionId)
                Log.d("AudioRecordManager", "NoiseSuppressor enabled")
            } else {
                Log.d("AudioRecordManager", "NoiseSuppressor not supported on this device")
            }
        }
    }
    private fun calculateRMS(buffer: ShortArray): Double {
        var sum = 0.0
        for (sample in buffer) {
            sum += sample * sample
        }
        return Math.sqrt(sum / buffer.size)
    }

    fun stopRecording() {
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }
}
