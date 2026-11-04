package com.yclive.streamer

import java.io.Serializable

enum class StreamOrientation : Serializable {
    LANDSCAPE, // Yatay - PUBG Mobile, CoD, emülatörler, rekabetçi oyunlar
    PORTRAIT   // Dikey - Genel telefon kullanımı, TikTok, Shorts vb.
}

data class StreamConfig(
    val baseResolution: Int = 1080, // 1080 veya 720
    val orientation: StreamOrientation = StreamOrientation.LANDSCAPE,
    val fps: Int = 60,
    val bitrate: Int = 25_000_000 // 25 Mbps
) : Serializable {

    val width: Int
        get() = if (orientation == StreamOrientation.LANDSCAPE) {
            if (baseResolution == 720) 1280 else 1920
        } else {
            if (baseResolution == 720) 720 else 1080
        }

    val height: Int
        get() = if (orientation == StreamOrientation.LANDSCAPE) {
            if (baseResolution == 720) 720 else 1080
        } else {
            if (baseResolution == 720) 1280 else 1920
        }

    val bitrateMbps: Int
        get() = bitrate / 1_000_000

    val resolutionLabel: String
        get() = "${width}x${height} (${if (orientation == StreamOrientation.LANDSCAPE) "Yatay" else "Dikey"})"

    companion object {
        const val RES_720P = 720
        const val RES_1080P = 1080

        const val FPS_60 = 60
        const val FPS_90 = 90

        const val BITRATE_15_MBPS = 15_000_000
        const val BITRATE_25_MBPS = 25_000_000
        const val BITRATE_35_MBPS = 35_000_000
    }
}
