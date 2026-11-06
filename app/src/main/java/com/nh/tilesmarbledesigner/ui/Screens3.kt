package com.nh.tilesmarbledesigner.ui

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.nh.tilesmarbledesigner.data.*
import com.nh.tilesmarbledesigner.engine.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.hypot
import kotlin.math.min

@Composable
fun CropScreen(vm: AppViewModel, go: (String) -> Unit, back: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val src = vm.cropSource
    var quad by remember(src) { mutableStateOf(src?.let { CropEngine.defaultQuad(it) } ?: FloatArray(8)) }
    var zoom by remember(src) { mutableFloatStateOf(1f) }
    var pan by remember(src) { mutableStateOf(Offset.Zero) }
    var angle by remember(src) { mutableFloatStateOf(0f) }
    var busy by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) scope.launch {
            busy = true
            val b = withContext(Dispatchers.IO) { CropEngine.decode(ctx, uri) }
            if (b == null) vm.toast = "Could not open this image" else vm.cropSource = b
            busy = false
        }
    }
    val pick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }

    ScreenFrame("Atomic Crop Editor", back) {
        if (src == null) {
            NHCard {
                Text("Import a photo of marble, tile or any surface, then drag the four corners to select the texture. Perspective is corrected automatically.", fontSize = 13.sp)
                PrimaryButton(if (busy) "Opening…" else "Import image from gallery", enabled = !busy) { pick() }
            }
            return@ScreenFrame
        }
        val iw = src.width; val ih = src.height
        Canvas(
            Modifier.fillMaxWidth().height(380.dp).clip(RoundedCornerShape(24.dp)).background(Color.Black)
                .pointerInput(src) {
                    var drag = -1
                    detectDragGestures(
                        onDragStart = { o ->
                            val fit = min(size.width / iw.toFloat(), size.height / ih.toFloat()) * zoom
                            val ox = size.width / 2f + pan.x - iw * fit / 2f; val oy = size.height / 2f + pan.y - ih * fit / 2f
                            var best = -1; var bd = 70f
                            for (i in 0 until 4) {
                                val d = hypot(quad[i * 2] * fit + ox - o.x, quad[i * 2 + 1] * fit + oy - o.y)
                                if (d < bd) { bd = d; best = i }
                            }
                            drag = best
                        },
                        onDrag = { ch, amt ->
                            ch.consume()
                            val fit = min(size.width / iw.toFloat(), size.height / ih.toFloat()) * zoom
                            if (drag >= 0) {
                                val q = quad.copyOf()
                                q[drag * 2] = (q[drag * 2] + amt.x / fit).coerceIn(0f, iw.toFloat())
                                q[drag * 2 + 1] = (q[drag * 2 + 1] + amt.y / fit).coerceIn(0f, ih.toFloat())
                                quad = q
                            } else pan += amt
                        }
                    )
                }
        ) {
            val fit = min(size.width / iw, size.height / ih) * zoom
            val ox = size.width / 2f + pan.x - iw * fit / 2f; val oy = size.height / 2f + pan.y - ih * fit / 2f
            drawImage(src.asImageBitmap(), IntOffset.Zero, IntSize(iw, ih), IntOffset(ox.toInt(), oy.toInt()), IntSize((iw * fit).toInt(), (ih * fit).toInt()))
            val pts = (0 until 4).map { Offset(quad[it * 2] * fit + ox, quad[it * 2 + 1] * fit + oy) }
            val path = Path().apply { moveTo(pts[0].x, pts[0].y); pts.drop(1).forEach { lineTo(it.x, it.y) }; close() }
            drawPath(path, Color(0x221F6FFF))
            drawPath(path, Color(0xFF3D8BFF), style = Stroke(4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 10f))))
            pts.forEach { drawCircle(Color.White, 16f, it); drawCircle(Color(0xFF1F6FFF), 16f, it, style = Stroke(5f)) }
        }
        Text("Drag a corner handle to resize/skew the selection. Drag elsewhere to move the image.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Zoom", Modifier.width(52.dp))
            Slider(zoom, { zoom = it }, Modifier.weight(1f), valueRange = 0.5f..4f)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Rotate", Modifier.width(52.dp))
            Slider(angle, { v -> quad = CropEngine.rotateQuad(quad, v - angle, iw, ih); angle = v }, Modifier.weight(1f), valueRange = -45f..45f)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton({ quad = CropEngine.autoQuad(src); angle = 0f }, Modifier.weight(1f)) { Icon(Icons.Filled.AutoFixHigh, null); Text(" Auto") }
            OutlinedButton({ vm.cropSource = CropEngine.rotate(src, 90f) }, Modifier.weight(1f)) { Icon(Icons.Filled.RotateRight, null); Text(" 90°") }
            OutlinedButton({ quad = CropEngine.defaultQuad(src); zoom = 1f; pan = Offset.Zero; angle = 0f }, Modifier.weight(1f)) { Icon(Icons.Filled.Refresh, null); Text(" Reset") }
        }
        GoldButton("Choose another image") { pick() }
        PrimaryButton("Crop & preview texture", enabled = !busy) {
            scope.launch {
                busy = true
                try { vm.cropResult = withContext(Dispatchers.Default) { CropEngine.warp(src, quad) }; go("texture") }
                catch (e: Throwable) { vm.toast = "Crop failed: try a smaller selection" }
                busy = false
            }
        }
    }
}

