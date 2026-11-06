package com.nh.tilesmarbledesigner.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.nh.tilesmarbledesigner.data.*
import com.nh.tilesmarbledesigner.engine.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun TemplatesScreen(vm: AppViewModel, go: (String) -> Unit, back: () -> Unit) {
    ScreenFrame("Room Templates", back) {
        Text("Pick a furnished room, then change the floor and wall tiles or marble to match your taste.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Templates.all.forEach { t ->
            val thumb by produceState<Bitmap?>(null, t.id) {
                value = withContext(Dispatchers.Default) { val tp = t.build(); Scene.render(tp, tp.designs, 640, 400, 16f, 26f, 1f, 512) }
            }
            NHCard {
                Box(Modifier.fillMaxWidth().aspectRatio(1.6f).softShadow(6.dp, RoundedCornerShape(16.dp)).clip(RoundedCornerShape(16.dp)).background(NavySurface), contentAlignment = Alignment.Center) {
                    val b = thumb
                    if (b != null) Image(b.asImageBitmap(), t.name, Modifier.fillMaxSize(), contentScale = ContentScale.Crop) else CircularProgressIndicator(color = Gold)
                }
                Text(t.name, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Gold)
                Text(t.desc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button({ vm.loadTemplate(t); go("preview") }, Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Blue)) { Text("View in 3D") }
                    OutlinedButton({ vm.loadTemplate(t); vm.selectedSurface = Surf.FLOOR; go("studio/floor") }, Modifier.weight(1f)) { Text("Change tiles", color = Gold) }
                }
            }
        }
    }
}

