package com.carui.launcher

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Bg = Color(0xFF090D15)
private val Panel = Color(0xFF151D2B)
private val Panel2 = Color(0xFF1B2637)
private val Accent = Color(0xFFB7D0FF)
private val Muted = Color(0xFF9AA9BF)
private val TextMain = Color(0xFFF4F7FC)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(9, 13, 21)
        window.navigationBarColor = android.graphics.Color.rgb(9, 13, 21)
        setContent { CarUiApp() }
    }
}

data class LaunchableApp(val label: String, val packageName: String, val icon: android.graphics.drawable.Drawable)

@Composable
private fun CarUiApp() {
    val context = LocalContext.current
    var page by remember { mutableStateOf("Home") }
    var clock by remember { mutableStateOf(currentClock()) }
    var selectedTab by remember { mutableStateOf("Home") }
    val apps = remember {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        context.packageManager.queryIntentActivities(intent, 0)
            .filter { it.activityInfo.packageName != context.packageName }
            .distinctBy { it.activityInfo.packageName }
            .map { LaunchableApp(it.loadLabel(context.packageManager).toString(), it.activityInfo.packageName, it.loadIcon(context.packageManager)) }
            .sortedBy { it.label.lowercase(Locale.getDefault()) }
    }
    LaunchedEffect(Unit) {
        while (true) { clock = currentClock(); delay(15_000) }
    }

    MaterialTheme(colorScheme = darkColorScheme(primary = Accent, background = Bg, surface = Panel)) {
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF101827), Bg)))) {
            Column(Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 14.dp)) {
                TopBar(clock = clock, onSettings = {
                    try { context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } catch (_: Exception) { }
                })
                Spacer(Modifier.height(16.dp))
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    when (page) {
                        "Apps" -> AppsPage(apps, onOpen = { launchApp(context, it.packageName) })
                        "Settings" -> SettingsPage(
                            onAndroidSettings = {
                                try { context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } catch (_: Exception) { }
                            },
                            onMaps = { openMaps(context) }
                        )
                        else -> DashboardPage(
                            apps = apps,
                            onMaps = { openMaps(context) },
                            onApps = { page = "Apps" },
                            onSettings = { page = "Settings" },
                            onOpenApp = { launchApp(context, it.packageName) }
                        )
                    }
                }
                Dock(selectedTab = selectedTab, onSelect = { tab ->
                    selectedTab = tab
                    when (tab) {
                        "Home" -> page = "Home"
                        "Apps" -> page = "Apps"
                        "Settings" -> page = "Settings"
                        "Maps" -> openMaps(context)
                        "Phone" -> openDialer(context)
                    }
                })
            }
        }
    }
}

private fun currentClock(): String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

@Composable
private fun TopBar(clock: String, onSettings: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("CARUI", color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
            Text("Drive in your own style", color = Muted, fontSize = 12.sp)
        }
        Text(clock, color = TextMain, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.width(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Bluetooth, contentDescription = "Bluetooth", tint = Muted, modifier = Modifier.size(17.dp))
            Icon(Icons.Default.Wifi, contentDescription = "Wi-Fi", tint = Muted, modifier = Modifier.size(17.dp))
        }
        Spacer(Modifier.width(12.dp))
        IconButtonSurface(icon = Icons.Default.Settings, label = "Settings", onClick = onSettings)
    }
}

@Composable
private fun DashboardPage(
    apps: List<LaunchableApp>, onMaps: () -> Unit, onApps: () -> Unit,
    onSettings: () -> Unit, onOpenApp: (LaunchableApp) -> Unit
) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(Modifier.weight(1.35f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            MapCard(onClick = onMaps, modifier = Modifier.weight(1.25f))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoTile("Connectivity", "Bluetooth & Wi-Fi", Icons.Default.Bluetooth, Modifier.weight(1f))
                InfoTile("Weather", "Add a weather provider", Icons.Default.Cloud, Modifier.weight(1f))
            }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            MediaCard(apps = apps, onOpenApp = onOpenApp, modifier = Modifier.weight(1f))
            QuickApps(apps = apps, onOpenApp = onOpenApp, onApps = onApps, modifier = Modifier.weight(0.85f))
        }
    }
}

