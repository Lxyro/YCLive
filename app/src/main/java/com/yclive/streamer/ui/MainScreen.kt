package com.yclive.streamer.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ScreenLockLandscape
import androidx.compose.material.icons.filled.ScreenLockPortrait
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yclive.streamer.StreamConfig
import com.yclive.streamer.StreamOrientation
import com.yclive.streamer.ui.theme.AmberGlow
import com.yclive.streamer.ui.theme.CrimsonRed
import com.yclive.streamer.ui.theme.ElectricViolet
import com.yclive.streamer.ui.theme.GlassBorder
import com.yclive.streamer.ui.theme.GlassBorderBright
import com.yclive.streamer.ui.theme.GlassCard
import com.yclive.streamer.ui.theme.GlassPill
import com.yclive.streamer.ui.theme.MidnightDark
import com.yclive.streamer.ui.theme.MidnightDeep
import com.yclive.streamer.ui.theme.NeonCyan
import com.yclive.streamer.ui.theme.NeonEmerald
import com.yclive.streamer.ui.theme.TextDim
import com.yclive.streamer.ui.theme.TextMuted
import com.yclive.streamer.ui.theme.TextPure

@Composable
fun MainScreen(
    isStreaming: Boolean,
    clientCount: Int,
    localIp: String,
    config: StreamConfig,
    onConfigChange: (StreamConfig) -> Unit,
    onToggleStream: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Yayın, 1: Ayarlar

    val streamUrl = "http://$localIp:8080"

    Scaffold(
        containerColor = MidnightDeep,
        bottomBar = {
            IosLiquidGlassBottomBar(
                selectedTab = selectedTab,
                onSelectTab = { selectedTab = it }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar: Streamlabs Style Midnight Brand Logo
            MidnightBrandHeader(isStreaming = isStreaming)

            Spacer(modifier = Modifier.height(20.dp))

            // Tab Content
            AnimatedVisibility(visible = selectedTab == 0, enter = fadeIn(), exit = fadeOut()) {
                StreamTabContent(
                    isStreaming = isStreaming,
                    clientCount = clientCount,
                    streamUrl = streamUrl,
                    config = config,
                    onToggleStream = onToggleStream,
                    onCopyUrl = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("YC Live URL", streamUrl))
                        Toast.makeText(context, "Yayın linki panoya kopyalandı!", Toast.LENGTH_SHORT).show()
                    },
                    onOpenUrl = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(streamUrl))
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    }
                )
            }

            AnimatedVisibility(visible = selectedTab == 1, enter = fadeIn(), exit = fadeOut()) {
                SettingsTabContent(
                    config = config,
                    isStreaming = isStreaming,
                    onConfigChange = onConfigChange
                )
            }
        }
    }
}

// -------------------------------------------------------------
// BRAND HEADER (Midnight Logo & Live Pulse)
// -------------------------------------------------------------
@Composable
private fun MidnightBrandHeader(isStreaming: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Geometric Midnight Shield Emblem
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF1E1B4B), Color(0xFF0F172A))
                        )
                    )
                    .border(
                        1.dp,
                        Brush.linearGradient(listOf(NeonCyan, ElectricViolet, NeonEmerald)),
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = "Logo",
                    tint = NeonCyan,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "YC LIVE",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPure,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Midnight Zero-Latency Mirror",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NeonCyan.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Live Badge Pill
        LiveStatusGlassPill(isStreaming = isStreaming)
    }
}

@Composable
private fun LiveStatusGlassPill(isStreaming: Boolean) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotPulse"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isStreaming) NeonEmerald.copy(alpha = 0.12f) else GlassPill,
        label = "bg"
    )
    val dotColor by animateColorAsState(
        targetValue = if (isStreaming) NeonEmerald else TextDim,
        label = "dot"
    )

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(bgColor)
            .border(
                1.dp,
                if (isStreaming) NeonEmerald.copy(alpha = 0.4f) else GlassBorder,
                RoundedCornerShape(24.dp)
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .scale(if (isStreaming) pulseScale else 1f)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (isStreaming) "CANLI (0ms)" else "BEKLEMEDE",
            color = if (isStreaming) NeonEmerald else TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.8.sp
        )
    }
}

