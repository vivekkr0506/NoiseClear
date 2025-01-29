package com.noiseclear.recorder

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.audiofx.NoiseSuppressor
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.media3.common.util.UnstableApi
import java.io.File
import java.io.FileOutputStream

@UnstableApi
class AudioRecorder(
    private val context: Context
): IAudioRecorder {

    private var recorder: MediaRecorder? = null

    private var audioRecord: AudioRecord? = null
    private var noiseSuppressor: NoiseSuppressor? = null
    private var isRecording = false

    private val timeLimit = 60 * 1000 // 1 minute
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
            setAudioSamplingRate(44100)
            setAudioEncodingBitRate(128000)
            setAudioChannels(1)

            prepare()
            start()

            recorder = this
        }
    }


    override fun startAudio(outputFile: File) {
        val sampleRate = 44100
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize
        ).apply {
            if (NoiseSuppressor.isAvailable()) {
                noiseSuppressor = NoiseSuppressor.create(audioSessionId).apply {
                    if (this == null) {
                        Log.d("Start Audio","NoiseSuppressor creation failed")
                    } else {
                        Log.d("Start Audio","NoiseSuppressor enabled")
                    }
                }
            } else {
                Log.d("Start Audio","NoiseSuppressor is not supported on this device")
            }

            startRecording()
        }

        // Start a thread to save the recorded data
        isRecording = true
        val thread = Thread {
            val outputStream = FileOutputStream(outputFile)
            val buffer = ByteArray(bufferSize)

            while (isRecording) {
                val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                if (read > 0) {
                    outputStream.write(buffer, 0, read)
                }
            }

            outputStream.close()
        }
        thread.start()
    }
    override fun stopRecording() {
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