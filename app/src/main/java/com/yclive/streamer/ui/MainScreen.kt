package com.yclive.streamer.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yclive.streamer.StreamConfig
import com.yclive.streamer.StreamOrientation
import com.yclive.streamer.ui.theme.AlertRed
import com.yclive.streamer.ui.theme.DimGrey
import com.yclive.streamer.ui.theme.GlassBorder
import com.yclive.streamer.ui.theme.GlassBorderBright
import com.yclive.streamer.ui.theme.GlassCard
import com.yclive.streamer.ui.theme.GlassCardSelected
import com.yclive.streamer.ui.theme.GlassPill
import com.yclive.streamer.ui.theme.LiveGreen
import com.yclive.streamer.ui.theme.MutedSlate
import com.yclive.streamer.ui.theme.ObsidianBlack
import com.yclive.streamer.ui.theme.PureWhite

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
        containerColor = ObsidianBlack,
        bottomBar = {
            AnimatedMonochromeBottomBar(
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
                .padding(horizontal = 22.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Minimalist Black & White Brand Header
            MonochromeBrandHeader(isStreaming = isStreaming)

            Spacer(modifier = Modifier.height(24.dp))

            // Smooth Animated Tab Transition
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) { it } + fadeIn())
                            .togetherWith(slideOutHorizontally(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) { -it } + fadeOut())
                    } else {
                        (slideInHorizontally(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) { -it } + fadeIn())
                            .togetherWith(slideOutHorizontally(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) { it } + fadeOut())
                    }
                },
                label = "tabTransition"
            ) { tab ->
                if (tab == 0) {
                    StreamTabContent(
                        isStreaming = isStreaming,
                        clientCount = clientCount,
                        streamUrl = streamUrl,
                        config = config,
                        onToggleStream = onToggleStream,
                        onCopyUrl = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("YC Live URL", streamUrl))
                            Toast.makeText(context, "Yayın linki kopyalandı", Toast.LENGTH_SHORT).show()
                        },
                        onOpenUrl = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(streamUrl))
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        }
                    )
                } else {
                    SettingsTabContent(
                        config = config,
                        isStreaming = isStreaming,
                        onConfigChange = onConfigChange
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// MINIMALIST MONOCHROME BRAND HEADER
// -------------------------------------------------------------
@Composable
private fun MonochromeBrandHeader(isStreaming: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Elegant Frosted Glass Brand Icon
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(GlassPill)
                    .border(1.dp, GlassBorderBright, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Tv,
                    contentDescription = "Logo",
                    tint = PureWhite,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = "YC LIVE",
                    color = PureWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Monochrome Glass Mirror",
                    color = MutedSlate,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }

        // Minimalist Live Pill
        LiveStatusIndicatorPill(isStreaming = isStreaming)
    }
}

@Composable
private fun LiveStatusIndicatorPill(isStreaming: Boolean) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isStreaming) LiveGreen.copy(alpha = 0.12f) else GlassPill,
        label = "pillBg"
    )

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(9999.dp))
            .background(bgColor)
            .border(
                1.dp,
                if (isStreaming) LiveGreen.copy(alpha = 0.4f) else GlassBorder,
                RoundedCornerShape(9999.dp)
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .scale(if (isStreaming) pulseScale else 1f)
                .clip(CircleShape)
                .background(if (isStreaming) LiveGreen else DimGrey)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (isStreaming) "CANLI (0ms)" else "BEKLEMEDE",
            color = if (isStreaming) LiveGreen else MutedSlate,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

// -------------------------------------------------------------
// TAB 1: YAYIN BÖLÜMÜ
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

        // Screen Mirror Monitor Card
        ScreenMonitorGlassCard(
            isStreaming = isStreaming,
            config = config,
            clientCount = clientCount
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Liquid Glass URL Card
        StreamUrlGlassCard(
            isStreaming = isStreaming,
            streamUrl = streamUrl,
            onCopyUrl = onCopyUrl,
            onOpenUrl = onOpenUrl
        )

        Spacer(modifier = Modifier.height(26.dp))

        // Minimalist Action Button
        StreamActionButton(
            isStreaming = isStreaming,
            onClick = onToggleStream
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Low-latency Notice
        MonochromeNoticeCard()
    }
}

@Composable
private fun ScreenMonitorGlassCard(
    isStreaming: Boolean,
    config: StreamConfig,
    clientCount: Int
) {
    val transition = rememberInfiniteTransition(label = "wave")
    val waveOffset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveOffset"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .border(1.dp, if (isStreaming) GlassBorderBright else GlassBorder, RoundedCornerShape(26.dp)),
        colors = CardDefaults.cardColors(containerColor = GlassCard),
        shape = RoundedCornerShape(26.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "EKRAN YANSITMA MONİTÖRÜ",
                    color = MutedSlate,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Text(
                    text = if (isStreaming) "$clientCount İzleyici Aktif" else "Yayın Kapalı",
                    color = if (isStreaming) LiveGreen else DimGrey,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Waveform Monitor
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF070709))
                    .border(1.dp, GlassBorder, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    if (isStreaming) {
                        val bars = 20
                        val step = w / bars
                        for (i in 0 until bars) {
                            val x = i * step + (step / 2)
                            val factor = kotlin.math.sin((i + waveOffset * 6.28).toDouble()).toFloat()
                            val barH = (h * 0.35f) + (h * 0.45f * kotlin.math.abs(factor))
                            drawLine(
                                color = PureWhite.copy(alpha = 0.85f),
                                start = Offset(x, (h - barH) / 2),
                                end = Offset(x, (h + barH) / 2),
                                strokeWidth = 2.5.dp.toPx()
                            )
                        }
                    } else {
                        drawLine(
                            color = GlassBorder,
                            start = Offset(0f, h / 2),
                            end = Offset(w, h / 2),
                            strokeWidth = 2f
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9999.dp))
                        .background(Color(0xE60A0A0C))
                        .border(1.dp, GlassBorder, RoundedCornerShape(9999.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isStreaming) "DONANIM KODLAYICI AKTİF (H.264)" else "YAYINI BAŞLATIN",
                        color = if (isStreaming) PureWhite else MutedSlate,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Config Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MonochromeBadge(text = if (config.orientation == StreamOrientation.LANDSCAPE) "Yatay (Oyun)" else "Dikey")
                MonochromeBadge(text = "${config.width}x${config.height}")
                MonochromeBadge(text = "${config.fps} FPS")
                MonochromeBadge(text = "${config.bitrateMbps} Mbps CBR")
            }
        }
    }
}

