package com.noiseclear

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import com.noiseclear.recorder.AudioRecordManager
import com.noiseclear.recorder.AudioRecorder
import com.noiseclear.viewModel.AudioViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class AudioViewModelTest {

//    @get:Rule
//    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var audioViewModel: AudioViewModel
    private lateinit var mockContext: Context
    private lateinit var mockAudioRecorder: AudioRecorder
    private lateinit var mockAudioRecordManager: AudioRecordManager

    @Before
    fun setUp() {
        mockContext = mockk()
        mockAudioRecorder = mockk(relaxed = true)
        mockAudioRecordManager = mockk(relaxed = true)

        every { mockContext.cacheDir } returns File("mock_cache_dir")
        every { mockContext.getFilesDir() } returns File("mock_files_dir")
        every { mockContext.filesDir } returns File("mock_files_dir")

        audioViewModel = AudioViewModel(mockContext)
    }

    @Test
    fun `checkPermission launches permission request if not granted`() {
        val mockLauncher = mockk<ActivityResultLauncher<String>>(relaxed = true)
        every {
            ContextCompat.checkSelfPermission(
                mockContext,
                android.Manifest.permission.RECORD_AUDIO
            )
        } returns PackageManager.PERMISSION_DENIED

        audioViewModel.checkPermission(mockContext, mockLauncher)

        verify { mockLauncher.launch(android.Manifest.permission.RECORD_AUDIO) }
    }

    @Test
    fun `checkPermission does nothing if permission is already granted`() {
        val mockLauncher = mockk<ActivityResultLauncher<String>>(relaxed = true)
        every {
            ContextCompat.checkSelfPermission(
                mockContext,
                android.Manifest.permission.RECORD_AUDIO
            )
        } returns PackageManager.PERMISSION_GRANTED

        audioViewModel.checkPermission(mockContext, mockLauncher)

        verify(exactly = 0) { mockLauncher.launch(any()) }
    }

//    @Test
//    fun `startRecording updates isRecording and audioFile states`() = runTest {
//        val testFile = File(mockContext.cacheDir, "audio_test.mp3")
//        every { mockAudioRecorder.start(any()) } answers {
//            firstArg<File>().writeText("test")
//        }
//
//        audioViewModel.startRecording(mockContext, mockAudioRecorder)
//
//        assertThat(audioViewModel.isRecording.value).isTrue()
//        assertThat(audioViewModel.audioFile.first().toString()).isEqualTo(testFile)
//    }
//
//    @Test
//    fun `stopRecording updates isRecording state`() = runTest {
//        audioViewModel.stopRecording(mockAudioRecorder)
//
//        verify { mockAudioRecorder.stop() }
//        assertThat(audioViewModel.isRecording.first()).isFalse()
//    }
//
//    @Test
//    fun `updateAudioFiles populates audioFiles list`() = runTest {
//        val mockFile = File(mockContext.cacheDir, "audio1.mp3")
//        mockFile.createNewFile()
//
//        audioViewModel.updateAudioFiles(mockContext)
//
//        val audioFiles = audioViewModel.audioFiles.first()
//        assertThat(audioFiles).contains(mockFile)
//
//        mockFile.delete()
//    }

//    @Test
//    fun `deleteAudio removes file and updates audioFiles`() = runTest {
//        val mockFile = File(mockContext.cacheDir, "audio_to_delete.mp3")
//        mockFile.createNewFile()
//
//        audioViewModel.deleteAudio(mockContext, mockFile)
//
//        assertThat(audioViewModel.audioFiles.first()).doesNotContain(mockFile)
//    }

//    @Test
//    fun `playAudio sets isPlaying to true`() = runTest {
//        val mockFile = File(mockContext.cacheDir, "audio_to_play.mp3")
//        audioViewModel.playAudio(mockContext, mockFile)
//
//        assertThat(audioViewModel.isPlaying.first()).isTrue()
//    }

//    @Test
//    fun `pauseAudio sets isPlaying to false`() = runTest {
//        audioViewModel.pauseAudio(mockContext)
//
//        assertThat(audioViewModel.isPlaying.first()).isFalse()
//    }

//    @Test
//    fun `saveRecording renames file and updates audioFiles`() = runTest {
//        val originalFile = File(mockContext.cacheDir, "audio_original.mp3")
//        originalFile.createNewFile()
//
//        val newName = "renamed_audio"
//        audioViewModel.saveRecording(mockContext, newName, originalFile)
//
//        val renamedFile = File(mockContext.cacheDir, "$newName.mp3")
//        assertThat(audioViewModel.audioFiles.first().toString()).contains(renamedFile)
//
//        renamedFile.delete()
//    }
}
