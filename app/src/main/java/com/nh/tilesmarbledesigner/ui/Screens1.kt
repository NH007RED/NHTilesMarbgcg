package com.nh.tilesmarbledesigner.ui

import android.graphics.Bitmap
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nh.tilesmarbledesigner.data.*
import com.nh.tilesmarbledesigner.engine.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Composable
fun LogoMark(modifier: Modifier = Modifier) {
    val pal = LocalPalette.current
    val roofC = pal.accent
    val cols = listOf(pal.dark, pal.accent, pal.soft, pal.chip)
    Canvas(modifier) {
        val w = size.width; val h = size.height
        val roof = Path().apply {
            moveTo(w * 0.08f, h * 0.45f); lineTo(w * 0.5f, h * 0.08f); lineTo(w * 0.92f, h * 0.45f)
            lineTo(w * 0.82f, h * 0.53f); lineTo(w * 0.5f, h * 0.25f); lineTo(w * 0.18f, h * 0.53f); close()
        }
        drawPath(roof, roofC)
        for (i in 0 until 4) {
            val x = w * (0.28f + (i % 2) * 0.24f); val y = h * (0.52f + (i / 2) * 0.24f)
            drawRoundRect(cols[i], Offset(x, y), Size(w * 0.21f, h * 0.21f), CornerRadius(10f))
        }
    }
}

@Composable
fun SplashScreen(onDone: () -> Unit) {
    val pal = LocalPalette.current
    val a = remember { Animatable(0f) }
    LaunchedEffect(Unit) { a.animateTo(1f, tween(900)); delay(700); onDone() }
    Box(
        Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(pal.bg, pal.chip))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier.alpha(a.value).scale(0.9f + 0.1f * a.value),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(Modifier.size(132.dp).softShadow(14.dp, RoundedCornerShape(38.dp)).background(pal.card, RoundedCornerShape(38.dp)), contentAlignment = Alignment.Center) {
                LogoMark(Modifier.size(92.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text("Tiles & Marble", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = pal.ink)
            Text("Room Designer", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = pal.accent)
            Text("Design • Measure • Calculate • Decorate", color = pal.label)
            Spacer(Modifier.height(16.dp))
            Text("NH Developer  ·  100% Offline", color = pal.dark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun FeatureTile(icon: ImageVector, title: String, sub: String, modifier: Modifier, onClick: () -> Unit) {
    val pal = LocalPalette.current
    Card(
        onClick = onClick, modifier = modifier.height(112.dp).softShadow(7.dp, RoundedCornerShape(20.dp)), shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = pal.card),
        border = BorderStroke(1.dp, pal.ink.copy(alpha = 0.10f))
    ) {
        Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
            IconBadge(icon)
            Column {
                Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, lineHeight = 16.sp, color = pal.ink, maxLines = 2)
                Text(sub, fontSize = 11.sp, color = pal.label, maxLines = 1)
            }
        }
    }
}

@Composable
private fun StatPill(label: String, value: String, modifier: Modifier) {
    val pal = LocalPalette.current
    Column(modifier.softShadow(5.dp, RoundedCornerShape(16.dp)).background(pal.card, RoundedCornerShape(16.dp)).padding(horizontal = 12.dp, vertical = 8.dp)) {
        Text(label, fontSize = 11.sp, color = pal.label)
        Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = pal.highlight, maxLines = 1)
    }
}

@Composable
fun HomeScreen(vm: AppViewModel, go: (String) -> Unit) {
    val pal = LocalPalette.current
    val p = vm.draft
    val projects by vm.projects.collectAsState()
    val settings by vm.settings.collectAsState()
    val hero by produceState<Bitmap?>(null, p) { value = withContext(Dispatchers.Default) { Scene.render(p, p.designs, 800, 450, 18f, 26f, 1f) } }
    ScreenFrame("Tiles & Marble", actions = {
        IconButton({ go("settings") }) { Icon(Icons.Filled.Palette, "Theme", tint = pal.accent) }
    }) {
        Text("Design  •  Measure  •  Calculate  •  Decorate", color = pal.label, fontSize = 12.sp)
        Box(Modifier.fillMaxWidth().height(180.dp).softShadow(12.dp, RoundedCornerShape(22.dp)).clip(RoundedCornerShape(22.dp)).background(pal.chip)) {
            hero?.let { Image(it.asImageBitmap(), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xB3000000)))))
            Column(Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                Text("Build Your Dream Room", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color.White)
                Text(p.name + "  ·  " + fmt2(p.unit.areaFromM2(Calc.floorArea(p))) + " " + p.unit.areaLabel, fontSize = 12.sp, color = Color(0xCCFFFFFF))
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatPill("Projects", "${projects.size}", Modifier.weight(1f))
            StatPill("Unit", settings.unit.label, Modifier.weight(1f))
            StatPill("Theme", pal.name.substringBefore(' '), Modifier.weight(1f))
        }
        PrimaryButton("Create new room") { go("create") }
        val items = listOf(
            Triple(Icons.Filled.Straighten, "Room Measurement", "Size, walls, doors") to "measure",
            Triple(Icons.Filled.Calculate, "Tile Calculator", "Tiles, boxes, marble") to "calc",
            Triple(Icons.Filled.Layers, "Wall Designer", "Patterns & textures") to "studio/wall",
            Triple(Icons.Filled.GridView, "Floor Designer", "Tiles, marble, wood") to "studio/floor",
            Triple(Icons.Filled.Crop, "Atomic Crop Tool", "Crop to texture") to "crop",
            Triple(Icons.Filled.Image, "Material Library", "Built-in & custom") to "library",
            Triple(Icons.Filled.Chair, "Room Templates", "5 furnished rooms") to "templates",
            Triple(Icons.Filled.Weekend, "Furniture", "Add, move, remove") to "furniture",
            Triple(Icons.Filled.ViewInAr, "Room 3D Preview", "Rotate & compare") to "preview",
            Triple(Icons.Filled.Fullscreen, "Full Screen 3D", "Walk around") to "fullview",
            Triple(Icons.Filled.Folder, "My Designs", "Saved projects") to "projects",
            Triple(Icons.Filled.Share, "Export & Share", "PNG, PDF, 3D, share") to "export"
        )
        items.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { (t, route) -> FeatureTile(t.first, t.second, t.third, Modifier.weight(1f)) { go(route) } }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
        NHCard {
            Text("Premium Interior Design", color = pal.accent, fontWeight = FontWeight.ExtraBold)
            Text("Smart design • Smart calculation • Works fully offline — no internet permission.", fontSize = 12.sp, color = pal.label)
        }
    }
}

