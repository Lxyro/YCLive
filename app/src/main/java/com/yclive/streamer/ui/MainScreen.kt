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
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yclive.streamer.StreamConfig
import com.yclive.streamer.StreamOrientation
import com.yclive.streamer.ui.theme.AlertRed
import com.yclive.streamer.ui.theme.AlertRedLight
import com.yclive.streamer.ui.theme.AppleBlue
import com.yclive.streamer.ui.theme.CanvasBg
import com.yclive.streamer.ui.theme.CardBorder
import com.yclive.streamer.ui.theme.CardSurface
import com.yclive.streamer.ui.theme.DockIconActive
import com.yclive.streamer.ui.theme.DockIconInactive
import com.yclive.streamer.ui.theme.DockPillActive
import com.yclive.streamer.ui.theme.LiquidGlassBorder
import com.yclive.streamer.ui.theme.LiveGreen
import com.yclive.streamer.ui.theme.LiveGreenLight
import com.yclive.streamer.ui.theme.PillBgActive
import com.yclive.streamer.ui.theme.PillBgInactive
import com.yclive.streamer.ui.theme.PillTextActive
import com.yclive.streamer.ui.theme.PillTextInactive
import com.yclive.streamer.ui.theme.PureWhite
import com.yclive.streamer.ui.theme.StudioBlack
import com.yclive.streamer.ui.theme.StudioBorder
import com.yclive.streamer.ui.theme.StudioDarkGrey
import com.yclive.streamer.ui.theme.StudioLightGrey
import com.yclive.streamer.ui.theme.StudioMutedGrey
import com.yclive.streamer.ui.theme.TextDark
import com.yclive.streamer.ui.theme.TextSubtle
import com.yclive.streamer.ui.theme.TextTitle

@Composable
fun MainScreen(
    isStreaming: Boolean,
    clientCount: Int,
    localIp: String,
    config: StreamConfig,
    onConfigChange: (StreamConfig) -> Unit,
    onToggleStream: () -> Unit
) {
    val scrollState = rememberScrollState()
    var selectedDockTab by remember { mutableIntStateOf(0) } // 0: Stüdyo, 1: Ayarlar, 2: Sistem
    val streamUrl = "http://$localIp:8080"

    Scaffold(
        containerColor = CanvasBg,
        bottomBar = {
            // True Liquid Glass Floating Island Dock
            LiquidGlassFloatingIsland(
                selectedTab = selectedDockTab,
                onSelectTab = { selectedDockTab = it }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Pro Studio Square Header (Black & Light Grey Theme)
            StudioSquareHeader(isStreaming = isStreaming, fps = config.fps)

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Horizontal Filter Segmented Pills with Liquid Glass Physics
            StudioSegmentedFilterRow(
                selectedTab = selectedDockTab,
                onTabSelect = { selectedDockTab = it }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 3. Animated Tab Switcher
            AnimatedContent(
                targetState = selectedDockTab,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) { it } + fadeIn())
                            .togetherWith(slideOutHorizontally(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) { -it } + fadeOut())
                    } else {
                        (slideInHorizontally(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) { -it } + fadeIn())
                            .togetherWith(slideOutHorizontally(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) { it } + fadeOut())
                    }
                },
                label = "studioTabTransition"
            ) { tab ->
                when (tab) {
                    0 -> StudioLiveDashboardTab(
                        isStreaming = isStreaming,
                        clientCount = clientCount,
                        streamUrl = streamUrl,
                        config = config,
                        onToggleStream = onToggleStream
                    )
                    1 -> StudioSettingsTab(
                        config = config,
                        isStreaming = isStreaming,
                        onConfigChange = onConfigChange
                    )
                    else -> StudioSystemTab(
                        localIp = localIp,
                        streamUrl = streamUrl,
                        isStreaming = isStreaming,
                        config = config
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 1. PRO STUDIO SQUARE HEADER (Square Logo, Light Grey & Black Studio Theme)
// -------------------------------------------------------------------------------------------------
@Composable
private fun StudioSquareHeader(isStreaming: Boolean, fps: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "YC LIVE PRO STUDIO",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = TextSubtle
                )
                // Small Studio Tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(StudioLightGrey)
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "$fps FPS PRO",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioBlack
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Ekran Stüdyosu",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.6).sp,
                color = TextTitle
            )
        }

        // Right side: Square Studio Logo Badge & Live Indicator
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Live Status Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (isStreaming) LiveGreenLight else PillBgInactive)
                    .padding(horizontal = 11.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (isStreaming) LiveGreen else TextSubtle)
                    )
                    Text(
                        text = if (isStreaming) "CANLI" else "HAZIR",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isStreaming) LiveGreen else TextSubtle,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Square Studio Logo Mark (Light Grey Frame, Obsidian Black Core)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(StudioBlack)
                    .border(1.5.dp, StudioLightGrey, RoundedCornerShape(12.dp))
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Tv,
                    contentDescription = "Studio Square Logo",
                    tint = PureWhite,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 2. SEGMENTED FILTER PILLS (With Tactile Liquid Motion)