@Composable
private fun MonochromeBadge(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x1AFFFFFF))
            .border(1.dp, GlassBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(text = text, color = PureWhite, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun StreamUrlGlassCard(
    isStreaming: Boolean,
    streamUrl: String,
    onCopyUrl: () -> Unit,
    onOpenUrl: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .border(1.dp, GlassBorder, RoundedCornerShape(26.dp)),
        colors = CardDefaults.cardColors(containerColor = GlassCard),
        shape = RoundedCornerShape(26.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "TARAYICI CANLI YAYIN ADRESİ",
                color = MutedSlate,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF09090C))
                    .border(1.dp, if (isStreaming) GlassBorderBright else GlassBorder, RoundedCornerShape(16.dp))
                    .clickable { onCopyUrl() }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = streamUrl,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isStreaming) PureWhite else MutedSlate
                )

                Row {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Kopyala",
                        tint = PureWhite,
                        modifier = Modifier
                            .size(19.dp)
                            .clickable { onCopyUrl() }
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Aç",
                        tint = MutedSlate,
                        modifier = Modifier
                            .size(19.dp)
                            .clickable { onOpenUrl() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Bilgisayar tarayıcısında bu adresi açarak sıfır gecikmeyle izleyin.",
                color = DimGrey,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun StreamActionButton(
    isStreaming: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isStreaming) AlertRed else PureWhite,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "btnColor"
    )

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(9999.dp))
            .background(bgColor)
            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(9999.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = if (isStreaming) PureWhite else ObsidianBlack
        ),
        shape = RoundedCornerShape(9999.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isStreaming) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = if (isStreaming) "YAYINI SONLANDIR" else "CANLI YAYINI BAŞLAT",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
        }
    }
}

