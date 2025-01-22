package com.noiseclear.playback

import java.io.File

interface IAudioPlayer {
    fun playAudio(file: File)
    fun stop()
    fun pauseAudio()
}