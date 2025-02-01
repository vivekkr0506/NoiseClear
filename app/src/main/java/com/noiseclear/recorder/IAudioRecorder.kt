package com.noiseclear.recorder

import java.io.File

interface IAudioRecorder {
    fun start(outputFile: File)
    fun stop()
}