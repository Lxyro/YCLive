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
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
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
import com.yclive.streamer.ui.theme.DockBg
import com.yclive.streamer.ui.theme.DockIconActive
import com.yclive.streamer.ui.theme.DockIconInactive
import com.yclive.streamer.ui.theme.DockPillActive
import com.yclive.streamer.ui.theme.LiveGreen
import com.yclive.streamer.ui.theme.LiveGreenLight
import com.yclive.streamer.ui.theme.PillBgActive
import com.yclive.streamer.ui.theme.PillBgInactive
import com.yclive.streamer.ui.theme.PillTextActive
import com.yclive.streamer.ui.theme.PillTextInactive
import com.yclive.streamer.ui.theme.PureWhite
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
    var selectedDockTab by remember { mutableIntStateOf(0) } // 0: Yayın, 1: Ayarlar, 2: Cihaz
    val streamUrl = "http://$localIp:8080"

    Scaffold(
        containerColor = CanvasBg,
        bottomBar = {
            FloatingAppleDockBar(
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
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Apple-Style Header (Matching Reference: "Good Morning! ☀️ / Hello, Drescher")
            AppleHeaderSection(isStreaming = isStreaming)

            Spacer(modifier = Modifier.height(18.dp))

            // 2. Segmented Horizontal Filter Pills (Matching Reference: [All] [Casual] [Jackets] [Shoes])
            IOSSegmentedFilterRow(
                selectedTab = selectedDockTab,
                onTabSelect = { selectedDockTab = it }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Tab Content with Spring Slide Transition
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
                label = "appleTabTransition"
            ) { tab ->
                when (tab) {
                    0 -> AppleLiveDashboardTab(
                        isStreaming = isStreaming,
                        clientCount = clientCount,
                        streamUrl = streamUrl,
                        config = config,
                        onToggleStream = onToggleStream
                    )
                    1 -> AppleSettingsTab(
                        config = config,
                        isStreaming = isStreaming,
                        onConfigChange = onConfigChange
                    )
                    else -> AppleDeviceTab(
                        localIp = localIp,
                        streamUrl = streamUrl,
                        isStreaming = isStreaming
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 1. APPLE TOP HEADER (Matching the Reference Screenshot)
// -------------------------------------------------------------------------------------------------
@Composable
private fun AppleHeaderSection(isStreaming: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "YC LIVE STREAMER ☀️",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = TextSubtle
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Ekran Yayını",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp,
                color = TextTitle
            )
        }

        // Right side: Status indicator + Minimalist Brand Emblem
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Live Status Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (isStreaming) LiveGreenLight else PillBgInactive)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
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

            // Minimalist Luxury Monochrome Emblem (NO neon)
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(DockBg)
                    .shadow(elevation = 3.dp, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Tv,
                    contentDescription = "Brand Emblem",
                    tint = PureWhite,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 2. SEGMENTED FILTER PILLS (Matching the Reference: [All] [Casual] [Jackets] [Shoes])
// -------------------------------------------------------------------------------------------------
@Composable
private fun IOSSegmentedFilterRow(
    selectedTab: Int,
    onTabSelect: (Int) -> Unit
) {
    val items = listOf("Yayın Masası", "Yatay / Dikey", "Cihaz & Ağ")
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items.forEachIndexed { index, label ->
            val isSelected = selectedTab == index
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) PillBgActive else PillBgInactive,
                animationSpec = tween(220),
                label = "pillBg"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) PillTextActive else PillTextInactive,
                animationSpec = tween(220),
                label = "pillText"
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(bgColor)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onTabSelect(index) }
                    .padding(horizontal = 20.dp, vertical = 10.dp),
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
// 3. TAB 1: APPLE LIVE DASHBOARD
// -------------------------------------------------------------------------------------------------
@Composable
private fun AppleLiveDashboardTab(
    isStreaming: Boolean,
    clientCount: Int,
    streamUrl: String,
    config: StreamConfig,
    onToggleStream: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Visual Display Card (With Frosted Glass Overlay Badge like reference image)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(26.dp),
                    spotColor = Color(0x12000000)
                ),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Monitor Mockup Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF141417)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isStreaming) {
                        // Realtime Pulse Radar
                        RadarPulseBackground()

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "CANLI YAYIN AKTİF",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                color = PureWhite
                            )
                            Text(
                                text = "Sıfır gecikmeli H.264 donanım akışı yayında",
                                fontSize = 12.sp,
                                color = Color(0xFF9CA3AF)
                            )
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tv,
                                contentDescription = null,
                                tint = Color(0xFF71717A),
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "Ekran Aynalama Hazır",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PureWhite
                            )
                            Text(
                                text = "Tarayıcıda izlemek için yayını başlatın",
                                fontSize = 12.sp,
                                color = Color(0xFF71717A)
                            )
                        }
                    }

                    // Frosted Glass Bottom Overlay (Matching the "Urbanco Casual Shirt / $35" overlay in reference)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(12.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xB3222226))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isStreaming) "İzleyici Bağlantı Adresi" else "Yerel Ağ Adresi",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFA1A1AA)
                                )
                                Text(
                                    text = streamUrl,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PureWhite,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                SmallActionButton(
                                    icon = Icons.Default.ContentCopy,
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("YC Live Adresi", streamUrl))
                                        Toast.makeText(context, "Bağlantı kopyalandı!", Toast.LENGTH_SHORT).show()
                                    }
                                )
                                SmallActionButton(
                                    icon = Icons.Default.Share,
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
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Realtime Stats Matrix
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                        title = "Gecikme",
                        value = "<40ms",
                        sub = "Sıfır Gecikme",
                        accentColor = LiveGreen
                    )
                    StatPillCard(
                        modifier = Modifier.weight(1f),
                        title = "Çözünürlük",
                        value = "${config.baseResolution}p",
                        sub = if (config.orientation == StreamOrientation.LANDSCAPE) "Yatay" else "Dikey",
                        accentColor = TextDark
                    )
                }
            }
        }

        // 4. Hero Capsule Action Button (Matching Reference "Ready? Let's Go!")
        AppleHeroActionButton(
            isStreaming = isStreaming,
            onClick = onToggleStream
        )
    }
}

