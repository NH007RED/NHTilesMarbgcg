package com.nh.tilesmarbledesigner.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nh.tilesmarbledesigner.data.*
import com.nh.tilesmarbledesigner.engine.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
private fun Thumb(path: String?, modifier: Modifier) {
    val b by produceState<Bitmap?>(null, path) { value = path?.let { withContext(Dispatchers.IO) { BitmapFactory.decodeFile(it) } } }
    Box(modifier.clip(RoundedCornerShape(14.dp)).background(NavySurface), contentAlignment = Alignment.Center) {
        if (b != null) Image(b!!.asImageBitmap(), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        else Icon(Icons.Filled.Image, null, tint = Color.Gray)
    }
}

private fun dateStr(ms: Long) = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(ms))

@Composable
fun ProjectsScreen(vm: AppViewModel, go: (String) -> Unit) {
    val list by vm.projects.collectAsState()
    var confirm by remember { mutableStateOf<ProjectEntity?>(null) }
    ScreenFrame("My Projects", scroll = false) {
        if (list.isEmpty()) {
            NHCard {
                Text("No saved projects yet. Create a room, design it, then tap Save project.")
                PrimaryButton("Create new room") { go("create") }
            }
        }
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(list, key = { it.id }) { e ->
                val pd = remember(e.json) { runCatching { ProjectData.fromJson(e.json) }.getOrNull() }
                var menu by remember { mutableStateOf(false) }
                Card(
                    onClick = { go("details/${e.id}") }, modifier = Modifier.softShadow(6.dp, RoundedCornerShape(20.dp)), shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = LocalPalette.current.card), border = BorderStroke(1.dp, LocalPalette.current.ink.copy(alpha = 0.10f))
                ) {
                    Row(Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Thumb(e.thumbPath, Modifier.size(92.dp, 70.dp))
                        Column(Modifier.weight(1f)) {
                            Text(e.name, fontWeight = FontWeight.Bold, maxLines = 2, fontSize = 14.sp)
                            if (pd != null) Text("${fmt2(pd.unit.areaFromM2(Calc.floorArea(pd)))} ${pd.unit.areaLabel}", color = Gold, fontSize = 13.sp)
                            Text(dateStr(e.updatedAt), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Box {
                            IconButton({ menu = true }) { Icon(Icons.Filled.MoreVert, "Menu") }
                            DropdownMenu(menu, { menu = false }) {
                                DropdownMenuItem({ Text("Edit") }, { menu = false; vm.open(e.id) { go("measure") } })
                                DropdownMenuItem({ Text("Duplicate") }, { menu = false; vm.duplicate(e.id) })
                                DropdownMenuItem({ Text("Delete") }, { menu = false; confirm = e })
                            }
                        }
                    }
                }
            }
        }
    }
    confirm?.let { e ->
        AlertDialog(
            onDismissRequest = { confirm = null }, title = { Text("Delete project?") }, text = { Text("\"${e.name}\" will be permanently deleted.") },
            confirmButton = { TextButton({ vm.delete(e.id); confirm = null }) { Text("Delete", color = Color(0xFFFF6B6B)) } },
            dismissButton = { TextButton({ confirm = null }) { Text("Cancel") } }
        )
    }
}

@Composable
fun DetailsScreen(vm: AppViewModel, id: Long, go: (String) -> Unit, back: () -> Unit) {
    val entity by produceState<ProjectEntity?>(null, id, vm.projects.collectAsState().value) { value = vm.load(id) }
    var confirm by remember { mutableStateOf(false) }
    ScreenFrame("Project Details", back) {
        val e = entity
        if (e == null) { Text("Loading…"); return@ScreenFrame }
        val p = remember(e.json) { ProjectData.fromJson(e.json) }
        val u = p.unit
        val r = remember(e.json) { Calc.report(p) }
        Thumb(e.thumbPath, Modifier.fillMaxWidth().height(200.dp))
        NHCard {
            Text(e.name, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Gold)
            StatRow("Created", dateStr(e.createdAt)); StatRow("Updated", dateStr(e.updatedAt))
            StatRow("Room", "${fmt(u.fromM(p.roomL))} × ${fmt(u.fromM(p.roomW))} × ${fmt(u.fromM(p.height))} ${u.label}")
            StatRow("Floor area", "${fmt2(u.areaFromM2(r.floorArea))} ${u.areaLabel}")
            StatRow("Net wall area", "${fmt2(u.areaFromM2(r.wallNet))} ${u.areaLabel}")
            StatRow("Floor boxes", "${r.floor.boxes}  (${r.floor.purchased} tiles)")
            StatRow("Wall boxes", "${r.wall.boxes}  (${r.wall.purchased} tiles)")
            StatRow("Total boxes", "${r.totalBoxes}", true)
            StatRow("Floor material", Materials.nameOf(p.design(Surf.FLOOR).materialId))
        }
        PrimaryButton("Edit project") { vm.open(id) { go("measure") } }
        GoldButton("3D preview") { vm.open(id) { go("preview") } }
        GoldButton("Export (PNG / PDF)") { vm.open(id) { go("export") } }
        GoldButton("Duplicate") { vm.duplicate(id) }
        TextButton({ confirm = true }, Modifier.fillMaxWidth()) { Text("Delete project", color = Color(0xFFFF6B6B)) }
    }
    if (confirm) AlertDialog(
        onDismissRequest = { confirm = false }, title = { Text("Delete project?") }, text = { Text("This cannot be undone.") },
        confirmButton = { TextButton({ vm.delete(id); confirm = false; back() }) { Text("Delete", color = Color(0xFFFF6B6B)) } },
        dismissButton = { TextButton({ confirm = false }) { Text("Cancel") } }
    )
}

