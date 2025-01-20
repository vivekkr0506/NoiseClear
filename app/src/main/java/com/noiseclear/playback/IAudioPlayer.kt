package com.noiseclear.playback

import java.io.File

interface IAudioPlayer {
    fun playFile(file: File)
    fun stop()
}