@Composable
fun CreateRoomScreen(vm: AppViewModel, go: (String) -> Unit, back: () -> Unit) {
    val s = vm.settings.collectAsState().value
    var name by remember { mutableStateOf("") }
    var shape by remember { mutableStateOf(RoomShape.RECTANGLE) }
    var unit by remember { mutableStateOf(s.unit) }
    var l by remember { mutableDoubleStateOf(if (s.unit == LengthUnit.FT || s.unit == LengthUnit.IN) 3.048 else 3.0) }
    var w by remember { mutableDoubleStateOf(l) }
    var h by remember { mutableDoubleStateOf(l) }
    val ww = if (shape == RoomShape.SQUARE) l else w
    ScreenFrame("Create Room", back) {
        OutlinedTextField(name, { name = it }, label = { Text("Room / project name") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = fieldColors())
        Text("Room type", color = Gold)
        ChipRow(RoomShape.values().toList(), shape, { it.label }) { shape = it }
        Text("Unit", color = Gold)
        ChipRow(LengthUnit.values().toList(), unit, { it.label }) { unit = it }
        DimField("Room length", l, unit) { l = it }
        if (shape != RoomShape.SQUARE) DimField("Room width", w, unit) { w = it }
        DimField("Wall height", h, unit) { h = it }
        NHCard { StatRow("Floor area", "${fmt2(unit.areaFromM2(l * ww))} ${unit.areaLabel}", true) }
        PrimaryButton("Create Room", enabled = l > 0 && ww > 0 && h > 0) {
            vm.newDraft(name, shape, unit, l, ww, h); go("measure")
        }
    }
}

private fun DrawScope.label(s: String, x: Float, y: Float, ink: Int, size: Float = 30f) {
    drawContext.canvas.nativeCanvas.drawText(s, x, y, android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        textSize = size; color = ink; textAlign = android.graphics.Paint.Align.CENTER
    })
}