@Composable
fun ExportScreen(vm: AppViewModel, back: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val p = vm.draft
    val views = listOf("Front" to (0f to 24f), "Left corner" to (-35f to 24f), "Right corner" to (35f to 24f), "Top view" to (0f to 80f), "Back" to (180f to 24f))
    var viewIdx by remember { mutableIntStateOf(0) }
    val angle = views[viewIdx].second
    val preview by produceState<Bitmap?>(null, p, viewIdx) { value = withContext(Dispatchers.Default) { Scene.render(p, p.designs, 1280, 960, angle.first, angle.second, 1f, 1024) } }
    var pendingFile by remember { mutableStateOf<File?>(null) }
    var busy by remember { mutableStateOf(false) }
    val saver = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("*/*")) { uri ->
        val f = pendingFile
        if (uri != null && f != null) scope.launch {
            withContext(Dispatchers.IO) { Exporter.copyTo(ctx, f, uri) }; vm.toast = "Saved"
        }
    }
    val errors = remember(p) { Calc.validate(p) }
    val caption = "${p.name} - designed with NH Tiles & Marble Room Designer"
    suspend fun png(): File? = withContext(Dispatchers.IO) {
        val b = Scene.render(p, p.designs, 1920, 1440, angle.first, angle.second, 1f, 1280)
        try { Exporter.savePng(ctx, b, p.name + "_" + views[viewIdx].first + "_3d") } finally { b.recycle() }
    }
    suspend fun pdf(): File? = if (errors.isNotEmpty()) null else withContext(Dispatchers.IO) { Exporter.createPdf(ctx, p, Calc.report(p), preview) }
    suspend fun model(): File? = try { withContext(Dispatchers.IO) { ModelExporter.exportZip(ctx, p) } } catch (e: Throwable) { vm.toast = "3D export failed"; null }
    fun whatsApp(f: File, mime: String) {
        if (!Exporter.sendWhatsApp(ctx, f, mime, caption)) { vm.toast = "WhatsApp not found - choose another app"; Exporter.share(ctx, f, mime) }
    }
    fun work(block: suspend () -> Unit) { scope.launch { busy = true; try { block() } finally { busy = false } } }
    val green = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366), contentColor = Color.Black)

    ScreenFrame("Export Design", back) {
        Box(Modifier.fillMaxWidth().aspectRatio(4f / 3f).clip(RoundedCornerShape(20.dp)).background(NavyCard), contentAlignment = Alignment.Center) {
            val b = preview
            if (b != null) Image(b.asImageBitmap(), null, Modifier.fillMaxSize()) else CircularProgressIndicator(color = Gold)
        }
        Text(p.name, fontWeight = FontWeight.Bold, color = Gold)
        if (busy) LinearProgressIndicator(Modifier.fillMaxWidth(), color = Gold)

        SectionTitle("3D view image")
        ChipRow((0 until views.size).toList(), viewIdx, { views[it].first }) { viewIdx = it }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton({
                work {
                    val b = withContext(Dispatchers.Default) { Scene.render(p, p.designs, 1920, 1440, angle.first, angle.second, 1f, 1280) }
                    val path = withContext(Dispatchers.IO) { Exporter.saveToGallery(ctx, b, p.name + "_" + views[viewIdx].first) }
                    b.recycle()
                    vm.toast = if (path != null) "Saved to $path" else "Could not save image"
                }
            }, Modifier.weight(1f), enabled = !busy) { Icon(Icons.Filled.Download, null); Text(" Save to gallery", fontSize = 12.sp) }
            OutlinedButton({
                work {
                    var ok = 0
                    withContext(Dispatchers.Default) {
                        views.forEach { (label, a) ->
                            val b = Scene.render(p, p.designs, 1920, 1440, a.first, a.second, 1f, 1280)
                            if (Exporter.saveToGallery(ctx, b, p.name + "_" + label) != null) ok++
                            b.recycle()
                        }
                    }
                    vm.toast = "$ok 3D images saved to gallery"
                }
            }, Modifier.weight(1f), enabled = !busy) { Icon(Icons.Filled.PhotoLibrary, null); Text(" Save all 5", fontSize = 12.sp) }
        }

        SectionTitle("Share on WhatsApp")
        Button({ work { png()?.let { whatsApp(it, "image/png") } } }, Modifier.fillMaxWidth().height(48.dp), enabled = !busy, colors = green, shape = RoundedCornerShape(14.dp)) {
            Icon(Icons.Filled.Share, null); Text("  3D design image", fontWeight = FontWeight.Bold)
        }
        Button({ work { pdf()?.let { whatsApp(it, "application/pdf") } } }, Modifier.fillMaxWidth().height(48.dp), enabled = errors.isEmpty() && !busy, colors = green, shape = RoundedCornerShape(14.dp)) {
            Icon(Icons.Filled.PictureAsPdf, null); Text("  PDF calculation report", fontWeight = FontWeight.Bold)
        }
        Button({ work { model()?.let { whatsApp(it, "application/zip") } } }, Modifier.fillMaxWidth().height(48.dp), enabled = !busy, colors = green, shape = RoundedCornerShape(14.dp)) {
            Icon(Icons.Filled.ViewInAr, null); Text("  3D model (OBJ .zip)", fontWeight = FontWeight.Bold)
        }

        SectionTitle("3D model (OBJ + MTL + textures)")
        Text("Opens in Blender, Windows 3D Viewer, MeshLab, Sketchfab and other 3D apps. Includes room, textures and furniture.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton({ work { model()?.let { pendingFile = it; saver.launch(it.name) } } }, Modifier.weight(1f), enabled = !busy) { Icon(Icons.Filled.Download, null); Text(" Save") }
            OutlinedButton({ work { model()?.let { Exporter.share(ctx, it, "application/zip") } } }, Modifier.weight(1f), enabled = !busy) { Icon(Icons.Filled.Share, null); Text(" Share") }
        }

        SectionTitle("3D view image (PNG file)")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton({ work { png()?.let { pendingFile = it; saver.launch(it.name) } } }, Modifier.weight(1f), enabled = preview != null && !busy) { Icon(Icons.Filled.Download, null); Text(" Save") }
            OutlinedButton({ work { png()?.let { Exporter.share(ctx, it, "image/png") } } }, Modifier.weight(1f), enabled = preview != null && !busy) { Icon(Icons.Filled.Share, null); Text(" Share") }
        }
        SectionTitle("Calculation report (PDF)")
        if (errors.isNotEmpty()) errors.forEach { Text("• $it", color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton({ work { pdf()?.let { pendingFile = it; saver.launch(it.name) } } }, Modifier.weight(1f), enabled = errors.isEmpty() && !busy) { Icon(Icons.Filled.PictureAsPdf, null); Text(" Save") }
            OutlinedButton({ work { pdf()?.let { Exporter.share(ctx, it, "application/pdf") } } }, Modifier.weight(1f), enabled = errors.isEmpty() && !busy) { Icon(Icons.Filled.Share, null); Text(" Share") }
        }
        PrimaryButton("Save project") { vm.save() }
    }
}