// -------------------------------------------------------------------------------------------------
// 4. TAB 2: APPLE SETTINGS TAB (Orientation Yatay/Dikey, FPS, Bitrate)
// -------------------------------------------------------------------------------------------------
@Composable
private fun AppleSettingsTab(
    config: StreamConfig,
    isStreaming: Boolean,
    onConfigChange: (StreamConfig) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Card 1: Orientation (Yatay / Dikey)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 3.dp, shape = RoundedCornerShape(26.dp), spotColor = Color(0x12000000)),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Ekran Yönü",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextTitle
                )
                Text(
                    text = "Oyunlar için Yatay, sosyal medya ve telefon ekranı için Dikey seçin",
                    fontSize = 12.sp,
                    color = TextSubtle
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OrientationSelectCard(
                        modifier = Modifier.weight(1f),
                        title = "Yatay Mod",
                        subtitle = "PUBG & Oyun",
                        icon = Icons.Default.Tv,
                        isSelected = config.orientation == StreamOrientation.LANDSCAPE,
                        enabled = !isStreaming,
                        onSelect = {
                            onConfigChange(config.copy(orientation = StreamOrientation.LANDSCAPE))
                        }
                    )
                    OrientationSelectCard(
                        modifier = Modifier.weight(1f),
                        title = "Dikey Mod",
                        subtitle = "Telefon / Reels",
                        icon = Icons.Default.PhoneAndroid,
                        isSelected = config.orientation == StreamOrientation.PORTRAIT,
                        enabled = !isStreaming,
                        onSelect = {
                            onConfigChange(config.copy(orientation = StreamOrientation.PORTRAIT))
                        }
                    )
                }
            }
        }

        // Card 2: Quality & FPS
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 3.dp, shape = RoundedCornerShape(26.dp), spotColor = Color(0x12000000)),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Kare Hızı (FPS)",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextTitle
                )
                Text(
                    text = "Yüksek yenileme hızında rekabetçi tepki süresi",
                    fontSize = 12.sp,
                    color = TextSubtle
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SegmentedChoicePill(
                        modifier = Modifier.weight(1f),
                        title = "60 FPS",
                        subtitle = "Standart Akıcı",
                        isSelected = config.fps == StreamConfig.FPS_60,
                        enabled = !isStreaming,
                        onSelect = { onConfigChange(config.copy(fps = StreamConfig.FPS_60)) }
                    )
                    SegmentedChoicePill(
                        modifier = Modifier.weight(1f),
                        title = "90 FPS",
                        subtitle = "Ultra Hızlı",
                        isSelected = config.fps == StreamConfig.FPS_90,
                        enabled = !isStreaming,
                        onSelect = { onConfigChange(config.copy(fps = StreamConfig.FPS_90)) }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Bitrate & Netlik",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextTitle
                )
                Text(
                    text = "Yerel Wi-Fi band genişliğinize göre seçin",
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
                        title = "15 Mbps",
                        subtitle = "Hafif",
                        isSelected = config.bitrate == StreamConfig.BITRATE_15_MBPS,
                        enabled = !isStreaming,
                        onSelect = { onConfigChange(config.copy(bitrate = StreamConfig.BITRATE_15_MBPS)) }
                    )
                    SegmentedChoicePill(
                        modifier = Modifier.weight(1f),
                        title = "25 Mbps",
                        subtitle = "Önerilen",
                        isSelected = config.bitrate == StreamConfig.BITRATE_25_MBPS,
                        enabled = !isStreaming,
                        onSelect = { onConfigChange(config.copy(bitrate = StreamConfig.BITRATE_25_MBPS)) }
                    )
                    SegmentedChoicePill(
                        modifier = Modifier.weight(1f),
                        title = "35 Mbps",
                        subtitle = "Ultra FHD",
                        isSelected = config.bitrate == StreamConfig.BITRATE_35_MBPS,
                        enabled = !isStreaming,
                        onSelect = { onConfigChange(config.copy(bitrate = StreamConfig.BITRATE_35_MBPS)) }
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 5. TAB 3: APPLE DEVICE TAB (Local Network & Status)
// -------------------------------------------------------------------------------------------------
@Composable
private fun AppleDeviceTab(
    localIp: String,
    streamUrl: String,
    isStreaming: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 3.dp, shape = RoundedCornerShape(26.dp), spotColor = Color(0x12000000)),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Ağ ve Bağlantı Bilgisi",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextTitle
            )

            InfoRowItem(label = "Cihaz Yerel IP", value = localIp)
            InfoRowItem(label = "HTTP Sunucu Portu", value = "8080")
            InfoRowItem(label = "WebSocket Protokolü", value = "ws://$localIp:8080/ws")
            InfoRowItem(label = "Video Kodlayıcı", value = "Donanımsal MediaCodec H.264")
            InfoRowItem(label = "Yayın Durumu", value = if (isStreaming) "Aktif Yayında" else "Boşta")
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 6. HERO CAPSULE ACTION BUTTON (Matching Reference: "Ready? Let's Go!")
// -------------------------------------------------------------------------------------------------
@Composable
private fun AppleHeroActionButton(
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
            .shadow(
                elevation = 6.dp,
                shape = CircleShape,
                spotColor = if (isStreaming) Color(0x33E11D48) else Color(0x3318181B)
            ),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isStreaming) AlertRed else DockBg,
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
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = if (isStreaming) "Yayını Sonlandır" else "Yayını Başlat",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.2).sp
            )
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 7. FLOATING APPLE DOCK BAR (Direct match to Screen 2 & 3 in reference image)
// -------------------------------------------------------------------------------------------------
@Composable
private fun FloatingAppleDockBar(
    selectedTab: Int,
    onSelectTab: (Int) -> Unit
) {
    val items = listOf(
        Pair(Icons.Default.Tv, "Yayın"),
        Pair(Icons.Default.Settings, "Ayarlar"),
        Pair(Icons.Default.PhoneAndroid, "Cihaz")
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp, bottom = 22.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(DockBg)
                .shadow(elevation = 10.dp, shape = CircleShape)
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index, item ->
                    val isSelected = selectedTab == index
                    val animScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.0f else 0.95f,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label = "dockScale"
                    )

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .scale(animScale)
                            .clip(CircleShape)
                            .background(if (isSelected) DockPillActive else Color.Transparent)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onSelectTab(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.first,
                            contentDescription = item.second,
                            tint = if (isSelected) DockIconActive else DockIconInactive,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// SUB-COMPONENTS & HELPERS
// -------------------------------------------------------------------------------------------------
@Composable
private fun SmallActionButton(
    icon: ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(Color(0x33FFFFFF))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PureWhite,
            modifier = Modifier.size(16.dp)
        )
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
            .clip(RoundedCornerShape(18.dp))
            .background(PillBgInactive)
            .padding(vertical = 12.dp, horizontal = 10.dp),
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
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
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
    val borderColor = if (isSelected) DockBg else CardBorder
    val bgColor = if (isSelected) PillBgInactive else CardSurface

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable(enabled = enabled, onClick = onSelect)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) DockBg else TextSubtle,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) TextDark else TextSubtle
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
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
    val bgColor = if (isSelected) DockBg else PillBgInactive
    val titleColor = if (isSelected) PureWhite else TextDark
    val subColor = if (isSelected) Color(0xFFD1D5DB) else TextSubtle

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
            fontWeight = FontWeight.SemiBold,
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