@Composable
private fun MonochromeNoticeCard() {
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
                text = "⚡ SIFIR GECİKME (BUFFER = 0) DONANIM PROTOKOLÜ",
                color = PureWhite,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "• H.264 Baseline MediaCodec donanım kodlayıcı devrededir.\n" +
                       "• Wi-Fi 5 GHz bağlantısında gecikme 50-70ms seviyesindedir.\n" +
                       "• OBS Browser Source içine tam ekran olarak doğrudan eklenebilir.",
                color = MutedSlate,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}

// -------------------------------------------------------------
// TAB 2: AYARLAR BÖLÜMÜ
// -------------------------------------------------------------
@Composable
private fun SettingsTabContent(
    config: StreamConfig,
    isStreaming: Boolean,
    onConfigChange: (StreamConfig) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .border(1.dp, GlassBorder, RoundedCornerShape(26.dp)),
        colors = CardDefaults.cardColors(containerColor = GlassCard),
        shape = RoundedCornerShape(26.dp)
    ) {
        Column(modifier = Modifier.padding(22.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = PureWhite, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = "YAYIN VE KODLAMA AYARLARI", color = PureWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 1. Ekran Yönü
            Text(text = "Ekran Yönü (Orientation)", color = MutedSlate, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MonochromeOptionChip(
                    title = "🎮 YATAY (Oyun)",
                    subtitle = "PUBG, CoD (16:9)",
                    selected = config.orientation == StreamOrientation.LANDSCAPE,
                    enabled = !isStreaming,
                    modifier = Modifier.weight(1f),
                    onClick = { onConfigChange(config.copy(orientation = StreamOrientation.LANDSCAPE)) }
                )
                MonochromeOptionChip(
                    title = "📱 DİKEY (Telefon)",
                    subtitle = "Shorts, Reels (9:16)",
                    selected = config.orientation == StreamOrientation.PORTRAIT,
                    enabled = !isStreaming,
                    modifier = Modifier.weight(1f),
                    onClick = { onConfigChange(config.copy(orientation = StreamOrientation.PORTRAIT)) }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2. Çözünürlük
            Text(text = "Çözünürlük Kalitesi", color = MutedSlate, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MonochromeOptionChip(
                    title = "1080p (Full HD)",
                    subtitle = if (config.orientation == StreamOrientation.LANDSCAPE) "1920x1080" else "1080x1920",
                    selected = config.baseResolution == StreamConfig.RES_1080P,
                    enabled = !isStreaming,
                    modifier = Modifier.weight(1f),
                    onClick = { onConfigChange(config.copy(baseResolution = StreamConfig.RES_1080P)) }
                )
                MonochromeOptionChip(
                    title = "720p (HD)",
                    subtitle = if (config.orientation == StreamOrientation.LANDSCAPE) "1280x720" else "720x1280",
                    selected = config.baseResolution == StreamConfig.RES_720P,
                    enabled = !isStreaming,
                    modifier = Modifier.weight(1f),
                    onClick = { onConfigChange(config.copy(baseResolution = StreamConfig.RES_720P)) }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3. FPS
            Text(text = "Kare Hızı (FPS)", color = MutedSlate, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MonochromeOptionChip(
                    title = "60 FPS",
                    subtitle = "Akıcı & Kararlı",
                    selected = config.fps == StreamConfig.FPS_60,
                    enabled = !isStreaming,
                    modifier = Modifier.weight(1f),
                    onClick = { onConfigChange(config.copy(fps = StreamConfig.FPS_60)) }
                )
                MonochromeOptionChip(
                    title = "90 FPS",
                    subtitle = "Ultra Rekabetçi",
                    selected = config.fps == StreamConfig.FPS_90,
                    enabled = !isStreaming,
                    modifier = Modifier.weight(1f),
                    onClick = { onConfigChange(config.copy(fps = StreamConfig.FPS_90)) }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 4. Bitrate
            Text(text = "Bant Genişliği (CBR Bitrate)", color = MutedSlate, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MonochromeOptionChip(
                    title = "15 Mbps",
                    subtitle = "Standart Wi-Fi",
                    selected = config.bitrate == StreamConfig.BITRATE_15_MBPS,
                    enabled = !isStreaming,
                    modifier = Modifier.weight(1f),
                    onClick = { onConfigChange(config.copy(bitrate = StreamConfig.BITRATE_15_MBPS)) }
                )
                MonochromeOptionChip(
                    title = "25 Mbps",
                    subtitle = "5 GHz Önerilen",
                    selected = config.bitrate == StreamConfig.BITRATE_25_MBPS,
                    enabled = !isStreaming,
                    modifier = Modifier.weight(1f),
                    onClick = { onConfigChange(config.copy(bitrate = StreamConfig.BITRATE_25_MBPS)) }
                )
                MonochromeOptionChip(
                    title = "35 Mbps",
                    subtitle = "Ultra Sadakat",
                    selected = config.bitrate == StreamConfig.BITRATE_35_MBPS,
                    enabled = !isStreaming,
                    modifier = Modifier.weight(1f),
                    onClick = { onConfigChange(config.copy(bitrate = StreamConfig.BITRATE_35_MBPS)) }
                )
            }
        }
    }
}

@Composable
private fun MonochromeOptionChip(
    title: String,
    subtitle: String,
    selected: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bg = if (selected) PureWhite else Color(0x0FFFFFFF)
    val textColor = if (selected) ObsidianBlack else PureWhite
    val subColor = if (selected) Color(0xCC000000) else DimGrey

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .border(1.dp, if (selected) PureWhite else GlassBorder, RoundedCornerShape(16.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(3.dp))
            Text(text = subtitle, color = subColor, fontSize = 10.sp)
        }
    }
}

// -------------------------------------------------------------
// ANIMATED LIQUID GLASS FLOATING BOTTOM BAR
// -------------------------------------------------------------
@Composable
private fun AnimatedMonochromeBottomBar(
    selectedTab: Int,
    onSelectTab: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp, vertical = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        // Floating Frosted Glass Capsule Dock
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(9999.dp))
                .background(Color(0xD9121216))
                .border(1.dp, GlassBorderBright, RoundedCornerShape(9999.dp))
                .padding(4.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            // Smooth Animated Sliding Indicator Pill
            val indicatorOffset by animateDpAsState(
                targetValue = if (selectedTab == 0) 0.dp else 108.dp,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "pillSlide"
            )

            // Sliding active highlight pill
            Box(
                modifier = Modifier
                    .offset { IntOffset(indicatorOffset.roundToPx(), 0) }
                    .width(108.dp)
                    .height(42.dp)
                    .clip(RoundedCornerShape(9999.dp))
                    .background(PureWhite)
            )

            // Tab Items Row
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomBarItem(
                    icon = Icons.Default.Tv,
                    label = "Yayın",
                    selected = selectedTab == 0,
                    onClick = { onSelectTab(0) }
                )
                BottomBarItem(
                    icon = Icons.Default.Settings,
                    label = "Ayarlar",
                    selected = selectedTab == 1,
                    onClick = { onSelectTab(1) }
                )
            }
        }
    }
}

@Composable
private fun BottomBarItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val contentColor by animateColorAsState(
        targetValue = if (selected) ObsidianBlack else MutedSlate,
        animationSpec = tween(200),
        label = "itemColor"
    )

    Row(
        modifier = Modifier
            .width(108.dp)
            .height(42.dp)
            .clip(RoundedCornerShape(9999.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = contentColor, modifier = Modifier.size(17.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, color = contentColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
