package com.yclive.streamer

import java.io.Serializable

data class StreamConfig(
    val width: Int = 1920,
    val height: Int = 1080,
    val fps: Int = 60,
    val bitrate: Int = 25_000_000 // 25 Mbps
) : Serializable {

    val bitrateMbps: Int
        get() = bitrate / 1_000_000

    val resolutionLabel: String
        get() = when {
            width >= 1920 || height >= 1920 -> "1080p (FHD)"
            width >= 1280 || height >= 1280 -> "720p (HD)"
            else -> "${width}x${height}"
        }

    companion object {
        val RESOLUTION_720P = Pair(1280, 720)
        val RESOLUTION_1080P = Pair(1920, 1080)

        val FPS_60 = 60
        val FPS_90 = 90

        val BITRATE_15_MBPS = 15_000_000
        val BITRATE_25_MBPS = 25_000_000
        val BITRATE_35_MBPS = 35_000_000
    }
}