// -------------------------------------------------------------------------------------------------
@Composable
private fun StudioSegmentedFilterRow(
    selectedTab: Int,
    onTabSelect: (Int) -> Unit
) {
    val items = listOf("Yayın Masası", "Yatay / Dikey", "Performans & Ağ")
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEachIndexed { index, label ->
            val isSelected = selectedTab == index
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) StudioBlack else PillBgInactive,
                animationSpec = tween(200),
                label = "pillBg"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) PureWhite else PillTextInactive,
                animationSpec = tween(200),
                label = "pillText"
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(bgColor)
                    .border(
                        width = if (isSelected) 1.dp else 0.dp,
                        color = if (isSelected) StudioLightGrey.copy(alpha = 0.3f) else Color.Transparent,
                        shape = RoundedCornerShape(999.dp)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onTabSelect(index) }
                    .padding(horizontal = 18.dp, vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    letterSpacing = (-0.2).sp,
                    color = textColor
                )
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 3. TAB 1: STUDIO LIVE DASHBOARD (Box -> Network Address -> Start Button)
// -------------------------------------------------------------------------------------------------
@Composable
private fun StudioLiveDashboardTab(
    isStreaming: Boolean,
    clientCount: Int,
    streamUrl: String,
    config: StreamConfig,
    onToggleStream: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // [1] STREAM MONITOR BOX (Dark Studio Surface + Animated Audio Waves)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(26.dp),
                    spotColor = Color(0x18000000)
                ),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Monitor Display Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(StudioBlack),
                    contentAlignment = Alignment.Center
                ) {
                    if (isStreaming) {
                        RadarPulseBackground()

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Animated Live Equalizer Waveform Bars!
                            StreamingEqualizerBars()

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = "CANLI YAYIN AKTİF",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.6.sp,
                                color = PureWhite
                            )
                            Text(
                                text = "60 FPS Donanım CBR Akışı • Sıfır Gecikme",
                                fontSize = 12.sp,
                                color = StudioLightGrey
                            )
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(StudioDarkGrey),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tv,
                                    contentDescription = null,
                                    tint = StudioLightGrey,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Text(
                                text = "Stüdyo Aynası Hazır",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite
                            )
                            Text(
                                text = "Bilgisayar tarayıcısında izlemek için yayını başlatın",
                                fontSize = 12.sp,
                                color = StudioMutedGrey
                            )
                        }
                    }

                    // Top Left Pro Badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xCC202024))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (config.orientation == StreamOrientation.LANDSCAPE) "YATAY (16:9 / 20:9)" else "DİKEY (9:16)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioLightGrey
                        )
                    }

                    // Top Right Latency Indicator
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xCC202024))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "<25ms SIFIR GECİKME",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = LiveGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stats Matrix
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatPillCard(
                        modifier = Modifier.weight(1f),
                        title = "İzleyici",
                        value = "$clientCount",
                        sub = if (clientCount > 0) "Canlı Bağlantı" else "Bekleniyor",
                        accentColor = if (clientCount > 0) LiveGreen else TextDark
                    )
                    StatPillCard(
                        modifier = Modifier.weight(1f),
                        title = "FPS Kilidi",
                        value = "${config.fps} FPS",
                        sub = "Sabit Akış",
                        accentColor = LiveGreen
                    )
                    StatPillCard(
                        modifier = Modifier.weight(1f),
                        title = "Bitrate",
                        value = "${config.bitrateMbps} Mbps",
                        sub = "CBR Turbo",
                        accentColor = TextDark
                    )
                }
            }
        }

        // [2] DEDICATED LOCAL NETWORK ADDRESS CARD (Placed BETWEEN Box and Action Button)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(22.dp),
                    spotColor = Color(0x14000000)
                ),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, StudioBorder.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Wi-Fi / Cast Icon Box
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(StudioBlack),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tv,
                            contentDescription = null,
                            tint = PureWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "YAYIN ADRESİ (TARAYICIDAN GİRİN)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp,
                            color = TextSubtle
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = streamUrl,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = StudioBlack,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Quick Action Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StudioActionButton(
                        icon = Icons.Default.ContentCopy,
                        tooltip = "Kopyala",
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("YC Live Adresi", streamUrl))
                            Toast.makeText(context, "Yayın adresi kopyalandı!", Toast.LENGTH_SHORT).show()
                        }
                    )
                    StudioActionButton(
                        icon = Icons.Default.Share,
                        tooltip = "Paylaş",
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, streamUrl)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Yayın Adresini Paylaş"))
                        }
                    )
                }
            }
        }

        // [3] HERO ACTION BUTTON (High-Contrast, Pure White Text and Icon, Always 100% Readable)
        StudioHeroActionButton(
            isStreaming = isStreaming,
            onClick = onToggleStream
        )

        // [4] PRO STREAMER FEATURES BADGE ROW (Visual Polish & Brand Image)
        ProStreamerFeatureRow()
    }
}

