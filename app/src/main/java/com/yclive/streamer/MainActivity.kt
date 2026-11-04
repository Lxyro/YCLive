package com.yclive.streamer

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.yclive.streamer.ui.MainScreen
import com.yclive.streamer.ui.theme.YCLiveTheme

class MainActivity : ComponentActivity() {

    private val projectionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            startStreamingService(result.resultCode, result.data!!)
        } else {
            Toast.makeText(this, "Ekran kaydı izni verilmedi.", Toast.LENGTH_SHORT).show()
        }
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(this, "Bildirim izni verilmedi; arka plan bildirimleri kısıtlanabilir.", Toast.LENGTH_SHORT).show()
        }
    }

    // Local mutable state for configuration when idle
    private var pendingConfig = StreamConfig()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkNotificationPermission()

        setContent {
            YCLiveTheme {
                val isStreaming by ScreenCaptureService.isStreamingFlow.collectAsState()
                val clientCount by ScreenCaptureService.clientCountFlow.collectAsState()
                val activeIp by ScreenCaptureService.currentIpFlow.collectAsState()
                var currentConfig by remember { mutableStateOf(pendingConfig) }

                MainScreen(
                    isStreaming = isStreaming,
                    clientCount = clientCount,
                    localIp = activeIp,
                    config = currentConfig,
                    onConfigChange = { newConfig ->
                        currentConfig = newConfig
                        pendingConfig = newConfig
                    },
                    onToggleStream = {
                        if (isStreaming) {
                            stopStreamingService()
                        } else {
                            requestScreenCapture()
                        }
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh IP address when returning to foreground
        val ip = NetworkUtils.getLocalIpAddress()
        // Set fallback if needed
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun requestScreenCapture() {
        val projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val captureIntent = projectionManager.createScreenCaptureIntent()
        projectionLauncher.launch(captureIntent)
    }

    private fun startStreamingService(resultCode: Int, data: Intent) {
        val serviceIntent = Intent(this, ScreenCaptureService::class.java).apply {
            action = ScreenCaptureService.ACTION_START
            putExtra(ScreenCaptureService.EXTRA_RESULT_CODE, resultCode)
            putExtra(ScreenCaptureService.EXTRA_RESULT_DATA, data)
            putExtra(ScreenCaptureService.EXTRA_CONFIG, pendingConfig)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    private fun stopStreamingService() {
        val serviceIntent = Intent(this, ScreenCaptureService::class.java).apply {
            action = ScreenCaptureService.ACTION_STOP
        }
        startService(serviceIntent)
    }
}
