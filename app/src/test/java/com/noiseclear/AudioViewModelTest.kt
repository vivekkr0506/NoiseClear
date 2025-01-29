package com.noiseclear

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import com.noiseclear.recorder.AudioRecorder
import com.noiseclear.viewModel.AudioViewModel
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.File

@RunWith(JUnit4::class)
class AudioViewModelTest {

    private lateinit var audioViewModel: AudioViewModel
    private lateinit var mockContext: Context
    private lateinit var mockAudioRecorder: AudioRecorder

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockContext = mockk(relaxed = true)
        mockAudioRecorder = mockk(relaxed = true)

        every { mockContext.cacheDir } returns File("mock_cache_dir").apply { mkdirs() }

        audioViewModel = AudioViewModel(mockContext)
    }

    @Test
    fun `checkPermission launches permission request if not granted`() {
        val mockLauncher = mockk<ActivityResultLauncher<String>>(relaxed = true)
        every {
            ContextCompat.checkSelfPermission(mockContext, android.Manifest.permission.RECORD_AUDIO)
        } returns PackageManager.PERMISSION_DENIED

        audioViewModel.checkPermission(mockContext, mockLauncher)

        verify { mockLauncher.launch(android.Manifest.permission.RECORD_AUDIO) }
    }

    @Test
    fun `checkPermission does nothing if permission is already granted`() {
        val mockLauncher = mockk<ActivityResultLauncher<String>>(relaxed = true)
        every {
            ContextCompat.checkSelfPermission(mockContext, android.Manifest.permission.RECORD_AUDIO)
        } returns PackageManager.PERMISSION_GRANTED

        audioViewModel.checkPermission(mockContext, mockLauncher)

        verify(exactly = 0) { mockLauncher.launch(any()) }
    }

    @Test
    fun `startRecording updates isRecording and audioFile states`() = runTest {
        val testFile = File(mockContext.cacheDir, "audio_test.mp3")
        every { mockAudioRecorder.start(any()) } answers {
            firstArg<File>().writeText("test")
        }

        audioViewModel.startRecording(mockContext, mockAudioRecorder)

        assert(audioViewModel.isRecording.first())
        assert(audioViewModel.audioFile.first()?.name == "audio_test.mp3")
    }

    @Test
    fun `stopRecording updates isRecording state`() = runTest {
        audioViewModel.stopRecording(mockAudioRecorder)

        verify { mockAudioRecorder.stop() }
        assert(!audioViewModel.isRecording.first())
    }

    @Test
    fun `updateAudioFiles populates audioFiles list`() = runTest {
        val mockFile = File(mockContext.cacheDir, "audio1.mp3")
        mockFile.createNewFile()

        audioViewModel.updateAudioFiles(mockContext)

        val audioFiles = audioViewModel.audioFiles.first()
        assert(audioFiles.contains(mockFile))

        mockFile.delete()
    }

    @Test
    fun `deleteAudio removes file and updates audioFiles`() = runTest {
        val mockFile = File(mockContext.cacheDir, "audio_to_delete.mp3")
        mockFile.createNewFile()

        audioViewModel.updateAudioFiles(mockContext)
        audioViewModel.deleteAudio(mockContext, mockFile)

        val audioFiles = audioViewModel.audioFiles.first()
        assert(!audioFiles.contains(mockFile))
    }

    @Test
    fun `playAudio sets isPlaying to true`() = runTest {
        val mockFile = File(mockContext.cacheDir, "audio_to_play.mp3")
        mockFile.createNewFile()

        audioViewModel.playAudio(mockContext, mockFile)

        assert(audioViewModel.isPlaying.first())
    }

    @Test
    fun `pauseAudio sets isPlaying to false`() = runTest {
        audioViewModel.pauseAudio(mockContext)

        assert(!audioViewModel.isPlaying.first())
    }

    @Test
    fun `saveRecording renames file and updates audioFiles`() = runTest {
        val originalFile = File(mockContext.cacheDir, "audio_original.mp3")
        originalFile.createNewFile()

        val newName = "renamed_audio"
        audioViewModel.saveRecording(mockContext, newName, originalFile)

        val renamedFile = File(mockContext.cacheDir, "$newName.mp3")
        assert(audioViewModel.audioFiles.first().any { it.name == renamedFile.name })

        renamedFile.delete()
    }
}

