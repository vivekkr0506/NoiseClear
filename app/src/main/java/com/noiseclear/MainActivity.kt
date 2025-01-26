package com.noiseclear

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
import com.noiseclear.composable.MainComponent
import com.noiseclear.recorder.AudioRecorder
import com.noiseclear.util.AudioViewModelFactory
import com.noiseclear.viewModel.AudioViewModel

@UnstableApi
class MainActivity : ComponentActivity() {

    private val recorder by lazy { AudioRecorder(applicationContext) }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (!isGranted) {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }

    private val requestPermissionLauncherNotification = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Notifications permission granted", Toast.LENGTH_SHORT)
                .show()
        } else {
            Toast.makeText(
                this, "${getString(R.string.app_name)} can't post notifications without Notification permission",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    private lateinit var audioViewModel: AudioViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Firebase.messaging.token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@OnCompleteListener
            }
            val token = task.result
            Log.d("FCM token:", token)
        })


       // askNotificationPermission()

        val viewModelFactory = AudioViewModelFactory(this)

        audioViewModel = ViewModelProvider(this, viewModelFactory).get(AudioViewModel::class.java)
        audioViewModel.checkPermission(this, requestPermissionLauncher)

        setContent {
            MainComponent(
                audioViewModel = audioViewModel,
                startRecording = { audioViewModel.startRecording(this, recorder) },
                stopRecording = { audioViewModel.stopRecording(recorder) },
                onPlayAudio = { audioViewModel.playAudio(this,it) },
                onPauseAudio = {audioViewModel.pauseAudio(this)},
                onDeleteAudio = { audioViewModel.deleteAudio(this,it) },
                onResumeAudio = {audioViewModel.resumeAudio(this,it!!)},
                onSaveRecording = { name, recordingFile ->
                    audioViewModel.saveRecording(this,
                        name,
                        recordingFile
                    )
                })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        recorder.stop()
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
//                Log.e(TAG, "PERMISSION_GRANTED")
                // FCM SDK (and your app) can post notifications.
            } else {
//                Log.e(TAG, "NO_PERMISSION")
                // Directly ask for the permission
                requestPermissionLauncherNotification.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

