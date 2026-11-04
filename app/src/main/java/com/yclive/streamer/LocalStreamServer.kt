package com.yclive.streamer

import android.content.Context
import android.util.Log
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.InputStreamReader
import java.util.concurrent.ConcurrentHashMap

class LocalStreamServer(
    private val context: Context,
    val port: Int = 8080,
    private val onRequestSyncFrame: (() -> Unit)? = null
) {
    private val tag = "LocalStreamServer"
    private var engine: ApplicationEngine? = null
    private val serverScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Active WebSocket clients set
    private val connectedClients = ConcurrentHashMap.newKeySet<DefaultWebSocketSession>()

    // Observable client count for UI
    private val _clientCount = MutableStateFlow(0)
    val clientCount: StateFlow<Int> = _clientCount.asStateFlow()

    // Cached SPS/PPS config bytes to send immediately to new clients
    @Volatile
    var cachedConfigBuffer: ByteArray? = null

    // Cache index.html content
    private val htmlContent: String by lazy {
        loadHtmlPlayer()
    }

    fun start() {
        if (engine != null) return

        try {
            engine = embeddedServer(CIO, host = "0.0.0.0", port = port) {
                install(WebSockets) {
                    maxFrameSize = 10 * 1024 * 1024L // 10MB
                    masking = false
                }

                install(CORS) {
                    anyHost()
                }

                routing {
                    // Serve Zero-Latency WebCodecs HTML5 Player
                    get("/") {
                        call.respondText(htmlContent, ContentType.Text.Html)
                    }

                    // JSON Server Status
                    get("/status") {
                        val json = """
                            {
                                "status": "live",
                                "clients": ${_clientCount.value},
                                "port": $port
                            }
                        """.trimIndent()
                        call.respondText(json, ContentType.Application.Json)
                    }

                    // Ultra-Low Latency H.264 Binary WebSocket
                    webSocket("/ws") {
                        val session = this
                        connectedClients.add(session)
                        _clientCount.value = connectedClients.size
                        Log.i(tag, "Yeni izleyici bağlandı. Toplam izleyici: ${_clientCount.value}")

                        try {
                            // Immediately send cached SPS/PPS so browser decoder initializes instantly
                            cachedConfigBuffer?.let { configBytes ->
                                session.send(Frame.Binary(true, configBytes))
                                Log.d(tag, "Yeni istemciye SPS/PPS config gönderildi (${configBytes.size} bayt)")
                            }

                            // Request an immediate IDR Keyframe from hardware encoder
                            onRequestSyncFrame?.invoke()

                            // Keep WebSocket open while listening for incoming messages or close signals
                            for (frame in incoming) {
                                // Can receive control commands from client if needed (e.g. ping)
                            }
                        } catch (e: Exception) {
                            Log.w(tag, "WebSocket istemci oturumu sonlandı: ${e.message}")
                        } finally {
                            connectedClients.remove(session)
                            _clientCount.value = connectedClients.size
                            Log.i(tag, "İzleyici ayrıldı. Kalan: ${_clientCount.value}")
                        }
                    }
                }
            }.start(wait = false)

            Log.i(tag, "Ktor CIO HTTP & WebSocket sunucusu başlatıldı: port $port")
        } catch (e: Exception) {
            Log.e(tag, "Sunucu başlatılırken hata oluştu", e)
        }
    }

    /**
     * Broadcasts raw H.264 Annex-B NAL unit buffer to all connected WebSocket clients.
     */
    fun broadcastFrame(frameData: ByteArray) {
        if (connectedClients.isEmpty()) return

        val binaryFrame = Frame.Binary(true, frameData)
        val deadSessions = mutableListOf<DefaultWebSocketSession>()

        for (client in connectedClients) {
            serverScope.launch {
                try {
                    client.send(binaryFrame)
                } catch (e: Exception) {
                    deadSessions.add(client)
                }
            }
        }

        if (deadSessions.isNotEmpty()) {
            connectedClients.removeAll(deadSessions)
            _clientCount.value = connectedClients.size
        }
    }

    fun stop() {
        serverScope.launch {
            for (client in connectedClients) {
                try {
                    client.close()
                } catch (_: Exception) {}
            }
            connectedClients.clear()
            _clientCount.value = 0

            try {
                engine?.stop(1000, 2000)
                engine = null
                Log.i(tag, "Ktor sunucusu durduruldu.")
            } catch (e: Exception) {
                Log.e(tag, "Sunucu durdurulurken hata", e)
            }
        }
    }

    private fun loadHtmlPlayer(): String {
        return try {
            context.assets.open("index.html").use { input ->
                InputStreamReader(input, Charsets.UTF_8).readText()
            }
        } catch (e: Exception) {
            Log.w(tag, "index.html assetten okunamadı, dahili şablon kullanılıyor", e)
            FALLBACK_HTML
        }
    }

    companion object {
        private const val FALLBACK_HTML = """
<!DOCTYPE html>
<html>
<head><title>YC Live Fallback</title></head>
<body style="background:#000;color:#fff;text-align:center;padding:50px;">
  <h2>YC Live Streamer</h2>
  <p>Lütfen ana oynatıcıyı yenileyin veya assets dosyasını kontrol edin.</p>
</body>
</html>
        """
    }
}