// -------------------------------------------------------------------------------------------------
// 4. TAB 2: STUDIO SETTINGS (Orientation, Strict 60/90 FPS, Bitrate)
// -------------------------------------------------------------------------------------------------
@Composable
private fun StudioSettingsTab(
    config: StreamConfig,
    isStreaming: Boolean,
    onConfigChange: (StreamConfig) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Card 1: Orientation Presets (Yatay / Dikey / Otomatik)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 3.dp, shape = RoundedCornerShape(24.dp), spotColor = Color(0x12000000)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Ekran Yönü & En-Boy Oranı",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextTitle
                )
                Text(
                    text = "Yatay modda oyunun 20:9 / 16:9 geniş ekranı korunur, kareleşme olmaz",
                    fontSize = 12.sp,
                    color = TextSubtle
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OrientationSelectCard(
                        modifier = Modifier.weight(1f),
                        title = "Yatay Mod",
                        subtitle = "PUBG & Oyun",
                        icon = Icons.Default.Tv,
                        isSelected = config.orientation == StreamOrientation.LANDSCAPE,
                        enabled = !isStreaming,
                        onSelect = { onConfigChange(config.copy(orientation = StreamOrientation.LANDSCAPE)) }
                    )
                    OrientationSelectCard(
                        modifier = Modifier.weight(1f),
                        title = "Dikey Mod",
                        subtitle = "Telefon / Reels",
                        icon = Icons.Default.PhoneAndroid,
                        isSelected = config.orientation == StreamOrientation.PORTRAIT,
                        enabled = !isStreaming,
                        onSelect = { onConfigChange(config.copy(orientation = StreamOrientation.PORTRAIT)) }
                    )
                    OrientationSelectCard(
                        modifier = Modifier.weight(1f),
                        title = "Otomatik",
                        subtitle = "Cihaz Yönü",
                        icon = Icons.Default.Tv,
                        isSelected = config.orientation == StreamOrientation.AUTO,
                        enabled = !isStreaming,
                        onSelect = { onConfigChange(config.copy(orientation = StreamOrientation.AUTO)) }
                    )
                }
            }
        }

        // Card 2: Strict FPS Limiter (60 vs 90 FPS)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 3.dp, shape = RoundedCornerShape(24.dp), spotColor = Color(0x12000000)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Kare Hızı (FPS) Sabitleyici",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextTitle
                        )
                        Text(
                            text = "120Hz ekranlarda gereksiz kareler kısıtlanır, Wi-Fi gecikmesi 0ms kalır",
                            fontSize = 12.sp,
                            color = TextSubtle
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SegmentedChoicePill(
                        modifier = Modifier.weight(1f),
                        title = "60 FPS",
                        subtitle = "Standart",
                        isSelected = config.fps == StreamConfig.FPS_60,
                        enabled = !isStreaming,
                        onSelect = { onConfigChange(config.copy(fps = StreamConfig.FPS_60)) }
                    )
                    SegmentedChoicePill(
                        modifier = Modifier.weight(1f),
                        title = "90 FPS",
                        subtitle = "Akıcı",
                        isSelected = config.fps == StreamConfig.FPS_90,
                        enabled = !isStreaming,
                        onSelect = { onConfigChange(config.copy(fps = StreamConfig.FPS_90)) }
                    )
                    SegmentedChoicePill(
                        modifier = Modifier.weight(1f),
                        title = "120 FPS",
                        subtitle = "E-Spor Pro",
                        isSelected = config.fps == StreamConfig.FPS_120,
                        enabled = !isStreaming,
                        onSelect = { onConfigChange(config.copy(fps = StreamConfig.FPS_120)) }
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Donanımsal Bitrate (CBR & High Profile)",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextTitle
                )
                Text(
                    text = "Yüksek hareketli oyunlarda görüntü bozulmasını engelleyen sabit bant genişliği",
                    fontSize = 12.sp,
                    color = TextSubtle
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SegmentedChoicePill(
                        modifier = Modifier.weight(1f),
                        title = "25 Mbps",
                        subtitle = "Dengeli",
                        isSelected = config.bitrate == StreamConfig.BITRATE_25_MBPS,
                        enabled = !isStreaming,
                        onSelect = { onConfigChange(config.copy(bitrate = StreamConfig.BITRATE_25_MBPS)) }
                    )
                    SegmentedChoicePill(
                        modifier = Modifier.weight(1f),
                        title = "40 Mbps",
                        subtitle = "Pro Gaming",
                        isSelected = config.bitrate == StreamConfig.BITRATE_40_MBPS,
                        enabled = !isStreaming,
                        onSelect = { onConfigChange(config.copy(bitrate = StreamConfig.BITRATE_40_MBPS)) }
                    )
                    SegmentedChoicePill(
                        modifier = Modifier.weight(1f),
                        title = "60 Mbps",
                        subtitle = "Kristal 120Hz",
                        isSelected = config.bitrate == StreamConfig.BITRATE_60_MBPS,
                        enabled = !isStreaming,
                        onSelect = { onConfigChange(config.copy(bitrate = StreamConfig.BITRATE_60_MBPS)) }
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 5. TAB 3: STUDIO SYSTEM TAB (System Specs & Diagnostic)
// -------------------------------------------------------------------------------------------------
@Composable
private fun StudioSystemTab(
    localIp: String,
    streamUrl: String,
    isStreaming: Boolean,
    config: StreamConfig
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 3.dp, shape = RoundedCornerShape(24.dp), spotColor = Color(0x12000000)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Stüdyo Ağ & Donanım Özellikleri",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextTitle
            )

            InfoRowItem(label = "Yerel Ağ IP Adresi", value = localIp)
            InfoRowItem(label = "Ktor CIO Port", value = "8080")
            InfoRowItem(label = "WebSocket Akışı", value = "ws://$localIp:8080/ws")
            InfoRowItem(label = "Hedef Donanım FPS", value = "${config.fps} FPS Sabit")
            InfoRowItem(label = "Encoder Profili", value = "H.264 Baseline Level 4.2")
            InfoRowItem(label = "Gecikme Modu", value = "Turbo CBR Latency: 0")
            InfoRowItem(label = "Yayın Durumu", value = if (isStreaming) "Canlı Yayında" else "Boşta / Bekliyor")
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 6. HERO ACTION BUTTON (High-Contrast, Pure White Text and Icon, Crystal Clear Visibility)
// -------------------------------------------------------------------------------------------------
@Composable
private fun StudioHeroActionButton(
    isStreaming: Boolean,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "heroBtnScale"
    )

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .scale(scale)
            .border(
                width = 1.dp,
                color = if (isStreaming) Color(0x33FFFFFF) else StudioLightGrey.copy(alpha = 0.4f),
                shape = CircleShape
            )
            .shadow(
                elevation = 8.dp,
                shape = CircleShape,
                spotColor = if (isStreaming) Color(0x40E11D48) else Color(0x40121214)
            ),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isStreaming) AlertRed else StudioBlack,
            contentColor = PureWhite
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = if (isStreaming) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = null,
                tint = PureWhite, // 100% EXPLICIT PURE WHITE
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = if (isStreaming) "Yayını Sonlandır" else "Yayını Başlat",
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.2).sp,
                color = PureWhite // 100% EXPLICIT PURE WHITE - NEVER BLACK
            )
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 7. TRUE LIQUID GLASS FLOATING ISLAND DOCK BAR (Translucent, Glass Highlight, Spring Pill)
// -------------------------------------------------------------------------------------------------
@Composable
private fun LiquidGlassFloatingIsland(
    selectedTab: Int,
    onSelectTab: (Int) -> Unit
) {
    val items = listOf(
        Pair(Icons.Default.Tv, "Stüdyo"),
        Pair(Icons.Default.Settings, "Ayarlar"),
        Pair(Icons.Default.PhoneAndroid, "Sistem")
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 44.dp, end = 44.dp, bottom = 22.dp),
        contentAlignment = Alignment.Center
    ) {
        // Liquid Glass Capsule Dock
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xF2FFFFFF),
                            Color(0xE6F1F3F7)
                        )
                    )
                )
                .border(1.5.dp, Color(0x80FFFFFF), CircleShape)
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    spotColor = Color(0x20000000)
                )
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index, item ->
                    val isSelected = selectedTab == index
                    val animScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.0f else 0.94f,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label = "dockScale"
                    )

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .scale(animScale)
                            .clip(CircleShape)
                            .background(if (isSelected) StudioBlack else Color.Transparent)
                            .border(
                                width = if (isSelected) 1.dp else 0.dp,
                                color = if (isSelected) StudioLightGrey.copy(alpha = 0.3f) else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onSelectTab(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.first,
                            contentDescription = item.second,
                            tint = if (isSelected) PureWhite else TextSubtle,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// SUB-COMPONENTS, PRO BADGES & ANIMATIONS
// -------------------------------------------------------------------------------------------------
@Composable
private fun StudioActionButton(
    icon: ImageVector,
    tooltip: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(PillBgInactive)
            .border(1.dp, StudioBorder.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = tooltip,
            tint = StudioBlack,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun ProStreamerFeatureRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ProBadgeItem(modifier = Modifier.weight(1f), title = "Turbo CBR", subtitle = "Düşük Gecikme")
        ProBadgeItem(modifier = Modifier.weight(1f), title = "60 FPS Sabit", subtitle = "Sıfır Dalgalanma")
        ProBadgeItem(modifier = Modifier.weight(1f), title = "Oyun Modu", subtitle = "PUBG / 20:9 Uyumlu")
    }
}

@Composable
private fun ProBadgeItem(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(PillBgInactive.copy(alpha = 0.8f))
            .padding(vertical = 10.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = StudioBlack
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = TextSubtle
            )
        }
    }
}