// -------------------------------------------------------------
// TAB 1: YAYIN BÖLÜMÜ (CANLI GÖSTERGE & EKRAN MONİTÖRÜ)
// -------------------------------------------------------------
@Composable
private fun StreamTabContent(
    isStreaming: Boolean,
    clientCount: Int,
    streamUrl: String,
    config: StreamConfig,
    onToggleStream: () -> Unit,
    onCopyUrl: () -> Unit,
    onOpenUrl: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        // Screen Mirror Live Signal Monitor (Sol Üst Görsel Doğrulama)
        ScreenMirrorMonitorCard(
            isStreaming = isStreaming,
            config = config,
            clientCount = clientCount
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Liquid Glass URL Card
        LiquidGlassUrlCard(
            isStreaming = isStreaming,
            streamUrl = streamUrl,
            onCopyUrl = onCopyUrl,
            onOpenUrl = onOpenUrl
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Big iOS Liquid Glass Start/Stop Button
        LiquidGlassActionButton(
            isStreaming = isStreaming,
            onClick = onToggleStream
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Performance Stats
        PerformanceSummaryCard(config = config)
    }
}

@Composable
private fun ScreenMirrorMonitorCard(
    isStreaming: Boolean,
    config: StreamConfig,
    clientCount: Int
) {
    val transition = rememberInfiniteTransition(label = "radar")
    val waveOffset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .border(
                1.dp,
                if (isStreaming) GlassBorderBright else GlassBorder,
                RoundedCornerShape(22.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = GlassCard),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "Monitör",
                        tint = NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "EKRAN YANSITMA MONİTÖRÜ",
                        color = TextPure,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = if (isStreaming) "$clientCount İzleyici Aktif" else "Yayın Kapalı",
                    color = if (isStreaming) NeonEmerald else TextDim,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Canvas Waveform Monitor Visualizer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF070B14))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    if (isStreaming) {
                        // Dynamic streaming pulse lines
                        val lineCount = 18
                        val step = w / lineCount
                        for (i in 0 until lineCount) {
                            val x = i * step + (step / 2)
                            val factor = kotlin.math.sin((i + waveOffset * 6).toDouble()).toFloat()
                            val barHeight = (h * 0.4f) + (h * 0.35f * kotlin.math.abs(factor))
                            drawLine(
                                color = if (i % 2 == 0) NeonCyan else ElectricViolet,
                                start = Offset(x, (h - barHeight) / 2),
                                end = Offset(x, (h + barHeight) / 2),
                                strokeWidth = 3.dp.toPx()
                            )
                        }
                    } else {
                        // Static grid line
                        drawLine(
                            color = Color(0xFF1E293B),
                            start = Offset(0f, h / 2),
                            end = Offset(w, h / 2),
                            strokeWidth = 2f
                        )
                    }
                }

                // Overlay Status Badge
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xCC030712))
                        .border(1.dp, if (isStreaming) NeonCyan.copy(alpha = 0.5f) else Color(0xFF334155), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isStreaming) "DONANIM KODLAYICI: AKTİF (H.264)" else "YAYINI BAŞLATIN",
                        color = if (isStreaming) NeonCyan else TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Current Active Config Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ConfigBadgePill(
                    icon = if (config.orientation == StreamOrientation.LANDSCAPE) Icons.Default.ScreenLockLandscape else Icons.Default.ScreenLockPortrait,
                    text = if (config.orientation == StreamOrientation.LANDSCAPE) "Yatay (Oyun)" else "Dikey"
                )
                ConfigBadgePill(icon = Icons.Default.Tv, text = "${config.width}x${config.height}")
                ConfigBadgePill(icon = Icons.Default.Speed, text = "${config.fps} FPS")
                ConfigBadgePill(icon = Icons.Default.GraphicEq, text = "${config.bitrateMbps}M CBR")
            }
        }
    }
}

