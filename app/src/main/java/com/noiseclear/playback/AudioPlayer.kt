package com.noiseclear.playback

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.core.net.toUri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

class AudioPlayer(
    private val context: Context
): IAudioPlayer {

    private var player: MediaPlayer? = null
    private var pausedLength = 0
    override fun playAudio(file: File,getDuration: (Float) -> Unit,getProgress :(Float) -> Unit) {
        if (!file.exists()) {
            Log.e("AudioPlayback", "File does not exist: ${file.absolutePath}")
            return
        }

        try {
            player?.release()
            player = MediaPlayer().apply {
                setDataSource(context,file.toUri())
                setOnPreparedListener {
                    getDuration(duration.toFloat() / 1000f)
                    start()
                    CoroutineScope(Dispatchers.Main).launch {
                        while (isPlaying) {
                            if (duration > 0) {
                                val progress = currentPosition.toFloat() / duration.toFloat()
                                if(progress == duration.toFloat()){
                                    stop()
                                }
                                getProgress(progress)
                            }
                            delay(500)
                        }
                    }
                }
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

    override fun pauseResumeAudio(isPlaying: Boolean, pausedLength: Int) {
        player?.let {
            if (isPlaying) {
                this.pausedLength = it.currentPosition
                it.pause()
            } else {
                it.seekTo(this.pausedLength)
                it.start()
            }
        }
    }

    override fun getCurrentPosition(isPlaying : Boolean) : Int? {
        if(isPlaying) return player?.currentPosition else return 0
    }
}