@Composable
private fun MapCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.clip(RoundedCornerShape(28.dp)).background(Brush.linearGradient(listOf(Color(0xFF203B4B), Color(0xFF182333), Color(0xFF121B2A)))).clickable(onClick = onClick).padding(22.dp)) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RoundIcon(Icons.Default.Map, Color(0xFF9ED9CF))
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("NAVIGATION", color = Color(0xFFB5C9D7), fontSize = 11.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
                    Text("Ready when you are", color = TextMain, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                // Decorative route lines; tap anywhere on the card to open a real maps app.
                androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
                    val w = size.width; val h = size.height
                    val road = Color(0xFF4E6878)
                    drawLine(road, androidx.compose.ui.geometry.Offset(w * .08f, h * .72f), androidx.compose.ui.geometry.Offset(w * .36f, h * .40f), 8f)
                    drawLine(road, androidx.compose.ui.geometry.Offset(w * .36f, h * .40f), androidx.compose.ui.geometry.Offset(w * .63f, h * .52f), 8f)
                    drawLine(road, androidx.compose.ui.geometry.Offset(w * .63f, h * .52f), androidx.compose.ui.geometry.Offset(w * .91f, h * .18f), 8f)
                    drawLine(Accent, androidx.compose.ui.geometry.Offset(w * .08f, h * .72f), androidx.compose.ui.geometry.Offset(w * .36f, h * .40f), 3f)
                    drawLine(Accent, androidx.compose.ui.geometry.Offset(w * .36f, h * .40f), androidx.compose.ui.geometry.Offset(w * .63f, h * .52f), 3f)
                    drawLine(Accent, androidx.compose.ui.geometry.Offset(w * .63f, h * .52f), androidx.compose.ui.geometry.Offset(w * .91f, h * .18f), 3f)
                    drawCircle(Color(0xFF8FE0D1), 9f, androidx.compose.ui.geometry.Offset(w * .08f, h * .72f))
                    drawCircle(Accent, 9f, androidx.compose.ui.geometry.Offset(w * .91f, h * .18f))
                }
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Open navigation", color = TextMain, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Text("Choose Google Maps or another map app", color = Muted, fontSize = 12.sp)
                }
                Icon(Icons.Default.Navigation, contentDescription = null, tint = Accent, modifier = Modifier.size(26.dp))
            }
        }
    }
}

@Composable
private fun MediaCard(apps: List<LaunchableApp>, onOpenApp: (LaunchableApp) -> Unit, modifier: Modifier = Modifier) {
    val musicApp = apps.firstOrNull { it.label.contains("Spotify", true) }
        ?: apps.firstOrNull { it.label.contains("YouTube Music", true) }
        ?: apps.firstOrNull { it.label.contains("Music", true) }
    Column(modifier.clip(RoundedCornerShape(28.dp)).background(Brush.linearGradient(listOf(Color(0xFF342B4A), Color(0xFF1C2336)))).padding(22.dp), verticalArrangement = Arrangement.SpaceBetween) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RoundIcon(Icons.Default.MusicNote, Color(0xFFD7B8FF))
            Spacer(Modifier.width(10.dp))
            Column {
                Text("NOW PLAYING", color = Color(0xFFC6B7DB), fontSize = 11.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
                Text("Your music", color = TextMain, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        Column {
            Text("Music controls", color = TextMain, fontSize = 21.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(5.dp))
            Text(if (musicApp != null) "Open ${musicApp.label} to start playback" else "Open your preferred music app to start playback", color = Muted, fontSize = 13.sp)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { musicApp?.let(onOpenApp) }) { Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", tint = TextMain, modifier = Modifier.size(30.dp)) }
            Box(Modifier.padding(horizontal = 14.dp).size(54.dp).clip(CircleShape).background(Accent).clickable { musicApp?.let(onOpenApp) }, contentAlignment = Alignment.Center) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Open music", tint = Color(0xFF152033), modifier = Modifier.size(34.dp))
            }
            IconButton(onClick = { musicApp?.let(onOpenApp) }) { Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = TextMain, modifier = Modifier.size(30.dp)) }
        }
        Text("Tap to open music app", color = Color(0xFFB9B2CC), fontSize = 11.sp)
    }
}