@Composable
fun TextureScreen(vm: AppViewModel, go: (String) -> Unit, back: () -> Unit) {
    val bmp = vm.cropResult
    var name by remember { mutableStateOf("My texture") }
    var grout by remember { mutableDoubleStateOf(vm.draft.design(vm.selectedSurface).groutMm) }
    val sel = vm.selectedSurface
    ScreenFrame("Texture Preview", back) {
        if (bmp == null) { Text("No cropped texture yet. Use the Atomic Crop Editor first."); PrimaryButton("Open crop editor") { back() }; return@ScreenFrame }
        Text("Cropped texture", color = Gold)
        Image(bmp.asImageBitmap(), null, Modifier.fillMaxWidth().heightIn(max = 220.dp).clip(RoundedCornerShape(16.dp)), contentScale = ContentScale.Fit)
        Text("Repeating tile preview (never stretched — each tile shows the texture at its own aspect ratio)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        val tiled by produceState<Bitmap?>(null, bmp, grout) {
            value = withContext(Dispatchers.Default) {
                PatternComposer.compose(bmp, true, SurfaceDesign("custom:preview", 0.34, 0.34, TilePattern.STRAIGHT, grout, vm.draft.design(sel).groutColor), 1.0, 1.0, 512)
            }
        }
        tiled?.let { Image(it.asImageBitmap(), null, Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(16.dp))) }
        Text("Grout: ${fmt(grout)} mm", fontSize = 13.sp)
        Slider(grout.toFloat(), { grout = it.toDouble() }, valueRange = 0f..20f)
        OutlinedTextField(name, { name = it }, label = { Text("Material name") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = fieldColors())
        Text("Apply to", color = Gold)
        ChipRow(Surf.values().toList(), sel, { it.label }) { vm.selectedSurface = it }
        PrimaryButton("Save & apply to ${sel.label}") {
            vm.saveCustomMaterial(name, bmp, sel, grout); go(if (sel == Surf.FLOOR) "studio/floor" else "studio/wall")
        }
        GoldButton("Save to library only") { vm.saveCustomMaterial(name, bmp, null) }
    }
}

@Composable
fun PreviewScreen(vm: AppViewModel, go: (String) -> Unit, back: () -> Unit) {
    val p = vm.draft
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var yaw by remember { mutableFloatStateOf(0f) }
    var pitch by remember { mutableFloatStateOf(24f) }
    var zoom by remember { mutableFloatStateOf(1f) }
    var compare by remember { mutableStateOf(false) }
    var split by remember { mutableFloatStateOf(0.5f) }
    val after by rememberSceneBitmaps(p, p.designs)
    val before by rememberSceneBitmaps(p, Scene.before(), compare)
    val boxes = remember(p.furniture, p.showFurniture, p.sections) { FurnitureLib.layout(p) }
    ScreenFrame("Room 3D Preview", back, actions = {
        IconButton({ go("fullview") }) { Icon(Icons.Filled.Fullscreen, "Full screen") }
    }) {
        Box(Modifier.fillMaxWidth().height(320.dp)) {
            RoomCanvas(after, if (compare) before else null, p, yaw, pitch, zoom, split, Modifier.fillMaxSize(), boxes) { pan, z ->
                yaw += pan.x * 0.3f; pitch = (pitch + pan.y * 0.3f).coerceIn(-80f, 80f); zoom = (zoom * z).coerceIn(0.5f, 3f)
            }
            if (compare) {
                Tag("AFTER", Modifier.align(Alignment.TopStart).padding(12.dp))
                Tag("BEFORE", Modifier.align(Alignment.TopEnd).padding(12.dp))
            }
        }
        Text("Drag to rotate • pinch to zoom", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        PrimaryButton("Full screen 3D view") { go("fullview") }
        SectionTitle("3D view image (current angle)")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton({
                val y = yaw; val pi = pitch; val z = zoom
                scope.launch {
                    val path = withContext(Dispatchers.Default) {
                        val b = Scene.render(p, p.designs, 1920, 1440, y, pi, z, 1280)
                        try { Exporter.saveToGallery(ctx, b, p.name + "_3dview") } finally { b.recycle() }
                    }
                    vm.toast = if (path != null) "3D image saved to $path" else "Could not save image"
                }
            }, Modifier.weight(1f)) { Icon(Icons.Filled.Download, null); Text(" Save to gallery") }
            OutlinedButton({
                val y = yaw; val pi = pitch; val z = zoom
                scope.launch {
                    val f = withContext(Dispatchers.Default) {
                        val b = Scene.render(p, p.designs, 1920, 1440, y, pi, z, 1280)
                        try { Exporter.savePng(ctx, b, p.name + "_3dview") } finally { b.recycle() }
                    }
                    Exporter.share(ctx, f, "image/png")
                }
            }, Modifier.weight(1f)) { Icon(Icons.Filled.Share, null); Text(" Share") }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Before / after")
            Switch(compare, { compare = it })
            if (compare) Slider(split, { split = it }, Modifier.weight(1f), valueRange = 0.05f..0.95f)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Show furniture")
            Switch(p.showFurniture, { v -> vm.update { it.copy(showFurniture = v) } })
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton({ yaw = 0f; pitch = 24f; zoom = 1f }, Modifier.weight(1f)) { Text("Reset view") }
            OutlinedButton({ yaw = 90f * ((yaw / 90f).toInt() + 1) }, Modifier.weight(1f)) { Text("Rotate 90°") }
        }
        SectionTitle("Change materials")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton({ vm.selectedSurface = Surf.FLOOR; go("studio/floor") }, Modifier.weight(1f)) { Text("Floor tiles / marble", fontSize = 12.sp) }
            OutlinedButton({ vm.selectedSurface = Surf.BACK; go("studio/wall") }, Modifier.weight(1f)) { Text("Wall tiles / marble", fontSize = 12.sp) }
        }
        GoldButton("Furniture & room templates") { go("furniture") }
        GoldButton("Export design") { go("export") }
    }
}