@Composable
fun SettingsScreen(vm: AppViewModel) {
    val s by vm.settings.collectAsState()
    ScreenFrame("Settings") {
        val pal = LocalPalette.current
        SectionTitle("Theme (${pal.name})")
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Palettes.all.forEach { pp -> ThemeSwatch(pp, pp.id == pal.id) { vm.saveSettings(s.copy(themeId = pp.id)) } }
        }
        NHCard {
            SectionTitle("Defaults for new rooms")
            Text("Measurement unit")
            ChipRow(LengthUnit.values().toList(), s.unit, { it.label }) { vm.saveSettings(s.copy(unit = it)) }
            NumField("Default wastage", s.wastage, "w", "%", allowZero = true) { v -> if (v <= 100) vm.saveSettings(s.copy(wastage = v)) }
            Text("Default grout width: ${fmt(s.groutMm)} mm")
            Slider(s.groutMm.toFloat(), { vm.saveSettings(s.copy(groutMm = it.toDouble())) }, valueRange = 0f..20f)
            Text("Default grout colour")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(0xFFB8B8B8, 0xFFFFFFFF, 0xFF2B2B2B, 0xFFD4AF37, 0xFF8B7355, 0xFFE8DCC8).forEach {
                    val c = it.toInt(); ColorDot(c, s.groutColor == c) { vm.saveSettings(s.copy(groutColor = c)) }
                }
            }
            Text("Defaults apply to rooms created from now on.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        NHCard {
            SectionTitle("About")
            Text("NH Tiles & Marble Room Designer 3.4.0")
            Text("com.nh.tilesmarbledesigner", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Branding: NH Developer", fontSize = 12.sp)
            Text("Switch theme above: Forest, Lavender, Ocean, Sunset, Rose, Slate or Midnight (dark).", fontSize = 12.sp)
            Text("100% offline. This app declares no INTERNET permission; all projects, textures and exports stay on your device.", fontSize = 12.sp)
        }
    }
}
