package com.yclive.streamer.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tv
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yclive.streamer.StreamConfig
import com.yclive.streamer.ui.theme.BgDark
import com.yclive.streamer.ui.theme.CardBorder
import com.yclive.streamer.ui.theme.CardDark
import com.yclive.streamer.ui.theme.CyanAccent
import com.yclive.streamer.ui.theme.GreenAccent
import com.yclive.streamer.ui.theme.RedAccent
import com.yclive.streamer.ui.theme.SurfaceDark
import com.yclive.streamer.ui.theme.TextMuted
import com.yclive.streamer.ui.theme.TextPrimary
import com.yclive.streamer.ui.theme.TextSecondary
import com.yclive.streamer.ui.theme.YellowAccent

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

    val streamUrl = "http://$localIp:8080"

    Scaffold(
        containerColor = BgDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "YC LIVE",
                        style = MaterialTheme.typography.headlineLarge,
                        color = CyanAccent,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "Ultra Low-Latency Screen Streamer",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }

                // Status Badge
                StatusPill(isStreaming = isStreaming)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // URL & Broadcast Card
            BroadcastUrlCard(
                isStreaming = isStreaming,
                streamUrl = streamUrl,
                clientCount = clientCount,
                onCopyUrl = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("YC Live URL", streamUrl))
                    Toast.makeText(context, "Yayın URL'si kopyalandı!", Toast.LENGTH_SHORT).show()
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Settings Card (Disabled while streaming)
            StreamSettingsCard(
                config = config,
                isStreaming = isStreaming,
                onConfigChange = onConfigChange
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Big Start / Stop Action Button
            StartStopButton(
                isStreaming = isStreaming,
                onClick = onToggleStream
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Quick Tips & Performance Info
            InfoGuideCard()
        }
    }
}

@Composable
private fun StatusPill(isStreaming: Boolean) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isStreaming) GreenAccent.copy(alpha = 0.15f) else CardDark,
        label = "bgColor"
    )
    val dotColor by animateColorAsState(
        targetValue = if (isStreaming) GreenAccent else TextMuted,
        label = "dotColor"
    )
    val textColor by animateColorAsState(
        targetValue = if (isStreaming) GreenAccent else TextSecondary,
        label = "textColor"
    )

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(1.dp, dotColor.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .scale(if (isStreaming) scale else 1f)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (isStreaming) "CANLI YAYIN" else "BEKLEMEDE",
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun BroadcastUrlCard(
    isStreaming: Boolean,
    streamUrl: String,
    clientCount: Int,
    onCopyUrl: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (isStreaming) CyanAccent.copy(alpha = 0.5f) else CardBorder, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tv,
                        contentDescription = "Ekran Yayını",
                        tint = CyanAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tarayıcı Bağlantı Adresi",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                }

                if (isStreaming) {
                    Text(
                        text = "$clientCount İzleyici",
                        color = GreenAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // URL Box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(CardDark)
                    .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
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
                    color = if (isStreaming) CyanAccent else TextSecondary
                )
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "URL Kopyala",
                    tint = CyanAccent,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Bilgisayarında Chrome veya Edge açıp yukarıdaki adresi girmen yeterlidir.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun StreamSettingsCard(
    config: StreamConfig,
    isStreaming: Boolean,
    onConfigChange: (StreamConfig) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Yayın Ayarları",
                    tint = YellowAccent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Yayın Ayarları (MediaCodec)",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Resolution Presets
            Text(text = "Çözünürlük", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ChoiceChip(
                    text = "720p (HD)",
                    selected = config.width == StreamConfig.RESOLUTION_720P.first,
                    enabled = !isStreaming,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onConfigChange(
                            config.copy(
                                width = StreamConfig.RESOLUTION_720P.first,
                                height = StreamConfig.RESOLUTION_720P.second
                            )
                        )
                    }
                )
                ChoiceChip(
                    text = "1080p (FHD)",
                    selected = config.width == StreamConfig.RESOLUTION_1080P.first,
                    enabled = !isStreaming,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onConfigChange(
                            config.copy(
                                width = StreamConfig.RESOLUTION_1080P.first,
                                height = StreamConfig.RESOLUTION_1080P.second
                            )
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // FPS Presets
            Text(text = "Kare Hızı (FPS)", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ChoiceChip(
                    text = "60 FPS (Akıcı)",
                    selected = config.fps == StreamConfig.FPS_60,
                    enabled = !isStreaming,
                    modifier = Modifier.weight(1f),
                    onClick = { onConfigChange(config.copy(fps = StreamConfig.FPS_60)) }
                )
                ChoiceChip(
                    text = "90 FPS (Ultra)",
                    selected = config.fps == StreamConfig.FPS_90,
                    enabled = !isStreaming,
                    modifier = Modifier.weight(1f),
                    onClick = { onConfigChange(config.copy(fps = StreamConfig.FPS_90)) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bitrate Presets
            Text(text = "Bant Genişliği (CBR Bitrate)", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChoiceChip(
                    text = "15 Mbps",
                    selected = config.bitrate == StreamConfig.BITRATE_15_MBPS,
                    enabled = !isStreaming,
                    modifier = Modifier.weight(1f),
                    onClick = { onConfigChange(config.copy(bitrate = StreamConfig.BITRATE_15_MBPS)) }
                )
                ChoiceChip(
                    text = "25 Mbps",
                    selected = config.bitrate == StreamConfig.BITRATE_25_MBPS,
                    enabled = !isStreaming,
                    modifier = Modifier.weight(1f),
                    onClick = { onConfigChange(config.copy(bitrate = StreamConfig.BITRATE_25_MBPS)) }
                )
                ChoiceChip(
                    text = "35 Mbps",
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
private fun ChoiceChip(
    text: String,
    selected: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bg = if (selected) CyanAccent.copy(alpha = 0.2f) else CardDark
    val border = if (selected) CyanAccent else CardBorder
    val textClr = if (selected) CyanAccent else if (enabled) TextPrimary else TextMuted

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(10.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 11.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textClr,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun StartStopButton(
    isStreaming: Boolean,
    onClick: () -> Unit
) {
    val gradientBrush = if (isStreaming) {
        Brush.horizontalGradient(listOf(RedAccent, Color(0xFFD50000)))
    } else {
        Brush.horizontalGradient(listOf(CyanAccent, GreenAccent))
    }

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(gradientBrush),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = if (isStreaming) Color.White else BgDark
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isStreaming) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = if (isStreaming) "Yayını Durdur" else "Yayını Başlat",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = if (isStreaming) "YAYINI DURDUR" else "YAYINI BAŞLAT",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
private fun InfoGuideCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "⚡ PUBG & Rekabetçi Oyun Optimizasyonu",
                color = CyanAccent,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "• Gecikme 50-70ms seviyesindedir (WebCodecs sıfır tamponlama modu).\n" +
                       "• En yüksek performans için telefonun ve bilgisayarın 5 GHz Wi-Fi veya kablolu ağda olduğundan emin olun.\n" +
                       "• OBS Studio içine 'Browser Source' (Tarayıcı Kaynağı) olarak doğrudan URL eklenebilir.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                lineHeight = 20.sp
            )
        }
    }
}
