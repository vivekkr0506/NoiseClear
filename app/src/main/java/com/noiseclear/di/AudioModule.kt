package com.noiseclear.di


import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.noiseclear.recorder.AudioRecorder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(ViewModelComponent::class)
class AudioModule {

    @OptIn(UnstableApi::class)
    @Provides
    fun provideAudioRecorder(@ApplicationContext context: Context): AudioRecorder {
        return AudioRecorder(context)
    }
}