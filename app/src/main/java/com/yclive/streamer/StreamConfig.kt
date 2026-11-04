package com.yclive.streamer

import java.io.Serializable

enum class StreamOrientation : Serializable {
    AUTO,      // Otomatik - Cihazın dikey/yatay modunu algılar (En akıcı ve doğru oran)
    LANDSCAPE, // Yatay - PUBG Mobile, CoD, rekabetçi oyunlar için 16:9 / 20:9
    PORTRAIT   // Dikey - TikTok, Reels, dikey uygulamalar için 9:16
}

data class StreamConfig(
    val baseResolution: Int = 1080, // 1080 veya 720
    val orientation: StreamOrientation = StreamOrientation.LANDSCAPE,
    val fps: Int = 120, // 60, 90 veya 120 FPS
    val bitrate: Int = 40_000_000 // 40 Mbps (Kristal netlik)
) : Serializable {

    val width: Int
        get() = when (orientation) {
            StreamOrientation.LANDSCAPE -> if (baseResolution == 720) 1280 else 1920
            StreamOrientation.PORTRAIT -> if (baseResolution == 720) 720 else 1080
            StreamOrientation.AUTO -> if (baseResolution == 720) 1280 else 1920
        }

    val height: Int
        get() = when (orientation) {
            StreamOrientation.LANDSCAPE -> if (baseResolution == 720) 720 else 1080
            StreamOrientation.PORTRAIT -> if (baseResolution == 720) 1280 else 1920
            StreamOrientation.AUTO -> if (baseResolution == 720) 720 else 1080
        }

    val bitrateMbps: Int
        get() = bitrate / 1_000_000

    val resolutionLabel: String
        get() = when (orientation) {
            StreamOrientation.LANDSCAPE -> "${width}x${height} (Yatay 16:9 / 20:9)"
            StreamOrientation.PORTRAIT -> "${width}x${height} (Dikey 9:16)"
            StreamOrientation.AUTO -> "${width}x${height} (Otomatik)"
        }

    companion object {
        const val RES_720P = 720
        const val RES_1080P = 1080

        const val FPS_60 = 60
        const val FPS_90 = 90
        const val FPS_120 = 120

        const val BITRATE_15_MBPS = 15_000_000
        const val BITRATE_25_MBPS = 25_000_000
        const val BITRATE_40_MBPS = 40_000_000
        const val BITRATE_60_MBPS = 60_000_000
        const val BITRATE_35_MBPS = 35_000_000
    }
}
