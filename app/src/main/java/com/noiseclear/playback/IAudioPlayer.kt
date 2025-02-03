package com.noiseclear.playback

import java.io.File

interface IAudioPlayer {
    fun playAudio(file: File,getDuration: (Float) -> Unit,getProgress: (Float) -> Unit)
    fun stop()
    fun pauseResumeAudio(isPlaying : Boolean,pausedLength : Int)
    fun getCurrentPosition(isPlaying : Boolean) : Int?
}