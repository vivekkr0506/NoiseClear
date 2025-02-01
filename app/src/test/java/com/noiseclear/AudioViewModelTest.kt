package com.noiseclear

import android.content.Context

import com.noiseclear.playback.AudioPlayer
import com.noiseclear.recorder.AudioRecorder
import com.noiseclear.util.FILE_SIZE_LIMIT
import com.noiseclear.viewModel.AudioViewModel
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi

import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class AudioViewModelTest {

    @Mock
    lateinit var mockRecorder: AudioRecorder

    @Mock
    lateinit var mockAudioPlayer: AudioPlayer

    @Mock
    lateinit var mockContext: Context

    private lateinit var viewModel: AudioViewModel

    @Captor
    lateinit var captor: ArgumentCaptor<(Double) -> Unit>

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        viewModel = AudioViewModel(mockRecorder, mockContext)
    }

    @Test
    fun `test start recording`() = runBlockingTest {
        // Given
        val mockFile = File("mock_file")
        Mockito.`when`(mockContext.cacheDir).thenReturn(File("/mock/dir"))
        Mockito.`when`(mockRecorder.start(mockFile)).then {
            viewModel.startRecording()
        }

        // When
        viewModel.startRecording()

        // Then
        assertTrue(viewModel.isRecording.value)
    }

    @Test
    fun `test stop recording`() {
        // Given
        Mockito.`when`(mockRecorder.stop()).then {
            viewModel.stopRecording()
        }

        // When
        viewModel.stopRecording()

        // Then
        assertFalse(viewModel.isRecording.value)
    }

    @Test
    fun `test update noise level`() = runBlockingTest {
        // Given
        val noiseLevel = 60.0 // Simulating noise level above the threshold
        val mockFile = File("mock_audio_file")
        Mockito.`when`(mockContext.cacheDir).thenReturn(mockFile)

        // Simulating the noise level updating
        viewModel.startRecording()

        // When
        viewModel.recorder.getNoiseLabel { db ->
            assertEquals(db, noiseLevel)
        }

        // Then
        assertTrue(viewModel.isNoiseHigh.value)
    }

    @Test
    fun `test file size exceeds limit`() {
        // Given
        val largeFile = File(mockContext.cacheDir, "large_audio.mp3")
        Mockito.`when`(largeFile.length()).thenReturn((FILE_SIZE_LIMIT + 1).toLong())

        // When
        viewModel.startRecording()

        // Simulate a file size check
        val fileSize = largeFile.length()
        if (fileSize > FILE_SIZE_LIMIT) {
            viewModel.stopRecording()
            // Then
            assertFalse(viewModel.isRecording.value)
        }
    }

    @Test
    fun `test save recording`() {
        // Given
        val mockFile = File(mockContext.cacheDir, "audio.mp3")
        val newName = "new_audio_name"

        // When
        viewModel.saveRecording(newName, mockFile)

        // Then
        assertTrue(mockFile.exists())
    }

    @Test
    fun `test play audio`() {
        // Given
        val mockFile = File("mock_audio_file.mp3")
        Mockito.`when`(mockAudioPlayer.playAudio(mockFile)).then {
            viewModel.playAudio(mockFile)
        }

        // When
        viewModel.playAudio(mockFile)

        // Then
        assertTrue(viewModel.isPlaying.value)
    }

    @Test
    fun `test pause audio`() {
        // Given
        Mockito.`when`(mockAudioPlayer.pauseAudio()).then {
            viewModel.pauseAudio()
        }

        // When
        viewModel.pauseAudio()

        // Then
        assertFalse(viewModel.isPlaying.value)
    }

    @Test
    fun `test resume audio`() {
        // Given
        val mockFile = File("mock_audio_file.mp3")
        Mockito.`when`(mockAudioPlayer.playAudio(mockFile)).then {
            viewModel.resumeAudio(mockContext, mockFile)
        }

        // When
        viewModel.resumeAudio(mockContext, mockFile)

        // Then
        assertTrue(viewModel.isPlaying.value)
    }
}
