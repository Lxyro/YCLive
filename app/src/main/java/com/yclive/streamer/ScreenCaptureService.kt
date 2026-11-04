package com.yclive.streamer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import android.view.Surface

class ScreenCaptureService : Service() {

    private val tag = "ScreenCaptureService"
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var mediaCodec: MediaCodec? = null
    private var inputSurface: Surface? = null
    private var localStreamServer: LocalStreamServer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var drainJob: Job? = null

    private var config = StreamConfig()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: return START_NOT_STICKY

        when (action) {
            ACTION_START -> {
                val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 0)
                val resultData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(EXTRA_RESULT_DATA, Intent::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(EXTRA_RESULT_DATA)
                }
                val streamConfig = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getSerializableExtra(EXTRA_CONFIG, StreamConfig::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getSerializableExtra(EXTRA_CONFIG) as? StreamConfig
                } ?: StreamConfig()

                config = streamConfig
                _streamConfigFlow.value = config

                if (resultCode != 0 && resultData != null) {
                    startForegroundServiceWithNotification()
                    startStreaming(resultCode, resultData)
                } else {
                    Log.e(tag, "MediaProjection izni verileri eksik!")
                    stopSelf()
                }
            }
            ACTION_STOP -> {
                stopStreaming()
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    private fun startForegroundServiceWithNotification() {
        val stopIntent = Intent(this, ScreenCaptureService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val localIp = NetworkUtils.getLocalIpAddress()
        _currentIpFlow.value = localIp

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText("Yayın adresi: http://$localIp:8080")
            .setSmallIcon(R.drawable.ic_stream_live)
            .setContentIntent(openAppPendingIntent)
            .addAction(R.drawable.ic_stream_live, getString(R.string.action_stop), stopPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun startStreaming(resultCode: Int, resultData: Intent) {
        acquireWakeLock()

        // 1. Start Local Ktor CIO HTTP/WebSocket Server
        localStreamServer = LocalStreamServer(
            context = applicationContext,
            port = 8080,
            onRequestSyncFrame = { requestSyncFrame() }
        ).apply {
            start()
            // Observe client count
            serviceScope.launch {
                clientCount.collect { count ->
                    _clientCountFlow.value = count
                }
            }
        }

        // 2. Initialize MediaProjection
        val projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val mp = projectionManager.getMediaProjection(resultCode, resultData)
        mediaProjection = mp

        // Mandatory for Android 14+ (API 34)
        mp.registerCallback(object : MediaProjection.Callback() {
            override fun onStop() {
                Log.w(tag, "MediaProjection oturumu durduruldu.")
                stopStreaming()
                stopSelf()
            }
        }, Handler(Looper.getMainLooper()))

        // 3. Initialize Hardware Encoder & VirtualDisplay
        try {
            setupEncoderAndDisplay(mp)
            _isStreamingFlow.value = true
            Log.i(tag, "Ekran yakalama ve donanımsal kodlama başarıyla başlatıldı.")
        } catch (e: Exception) {
            Log.e(tag, "Donanımsal kodlayıcı başlatılırken hata oluştu", e)
            stopStreaming()
            stopSelf()
        }
    }

    private fun setupEncoderAndDisplay(mp: MediaProjection) {
        val format = MediaFormat.createVideoFormat(
            MediaFormat.MIMETYPE_VIDEO_AVC,
            config.width,
            config.height
        ).apply {
            setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            setInteger(MediaFormat.KEY_BIT_RATE, config.bitrate)
            setInteger(MediaFormat.KEY_FRAME_RATE, config.fps)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1) // 1 second keyframe interval

            // Constant Bitrate for smooth network stream
            setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR)

            // Ultra-low latency parameters
            setInteger(MediaFormat.KEY_LATENCY, 0)
            setInteger(MediaFormat.KEY_PRIORITY, 0) // Realtime

            // Baseline profile for widest compatibility and zero decoding delay
            setInteger(MediaFormat.KEY_PROFILE, MediaCodecInfo.CodecProfileLevel.AVCProfileBaseline)
            setInteger(MediaFormat.KEY_LEVEL, MediaCodecInfo.CodecProfileLevel.AVCLevel42)

            // Hardware low-latency extensions (Qualcomm Snapdragon, etc.)
            try {
                setInteger("vendor.qcom-ext-enc-low-latency.enable", 1)
            } catch (_: Exception) {}
        }

        val encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        val surface = encoder.createInputSurface()
        encoder.start()

        mediaCodec = encoder
        inputSurface = surface

        val metrics = resources.displayMetrics
        val densityDpi = metrics.densityDpi

        virtualDisplay = mp.createVirtualDisplay(
            "YCLiveVirtualDisplay",
            config.width,
            config.height,
            densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            surface,
            null,
            null
        )

        // Start reading encoded H.264 NAL units
        startEncoderDrainLoop(encoder)
    }

    private fun startEncoderDrainLoop(encoder: MediaCodec) {
        drainJob = serviceScope.launch(Dispatchers.IO) {
            val bufferInfo = MediaCodec.BufferInfo()
            Log.i(tag, "MediaCodec drain döngüsü aktif.")

            while (isActive) {
                try {
                    val outputBufferIndex = encoder.dequeueOutputBuffer(bufferInfo, 10_000)

                    when {
                        outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER -> {
                            // No output buffer ready yet
                        }
                        outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                            val newFormat = encoder.outputFormat
                            Log.i(tag, "MediaCodec format değişti: $newFormat")
                        }
                        outputBufferIndex >= 0 -> {
                            val outputBuffer = encoder.getOutputBuffer(outputBufferIndex)

                            if (outputBuffer != null && bufferInfo.size > 0) {
                                outputBuffer.position(bufferInfo.offset)
                                outputBuffer.limit(bufferInfo.offset + bufferInfo.size)

                                val frameData = ByteArray(bufferInfo.size)
                                outputBuffer.get(frameData)

                                // Check if this is Codec Config (SPS / PPS)
                                if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                                    Log.d(tag, "Codec Config (SPS/PPS) tespit edildi (${frameData.size} bayt)")
                                    localStreamServer?.cachedConfigBuffer = frameData
                                }

                                // Broadcast Annex-B packet over WebSocket
                                localStreamServer?.broadcastFrame(frameData)
                            }

                            encoder.releaseOutputBuffer(outputBufferIndex, false)
                        }
                    }
                } catch (e: Exception) {
                    if (isActive) {
                        Log.e(tag, "Drain döngüsü hatası: ${e.message}")
                    }
                    break
                }
            }
        }
    }

