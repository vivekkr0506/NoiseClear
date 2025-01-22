package com.noiseclear.playback

import android.content.Context
import android.media.MediaPlayer
import androidx.core.net.toUri
import java.io.File

class AudioPlayer(
    private val context: Context
): IAudioPlayer {

    private var player: MediaPlayer? = null

    override fun playAudio(file: File) {
        MediaPlayer.create(context, file.toUri()).apply {
            player = this
            start()
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