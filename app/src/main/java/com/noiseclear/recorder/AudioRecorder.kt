package com.noiseclear.recorder

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
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
    private val RECORDER_SAMPLE_RATE = 44100
    private val AUDIO_SOURCE = MediaRecorder.AudioSource.MIC
    private val RAW_AUDIO_SOURCE = MediaRecorder.AudioSource.UNPROCESSED // For raw audio
    private val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO
    private val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    private val BUFFER_SIZE_RECORDING = AudioRecord.getMinBufferSize(
        RECORDER_SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT
    )
    private fun createRecorder(): MediaRecorder {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else MediaRecorder()
    }
    private fun createAudioRecorder(): AudioRecord? {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_RECORD_AUDIO_PERMISSION
            )
            return null
        }

        return try {
            AudioRecord(
                AUDIO_SOURCE,
                RECORDER_SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                BUFFER_SIZE_RECORDING
            ).also {
                if (it.state != AudioRecord.STATE_INITIALIZED) {
                    throw IllegalStateException("AudioRecord initialization failed")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null // Return null if initialization fails
        }
    }



    companion object {
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
    }


    override fun start(outputFile: File) {
        createRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(FileOutputStream(outputFile).fd)

            prepare()
            start()

            recorder = this
        }
    }

    override fun startAudio() {
    }

    override fun stop() {
        recorder?.stop()
        recorder?.reset()
        recorder = null
    }

}