    /**
     * Dynamically request hardware encoder to emit an immediate IDR Keyframe.
     */
    private fun requestSyncFrame() {
        try {
            mediaCodec?.let { codec ->
                val params = Bundle().apply {
                    putInt(MediaCodec.PARAMETER_KEY_REQUEST_SYNC_FRAME, 0)
                }
                codec.setParameters(params)
                Log.d(tag, "Anlık IDR Keyframe istendi.")
            }
        } catch (e: Exception) {
            Log.w(tag, "Keyframe isteği iletilemedi: ${e.message}")
        }
    }

    private fun stopStreaming() {
        _isStreamingFlow.value = false

        drainJob?.cancel()
        drainJob = null

        try {
            virtualDisplay?.release()
            virtualDisplay = null
        } catch (e: Exception) {
            Log.w(tag, "VirtualDisplay serbest bırakılırken hata", e)
        }

        try {
            mediaCodec?.stop()
            mediaCodec?.release()
            mediaCodec = null
        } catch (e: Exception) {
            Log.w(tag, "MediaCodec durdurulurken hata", e)
        }

        try {
            inputSurface?.release()
            inputSurface = null
        } catch (e: Exception) {
            Log.w(tag, "InputSurface serbest bırakılırken hata", e)
        }

        try {
            mediaProjection?.stop()
            mediaProjection = null
        } catch (e: Exception) {
            Log.w(tag, "MediaProjection durdurulurken hata", e)
        }

        localStreamServer?.stop()
        localStreamServer = null

        releaseWakeLock()
        stopForeground(STOP_FOREGROUND_REMOVE)
        Log.i(tag, "Yayın tamamen durduruldu.")
    }

    private fun acquireWakeLock() {
        if (wakeLock == null) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "YCLive:ScreenStreamingLock").apply {
                acquire(4 * 60 * 60 * 1000L) // 4 hours maximum
            }
        }
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
            wakeLock = null
        } catch (_: Exception) {}
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.notification_channel_desc)
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        stopStreaming()
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        const val ACTION_START = "com.yclive.streamer.action.START"
        const val ACTION_STOP = "com.yclive.streamer.action.STOP"

        const val EXTRA_RESULT_CODE = "extra_result_code"
        const val EXTRA_RESULT_DATA = "extra_result_data"
        const val EXTRA_CONFIG = "extra_config"

        private const val CHANNEL_ID = "yc_live_stream_channel"
        private const val NOTIFICATION_ID = 1001

        // Reactive State for UI
        private val _isStreamingFlow = MutableStateFlow(false)
        val isStreamingFlow: StateFlow<Boolean> = _isStreamingFlow.asStateFlow()

        private val _clientCountFlow = MutableStateFlow(0)
        val clientCountFlow: StateFlow<Int> = _clientCountFlow.asStateFlow()

        private val _currentIpFlow = MutableStateFlow(NetworkUtils.getLocalIpAddress())
        val currentIpFlow: StateFlow<String> = _currentIpFlow.asStateFlow()

        private val _streamConfigFlow = MutableStateFlow(StreamConfig())
        val streamConfigFlow: StateFlow<StreamConfig> = _streamConfigFlow.asStateFlow()
    }
}