@Composable
private fun StreamingEqualizerBars() {
    val transition = rememberInfiniteTransition(label = "equalizer")
    val h1 by transition.animateFloat(
        initialValue = 8f, targetValue = 28f,
        animationSpec = infiniteRepeatable(tween(350, easing = LinearEasing), RepeatMode.Reverse),
        label = "h1"
    )
    val h2 by transition.animateFloat(
        initialValue = 22f, targetValue = 10f,
        animationSpec = infiniteRepeatable(tween(280, easing = LinearEasing), RepeatMode.Reverse),
        label = "h2"
    )
    val h3 by transition.animateFloat(
        initialValue = 12f, targetValue = 32f,
        animationSpec = infiniteRepeatable(tween(420, easing = LinearEasing), RepeatMode.Reverse),
        label = "h3"
    )
    val h4 by transition.animateFloat(
        initialValue = 26f, targetValue = 8f,
        animationSpec = infiniteRepeatable(tween(310, easing = LinearEasing), RepeatMode.Reverse),
        label = "h4"
    )
    val h5 by transition.animateFloat(
        initialValue = 10f, targetValue = 24f,
        animationSpec = infiniteRepeatable(tween(390, easing = LinearEasing), RepeatMode.Reverse),
        label = "h5"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.height(34.dp)
    ) {
        listOf(h1, h2, h3, h4, h5).forEach { barH ->
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(barH.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(PureWhite)
            )
        }
    }
}

