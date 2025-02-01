package com.noiseclear.recorder

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.audiofx.NoiseSuppressor
import android.os.Build
import android.util.Log
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.log10

@UnstableApi
class AudioRecorder(
    private val context: Context
): IAudioRecorder {

    private var recorder: MediaRecorder? = null
    private val timeLimit = 60 * 1000 // 1 minute
    private val sampleRate = 44100
    private val bufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    private var audioRecord: AudioRecord? = null
    private var isRecording = false

    private var noiseSuppressor: NoiseSuppressor? = null
    private fun createRecorder(): MediaRecorder {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else MediaRecorder()
    }
    override fun start(outputFile: File) {
        createRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(FileOutputStream(outputFile).fd)
            setMaxDuration(timeLimit)

            prepare()
            start()

            recorder = this
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun getNoiseLabel(onNoiseLevelUpdate: (Double) -> Unit) {
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
    override fun stop() {
        isRecording = false
        audioRecord?.apply {
            stop()
            release()
        }
        audioRecord = null
        recorder?.stop()
        recorder?.reset()
        recorder = null
    }
}