@Composable
private fun QuickApps(apps: List<LaunchableApp>, onOpenApp: (LaunchableApp) -> Unit, onApps: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.clip(RoundedCornerShape(28.dp)).background(Panel).padding(18.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Your apps", color = TextMain, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, modifier = Modifier.weight(1f))
            Text("SEE ALL  ›", color = Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable(onClick = onApps))
        }
        Spacer(Modifier.height(14.dp))
        val quick = apps.filter { it.label.contains("Maps", true) || it.label.contains("Spotify", true) || it.label.contains("YouTube Music", true) || it.label.contains("Phone", true) }.take(4)
        if (quick.isEmpty()) {
            Text("Open Apps to see installed applications", color = Muted, fontSize = 13.sp)
        } else {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                quick.forEach { app ->
                    Column(Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).clickable { onOpenApp(app) }.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        AppIcon(app, 40)
                        Spacer(Modifier.height(5.dp))
                        Text(app.label, color = TextMain, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoTile(title: String, subtitle: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Column(modifier.clip(RoundedCornerShape(22.dp)).background(Panel).padding(16.dp)) {
        RoundIcon(icon, Accent)
        Spacer(Modifier.height(12.dp))
        Text(title, color = TextMain, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Text(subtitle, color = Muted, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun AppsPage(apps: List<LaunchableApp>, onOpen: (LaunchableApp) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Text("Applications", color = TextMain, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("Tap an app to open it", color = Muted, fontSize = 14.sp)
        Spacer(Modifier.height(18.dp))
        if (apps.isEmpty()) {
            Text("No launchable apps found on this device.", color = Muted)
        } else {
            LazyVerticalGrid(modifier = Modifier.weight(1f).fillMaxWidth(), columns = GridCells.Adaptive(minSize = 100.dp), contentPadding = PaddingValues(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(apps, key = { it.packageName }) { app ->
                    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Panel).clickable { onOpen(app) }.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        AppIcon(app, 48)
                        Spacer(Modifier.height(9.dp))
                        Text(app.label, color = TextMain, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsPage(onAndroidSettings: () -> Unit, onMaps: () -> Unit) {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Settings", color = TextMain, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("CarUI starter build", color = Muted, fontSize = 14.sp)
        SettingsRow("Android system settings", "Display, Bluetooth, Wi-Fi and device options", Icons.Default.Settings, onAndroidSettings)
        SettingsRow("Maps", "Opens the map app available on this device", Icons.Default.Map, onMaps)
        Text("Note: connectivity toggles, live weather, media-session controls and vehicle-specific functions are not wired up in this MVP.", color = Muted, fontSize = 13.sp)
    }
}

@Composable
private fun SettingsRow(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Panel).clickable(onClick = onClick).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        RoundIcon(icon, Accent)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = TextMain, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = Muted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun Dock(selectedTab: String, onSelect: (String) -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp)).background(Color(0xE6192231)).padding(horizontal = 8.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
        DockItem("Home", Icons.Default.DirectionsCar, selectedTab, onSelect)
        DockItem("Maps", Icons.Default.Map, selectedTab, onSelect)
        DockItem("Phone", Icons.Default.Phone, selectedTab, onSelect)
        DockItem("Apps", Icons.Default.Apps, selectedTab, onSelect)
        DockItem("Settings", Icons.Default.Settings, selectedTab, onSelect)
    }
}

@Composable
private fun DockItem(label: String, icon: ImageVector, selected: String, onSelect: (String) -> Unit) {
    val active = label == selected
    val scale by animateFloatAsState(if (active) 1.04f else 1f, label = "dockScale")
    Column(Modifier.scale(scale).clip(RoundedCornerShape(16.dp)).background(if (active) Color(0xFF293A53) else Color.Transparent).clickable { onSelect(label) }.padding(horizontal = 15.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = label, tint = if (active) Accent else Muted, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(3.dp))
        Text(label, color = if (active) TextMain else Muted, fontSize = 10.sp)
    }
}

@Composable
private fun RoundIcon(icon: ImageVector, tint: Color) {
    Box(Modifier.size(42.dp).clip(CircleShape).background(Color.White.copy(alpha = .07f)), contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun IconButtonSurface(icon: ImageVector, label: String, onClick: () -> Unit) {
    Box(Modifier.size(38.dp).clip(CircleShape).background(Panel2).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = label, tint = Accent, modifier = Modifier.size(19.dp))
    }
}

@Composable
private fun AppIcon(app: LaunchableApp, size: Int) {
    val bitmap = remember(app.packageName) {
        val drawable = app.icon
        val bmp = android.graphics.Bitmap.createBitmap(size * 3, size * 3, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bmp)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        bmp.asImageBitmapCompat()
    }
    androidx.compose.foundation.Image(bitmap, contentDescription = app.label, modifier = Modifier.size(size.dp))
}

private fun android.graphics.Bitmap.asImageBitmapCompat(): androidx.compose.ui.graphics.ImageBitmap = this.asImageBitmap()

private fun launchApp(context: android.content.Context, packageName: String) {
    try {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (_: ActivityNotFoundException) { }
}

private fun openMaps(context: android.content.Context) {
    val geo = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q="))
    try { context.startActivity(geo) } catch (_: Exception) {
        try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } catch (_: Exception) { }
    }
}

private fun openDialer(context: android.content.Context) {
    try { context.startActivity(Intent(Intent.ACTION_DIAL).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } catch (_: Exception) { }
}

