package com.noiseclear.playback

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.core.net.toUri
import java.io.File
import java.io.IOException

class AudioPlayer(
    private val context: Context
): IAudioPlayer {

    private var player: MediaPlayer? = null

    override fun playAudio(file: File) {
        if (!file.exists()) {
            Log.e("AudioPlayback", "File does not exist: ${file.absolutePath}")
            return
        }

        try {
            player?.release()
            player = MediaPlayer().apply {
                setDataSource(context,file.toUri())
                setOnPreparedListener { it.start() }
                setOnErrorListener { mp, what, extra ->
                    Log.e("AudioPlayback", "Error occurred: what=$what, extra=$extra")
                    false
                }
                prepareAsync()
            }
        } catch (e: IOException) {
            Log.e("AudioPlayback", "Error preparing audio: ${e.message}")
        }
    }



    override fun stop() {
        player?.stop()
        player?.release()
        player = null
    }

    override fun pauseAudio() {
        player?.pause()
    }
}