@Composable
private fun StatPillCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    sub: String,
    accentColor: Color
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(PillBgInactive)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = TextSubtle
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = sub,
                fontSize = 10.sp,
                color = TextSubtle
            )
        }
    }
}

@Composable
private fun OrientationSelectCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    enabled: Boolean,
    onSelect: () -> Unit
) {
    val borderColor = if (isSelected) StudioBlack else CardBorder
    val bgColor = if (isSelected) PillBgInactive else CardSurface

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(bgColor)
            .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(18.dp))
            .clickable(enabled = enabled, onClick = onSelect)
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) StudioBlack else TextSubtle,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) TextDark else TextSubtle
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = TextSubtle
            )
        }
    }
}

@Composable
private fun SegmentedChoicePill(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    isSelected: Boolean,
    enabled: Boolean,
    onSelect: () -> Unit
) {
    val bgColor = if (isSelected) StudioBlack else PillBgInactive
    val titleColor = if (isSelected) PureWhite else TextDark
    val subColor = if (isSelected) StudioLightGrey else TextSubtle

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .clickable(enabled = enabled, onClick = onSelect)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = titleColor
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = subColor
            )
        }
    }
}

@Composable
private fun InfoRowItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = TextSubtle
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun RadarPulseBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 180f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            color = LiveGreen.copy(alpha = pulseAlpha),
            radius = pulseRadius,
            center = center
        )
    }
}