@Composable
fun MeasurementScreen(vm: AppViewModel, go: (String) -> Unit) {
    val p = vm.draft; val u = p.unit
    ScreenFrame("Room Measurement") {
        OutlinedTextField(p.name, { n -> vm.update { it.copy(name = n) } }, label = { Text("Project name") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = fieldColors())
        ChipRow(RoomShape.values().toList(), p.shape, { it.label }) { sh ->
            vm.update { it.copy(shape = sh).let { q -> if (sh == RoomShape.SQUARE) q.withRoom(q.roomL, q.roomL, q.height) else q } }
        }
        ChipRow(LengthUnit.values().toList(), u, { it.label }) { nu -> vm.update { it.copy(unit = nu) } }
        val pal = LocalPalette.current
        val roomFill = pal.soft; val roomLine = pal.dark; val inkC = pal.ink.toArgb()
        Canvas(Modifier.fillMaxWidth().height(210.dp).clip(RoundedCornerShape(20.dp)).background(pal.chip)) {
            val l = p.roomL.toFloat(); val w = p.roomW.toFloat()
            val k = minOf((size.width - 140f) / l, (size.height - 90f) / w)
            val tl = Offset((size.width - l * k) / 2f, (size.height - w * k) / 2f + 10f)
            drawRect(roomFill, tl, Size(l * k, w * k))
            drawRect(roomLine, tl, Size(l * k, w * k), style = Stroke(5f))
            label("${fmt(u.fromM(p.roomL))} ${u.label}", size.width / 2f, tl.y - 12f, inkC)
            label("${fmt(u.fromM(p.roomW))} ${u.label}", tl.x + l * k + 62f, tl.y + w * k / 2f, inkC)
            label("Floor ${fmt2(u.areaFromM2(p.roomL * p.roomW))} ${u.areaLabel}", size.width / 2f, tl.y + w * k / 2f + 10f, inkC, 28f)
        }
        DimField("Room length", p.roomL, u) { v -> vm.update { q -> q.withRoom(v, if (q.shape == RoomShape.SQUARE) v else q.roomW, q.height) } }
        if (p.shape == RoomShape.SQUARE) Text("Square room: width always equals length.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        else DimField("Room width", p.roomW, u) { v -> vm.update { q -> q.withRoom(q.roomL, v, q.height) } }
        DimField("Wall height", p.height, u) { v -> vm.update { q -> q.withRoom(q.roomL, q.roomW, v) } }
        val r = remember(p) { Calc.report(p) }
        NHCard {
            StatRow("Total floor area", "${fmt2(u.areaFromM2(r.floorArea))} ${u.areaLabel}")
            StatRow("Net wall area", "${fmt2(u.areaFromM2(r.wallNet))} ${u.areaLabel}")
        }
        GoldButton("Wall measurement (walls, doors, windows)") { go("wall") }
        GoldButton("Floor measurement (irregular sections)") { go("floor") }
        PrimaryButton("Tile Calculator") { go("calc") }
    }
}

@Composable
fun WallScreen(vm: AppViewModel, back: () -> Unit) {
    val p = vm.draft; val u = p.unit
    var showAdd by remember { mutableStateOf(false) }
    ScreenFrame("Wall Measurement", back) {
        Text("Each wall is length × height. Editing room size in Measurement re-syncs walls 1–4.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        p.walls.forEachIndexed { i, w ->
            NHCard {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    SectionTitle("Wall ${i + 1} (${Scene.wallName(i)})")
                    if (i >= 4) IconButton({
                        vm.update { q ->
                            q.copy(walls = q.walls.filterIndexed { idx, _ -> idx != i },
                                openings = q.openings.filter { it.wall != i }.map { if (it.wall > i) it.copy(wall = it.wall - 1) else it })
                        }
                    }) { Icon(Icons.Filled.Delete, "Delete wall", tint = Color(0xFFFF6B6B)) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DimField("Length", w.length, u, Modifier.weight(1f)) { v -> vm.update { q -> q.copy(walls = q.walls.toMutableList().also { it[i] = it[i].copy(length = v) }) } }
                    DimField("Height", w.height, u, Modifier.weight(1f)) { v -> vm.update { q -> q.copy(walls = q.walls.toMutableList().also { it[i] = it[i].copy(height = v) }) } }
                }
                val g = w.length * w.height; val o = Calc.openingArea(p, i)
                StatRow("Net area", "${fmt2(u.areaFromM2(g - o))} ${u.areaLabel}")
            }
        }
        GoldButton("Add wall") { vm.update { q -> q.copy(walls = q.walls + Wall(q.roomL, q.height)) } }
        SectionTitle("Doors & windows")
        if (p.openings.isEmpty()) Text("No openings. Add doors/windows to deduct them from wall area.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        p.openings.forEachIndexed { i, o ->
            NHCard {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("${if (o.door) "Door" else "Window"}  ${fmt(u.fromM(o.w))} × ${fmt(u.fromM(o.h))} ${u.label}  × ${o.count}  (Wall ${o.wall + 1}, ${Scene.wallName(o.wall)}, ${fmt(o.pos * 100)}% along)", Modifier.weight(1f), fontSize = 13.sp)
                    IconButton({ vm.update { q -> q.copy(openings = q.openings.filterIndexed { idx, _ -> idx != i }) } }) { Icon(Icons.Filled.Delete, "Delete", tint = Color(0xFFFF6B6B)) }
                }
            }
        }
        PrimaryButton("Add door / window") { showAdd = true }
        val errs = Calc.validate(p).filter { it.startsWith("Wall") || it.startsWith("Opening") }
        errs.forEach { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
    }
    if (showAdd) {
        var wall by remember { mutableIntStateOf(0) }
        var door by remember { mutableStateOf(true) }
        var ow by remember { mutableDoubleStateOf(0.9) }
        var oh by remember { mutableDoubleStateOf(2.1) }
        var cnt by remember { mutableIntStateOf(1) }
        var pos by remember { mutableFloatStateOf(0.5f) }
        var sill by remember { mutableDoubleStateOf(0.0) }
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("Add opening") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ChipRow(listOf(true, false), door, { if (it) "Door" else "Window" }) { door = it; ow = if (it) 0.9 else 1.2; oh = if (it) 2.1 else 1.2; sill = if (it) 0.0 else 0.9 }
                    ChipRow(p.walls.indices.toList(), wall, { "Wall ${it + 1} (${Scene.wallName(it)})" }) { wall = it }
                    DimField("Width", ow, u, key = door) { ow = it }
                    DimField("Height", oh, u, key = door) { oh = it }
                    if (!door) NumField("Sill height (above floor)", u.fromM(sill), listOf(u, door), u.label, allowZero = true) { sill = u.toM(it) }
                    IntField("Count", cnt, 1) { cnt = it }
                    Text("Position along wall: ${(pos * 100).toInt()} %", fontSize = 13.sp)
                    Slider(pos, { pos = it }, valueRange = 0.1f..0.9f)
                }
            },
            confirmButton = {
                TextButton({
                    vm.update { q -> q.copy(openings = q.openings + Opening(wall, door, ow, oh, cnt, pos.toDouble(), if (door) 0.0 else sill)) }
                    showAdd = false
                }) { Text("Add", color = Gold) }
            },
            dismissButton = { TextButton({ showAdd = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun DimField(label: String, meters: Double, unit: LengthUnit, modifier: Modifier = Modifier, key: Any?, onMeters: (Double) -> Unit) =
    NumField(label, unit.fromM(meters), listOf(unit, key), unit.label, modifier) { onMeters(unit.toM(it)) }

@Composable
fun FloorScreen(vm: AppViewModel, back: () -> Unit) {
    val p = vm.draft; val u = p.unit
    ScreenFrame("Floor Measurement", back) {
        Text("For irregular rooms (L-shape, alcoves…) split the floor into rectangles. Section 1 is the main room.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        p.sections.forEachIndexed { i, s ->
            NHCard {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    SectionTitle(if (i == 0) "Section 1 (main room)" else "Section ${i + 1}")
                    if (i > 0) IconButton({ vm.update { q -> q.copy(sections = q.sections.filterIndexed { idx, _ -> idx != i }) } }) { Icon(Icons.Filled.Delete, "Delete", tint = Color(0xFFFF6B6B)) }
                }
                if (i == 0) {
                    StatRow("Length × width", "${fmt(u.fromM(s.length))} × ${fmt(u.fromM(s.width))} ${u.label}")
                    Text("Edit the main room in Room Measurement.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DimField("Length", s.length, u, Modifier.weight(1f)) { v -> vm.update { q -> q.copy(sections = q.sections.toMutableList().also { it[i] = it[i].copy(length = v) }) } }
                    DimField("Width", s.width, u, Modifier.weight(1f)) { v -> vm.update { q -> q.copy(sections = q.sections.toMutableList().also { it[i] = it[i].copy(width = v) }) } }
                }
                StatRow("Area", "${fmt2(u.areaFromM2(s.length * s.width))} ${u.areaLabel}")
            }
        }
        GoldButton("Add floor section") { vm.update { q -> q.copy(sections = q.sections + Section(q.roomL / 2, q.roomW / 2)) } }
        NHCard { StatRow("Total floor area", "${fmt2(u.areaFromM2(Calc.floorArea(p)))} ${u.areaLabel}", true) }
        Text("The 3D preview shows the main rectangle; extra sections are included in all calculations.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