@Composable
fun FurnitureScreen(vm: AppViewModel, go: (String) -> Unit, back: () -> Unit) {
    val p = vm.draft
    val bmps by rememberSceneBitmaps(p, p.designs)
    val boxes = remember(p.furniture, p.showFurniture, p.sections) { FurnitureLib.layout(p) }
    var yaw by remember { mutableFloatStateOf(0f) }
    var pitch by remember { mutableFloatStateOf(30f) }
    var zoom by remember { mutableFloatStateOf(1f) }
    ScreenFrame("Furniture & Templates", back) {
        RoomCanvas(bmps, null, p, yaw, pitch, zoom, modifier = Modifier.fillMaxWidth().height(270.dp), boxes = boxes) { pan, z ->
            yaw += pan.x * 0.3f; pitch = (pitch + pan.y * 0.3f).coerceIn(-80f, 80f); zoom = (zoom * z).coerceIn(0.5f, 3f)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Show furniture", Modifier.weight(1f))
            Switch(p.showFurniture, { v -> vm.update { it.copy(showFurniture = v) } })
        }
        GoldButton("Load a room template") { go("templates") }
        SectionTitle("Add furniture")
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FKind.values().forEach { k ->
                AssistChip({ vm.update { it.copy(furniture = it.furniture + Furniture(k, 0.5, 0.5, 0), showFurniture = true) } }, { Text(k.label) },
                    leadingIcon = { Icon(Icons.Filled.Add, null, Modifier.size(16.dp)) })
            }
        }
        SectionTitle("In this room (${p.furniture.size})")
        if (p.furniture.isEmpty()) Text("No furniture. Add pieces above or load a template.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        p.furniture.forEachIndexed { i, f ->
            NHCard {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(f.kind.label, fontWeight = FontWeight.Bold)
                    Row {
                        IconButton({ vm.update { q -> q.copy(furniture = q.furniture.toMutableList().also { it[i] = it[i].copy(rot = (it[i].rot + 90) % 360) }) } }) { Icon(Icons.Filled.RotateRight, "Rotate", tint = Gold) }
                        IconButton({ vm.update { q -> q.copy(furniture = q.furniture.filterIndexed { idx, _ -> idx != i }) } }) { Icon(Icons.Filled.Delete, "Remove", tint = Color(0xFFFF6B6B)) }
                    }
                }
                Text("Left wall  ↔  right wall", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Slider(f.fx.toFloat(), { v -> vm.update { q -> q.copy(furniture = q.furniture.toMutableList().also { it[i] = it[i].copy(fx = v.toDouble()) }) } })
                Text("Front  ↔  back wall", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Slider(f.fz.toFloat(), { v -> vm.update { q -> q.copy(furniture = q.furniture.toMutableList().also { it[i] = it[i].copy(fz = v.toDouble()) }) } })
            }
        }
        if (p.furniture.isNotEmpty()) TextButton({ vm.update { it.copy(furniture = emptyList()) } }, Modifier.fillMaxWidth()) { Text("Remove all furniture", color = Color(0xFFFF6B6B)) }
        PrimaryButton("Full screen 3D view") { go("fullview") }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun FullViewScreen(vm: AppViewModel, back: () -> Unit) {
    val p = vm.draft
    val view = LocalView.current
    DisposableEffect(Unit) {
        val win = view.context.findActivity()?.window
        val ctl = win?.let { WindowCompat.getInsetsController(it, view) }
        ctl?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        ctl?.hide(WindowInsetsCompat.Type.systemBars())
        onDispose { ctl?.show(WindowInsetsCompat.Type.systemBars()) }
    }
    var yaw by remember { mutableFloatStateOf(0f) }
    var pitch by remember { mutableFloatStateOf(22f) }
    var zoom by remember { mutableFloatStateOf(1f) }
    var compare by remember { mutableStateOf(false) }
    var split by remember { mutableFloatStateOf(0.5f) }
    var auto by remember { mutableStateOf(false) }
    var ui by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val after by rememberSceneBitmaps(p, p.designs)
    val before by rememberSceneBitmaps(p, Scene.before(), compare)
    val boxes = remember(p.furniture, p.showFurniture, p.sections) { FurnitureLib.layout(p) }
    LaunchedEffect(auto) { while (auto) { delay(16); yaw += 0.35f } }

    Box(Modifier.fillMaxSize().background(LocalPalette.current.bg)) {
        RoomCanvas(
            after, if (compare) before else null, p, yaw, pitch, zoom, split,
            Modifier.fillMaxSize().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { ui = !ui },
            boxes, false
        ) { pan, z ->
            yaw += pan.x * 0.3f; pitch = (pitch + pan.y * 0.3f).coerceIn(-80f, 80f); zoom = (zoom * z).coerceIn(0.5f, 4f)
        }
        if (ui) {
            @Composable
            fun Round(icon: androidx.compose.ui.graphics.vector.ImageVector, desc: String, on: Boolean = false, click: () -> Unit) =
                IconButton(click, Modifier.size(40.dp).background(if (on) Blue else Color(0x99000000), CircleShape)) { Icon(icon, desc, tint = Color.White) }
            Row(Modifier.align(Alignment.TopStart).padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Round(Icons.Filled.Close, "Close") { back() }
            }
            Row(Modifier.align(Alignment.TopEnd).padding(12.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Round(Icons.Filled.Autorenew, "Auto rotate", auto) { auto = !auto }
                Round(Icons.Filled.Chair, "Furniture", p.showFurniture) { vm.update { it.copy(showFurniture = !it.showFurniture) } }
                Round(Icons.Filled.CompareArrows, "Before / after", compare) { compare = !compare }
                Round(Icons.Filled.Download, "Save 3D image") {
                    val ctx = view.context
                    val y = yaw; val pi = pitch; val z = zoom
                    scope.launch {
                        val path = withContext(Dispatchers.Default) {
                            val b = Scene.render(p, p.designs, 1920, 1440, y, pi, z, 1280)
                            try { Exporter.saveToGallery(ctx, b, p.name + "_3dview") } finally { b.recycle() }
                        }
                        vm.toast = if (path != null) "3D image saved to $path" else "Could not save image"
                    }
                }
                Round(Icons.Filled.Share, "Share on WhatsApp") {
                    val ctx = view.context
                    val y = yaw; val pi = pitch; val z = zoom
                    scope.launch {
                        val f = withContext(Dispatchers.Default) {
                            val b = Scene.render(p, p.designs, 1280, 960, y, pi, z, 1024)
                            Exporter.savePng(ctx, b, p.name + "_3dview")
                        }
                        if (!Exporter.sendWhatsApp(ctx, f, "image/png", p.name)) { vm.toast = "WhatsApp not found - choose another app"; Exporter.share(ctx, f, "image/png") }
                    }
                }
                Round(Icons.Filled.Refresh, "Reset view") { yaw = 0f; pitch = 22f; zoom = 1f; auto = false }
            }
            Column(Modifier.align(Alignment.BottomCenter).padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                if (compare) Slider(split, { split = it }, Modifier.fillMaxWidth(0.7f), valueRange = 0.05f..0.95f)
                Tag("Drag: rotate  •  Pinch: zoom  •  Tap: hide")
            }
        }
    }
}