@Composable
private fun ConfigBadgePill(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0F172A))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(12.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text, color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun LiquidGlassUrlCard(
    isStreaming: Boolean,
    streamUrl: String,
    onCopyUrl: () -> Unit,
    onOpenUrl: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .border(1.dp, GlassBorder, RoundedCornerShape(22.dp)),
        colors = CardDefaults.cardColors(containerColor = GlassCard),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "TARAYICI CANLI YAYIN ADRESİ",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // URL Box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF080C16))
                    .border(1.dp, if (isStreaming) NeonCyan.copy(alpha = 0.4f) else Color(0xFF1E293B), RoundedCornerShape(14.dp))
                    .clickable { onCopyUrl() }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = streamUrl,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isStreaming) NeonCyan else TextMuted
                )

                Row {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Kopyala",
                        tint = NeonCyan,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { onCopyUrl() }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(
                        imageVector = Icons.Default.OpenInBrowser,
                        contentDescription = "Aç",
                        tint = ElectricViolet,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { onOpenUrl() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "PC tarayıcınızda veya OBS Studio Browser Source içine bu adresi girin.",
                color = TextDim,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun LiquidGlassActionButton(
    isStreaming: Boolean,
    onClick: () -> Unit
) {
    val gradient = if (isStreaming) {
        Brush.horizontalGradient(listOf(CrimsonRed, Color(0xFFB91C1C)))
    } else {
        Brush.horizontalGradient(listOf(NeonCyan, Color(0xFF0284C7), NeonEmerald))
    }

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(gradient)
            .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(18.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = if (isStreaming) Color.White else MidnightDeep
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isStreaming) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = if (isStreaming) "YAYINI SONLANDIR" else "CANLI YAYINI BAŞLAT",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
private fun PerformanceSummaryCard(config: StreamConfig) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, GlassBorder, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = GlassCard),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "⚡ SIFIR GECİKME (BUFFER = 0) PROTOKOLÜ",
                color = NeonCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "• WebCodecs & Hardware MediaCodec sıfır kopyalama modu aktiftir.\n" +
                       "• Ekran gecikmesi Wi-Fi 5 GHz ağında ~50-70ms seviyesindedir.\n" +
                       "• Yeni bağlanan izleyicilere anında IDR anahtar kare (Sync Frame) üretilir.",
                color = TextMuted,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}

// -------------------------------------------------------------
// TAB 2: AYARLAR BÖLÜMÜ (YATAY/DİKEY, ÇÖZÜNÜRLÜK, FPS, BITRATE)
// -------------------------------------------------------------
@Composable
private fun SettingsTabContent(
    config: StreamConfig,
    isStreaming: Boolean,
    onConfigChange: (StreamConfig) -> Unit
) {
    Column {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .border(1.dp, GlassBorder, RoundedCornerShape(22.dp)),
            colors = CardDefaults.cardColors(containerColor = GlassCard),
            shape = RoundedCornerShape(22.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = AmberGlow, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "YAYIN & KODLAMA AYARLARI", color = TextPure, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 1. EKRAN YÖNÜ: YATAY (Oyun) vs DİKEY (Shorts)
                Text(text = "Ekran Yönü (Orientation)", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    GlassChoiceChip(
                        title = "🎮 YATAY (Oyun)",
                        subtitle = "PUBG, CoD (16:9)",
                        selected = config.orientation == StreamOrientation.LANDSCAPE,
                        enabled = !isStreaming,
                        modifier = Modifier.weight(1f),
                        onClick = { onConfigChange(config.copy(orientation = StreamOrientation.LANDSCAPE)) }
                    )
                    GlassChoiceChip(
                        title = "📱 DİKEY (Telefon)",
                        subtitle = "TikTok, Shorts (9:16)",
                        selected = config.orientation == StreamOrientation.PORTRAIT,
                        enabled = !isStreaming,
                        modifier = Modifier.weight(1f),
                        onClick = { onConfigChange(config.copy(orientation = StreamOrientation.PORTRAIT)) }
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 2. ÇÖZÜNÜRLÜK: 1080p vs 720p
                Text(text = "Çözünürlük Kalitesi", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    GlassChoiceChip(
                        title = "1080p (Full HD)",
                        subtitle = if (config.orientation == StreamOrientation.LANDSCAPE) "1920x1080" else "1080x1920",
                        selected = config.baseResolution == StreamConfig.RES_1080P,
                        enabled = !isStreaming,
                        modifier = Modifier.weight(1f),
                        onClick = { onConfigChange(config.copy(baseResolution = StreamConfig.RES_1080P)) }
                    )
                    GlassChoiceChip(
                        title = "720p (HD)",
                        subtitle = if (config.orientation == StreamOrientation.LANDSCAPE) "1280x720" else "720x1280",
                        selected = config.baseResolution == StreamConfig.RES_720P,
                        enabled = !isStreaming,
                        modifier = Modifier.weight(1f),
                        onClick = { onConfigChange(config.copy(baseResolution = StreamConfig.RES_720P)) }
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 3. KARE HIZI (FPS)
                Text(text = "Kare Hızı (FPS)", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    GlassChoiceChip(
                        title = "60 FPS",
                        subtitle = "Akıcı & Kararlı",
                        selected = config.fps == StreamConfig.FPS_60,
                        enabled = !isStreaming,
                        modifier = Modifier.weight(1f),
                        onClick = { onConfigChange(config.copy(fps = StreamConfig.FPS_60)) }
                    )
                    GlassChoiceChip(
                        title = "90 FPS",
                        subtitle = "Ultra Rekabetçi",
                        selected = config.fps == StreamConfig.FPS_90,
                        enabled = !isStreaming,
                        modifier = Modifier.weight(1f),
                        onClick = { onConfigChange(config.copy(fps = StreamConfig.FPS_90)) }
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 4. BİTRATE (CBR)
                Text(text = "Bant Genişliği (CBR Bitrate)", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlassChoiceChip(
                        title = "15 Mbps",
                        subtitle = "Orta Wi-Fi",
                        selected = config.bitrate == StreamConfig.BITRATE_15_MBPS,
                        enabled = !isStreaming,
                        modifier = Modifier.weight(1f),
                        onClick = { onConfigChange(config.copy(bitrate = StreamConfig.BITRATE_15_MBPS)) }
                    )
                    GlassChoiceChip(
                        title = "25 Mbps",
                        subtitle = "5 GHz Wi-Fi",
                        selected = config.bitrate == StreamConfig.BITRATE_25_MBPS,
                        enabled = !isStreaming,
                        modifier = Modifier.weight(1f),
                        onClick = { onConfigChange(config.copy(bitrate = StreamConfig.BITRATE_25_MBPS)) }
                    )
                    GlassChoiceChip(
                        title = "35 Mbps",
                        subtitle = "Maksimum Sadakat",
                        selected = config.bitrate == StreamConfig.BITRATE_35_MBPS,
                        enabled = !isStreaming,
                        modifier = Modifier.weight(1f),
                        onClick = { onConfigChange(config.copy(bitrate = StreamConfig.BITRATE_35_MBPS)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun GlassChoiceChip(
    title: String,
    subtitle: String,
    selected: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bg = if (selected) NeonCyan.copy(alpha = 0.18f) else Color(0xFF090D18)
    val border = if (selected) NeonCyan else Color(0xFF1E293B)
    val titleColor = if (selected) NeonCyan else if (enabled) TextPure else TextDim

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(14.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, color = titleColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(3.dp))
            Text(text = subtitle, color = TextDim, fontSize = 10.sp)
        }
    }
}

// -------------------------------------------------------------
// iOS LIQUID GLASS BOTTOM NAVIGATION BAR
// -------------------------------------------------------------
@Composable
private fun IosLiquidGlassBottomBar(
    selectedTab: Int,
    onSelectTab: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(30.dp))
                .background(Color(0xD90B1120))
                .border(1.dp, GlassBorderBright, RoundedCornerShape(30.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LiquidGlassNavTab(
                icon = Icons.Default.Tv,
                label = "Yayın",
                selected = selectedTab == 0,
                onClick = { onSelectTab(0) }
            )
            LiquidGlassNavTab(
                icon = Icons.Default.Settings,
                label = "Ayarlar",
                selected = selectedTab == 1,
                onClick = { onSelectTab(1) }
            )
        }
    }
}

@Composable
private fun LiquidGlassNavTab(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (selected) NeonCyan.copy(alpha = 0.22f) else Color.Transparent
    val contentColor = if (selected) NeonCyan else TextDim

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(22.dp))
            .background(bg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = contentColor, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, color = contentColor, fontSize = 13.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
    }
}
