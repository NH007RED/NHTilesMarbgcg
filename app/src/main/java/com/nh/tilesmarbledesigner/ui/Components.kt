package com.nh.tilesmarbledesigner.ui

import android.graphics.Bitmap
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.automirrored.filled.ArrowBack

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nh.tilesmarbledesigner.data.*
import com.nh.tilesmarbledesigner.engine.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.Locale

fun fmt(d: Double): String {
    val s = String.format(Locale.US, "%.3f", d).trimEnd('0').trimEnd('.')
    return if (s.isEmpty() || s == "-0") "0" else s
}

fun fmt2(d: Double): String = String.format(Locale.US, "%.2f", d)

fun parseNum(t: String): Double? = t.trim().replace(',', '.').toDoubleOrNull()

/** Soft shadow tinted with the active theme (black on the dark theme). */
@Composable
fun Modifier.softShadow(elevation: Dp = 6.dp, shape: Shape = RoundedCornerShape(20.dp)): Modifier {
    val pal = LocalPalette.current
    val amb = if (pal.isDark) Color.Black.copy(alpha = 0.5f) else pal.dark.copy(alpha = 0.22f)
    val spot = if (pal.isDark) Color.Black.copy(alpha = 0.7f) else pal.dark.copy(alpha = 0.40f)
    return this.shadow(elevation, shape, clip = false, ambientColor = amb, spotColor = spot)
}

@Composable
fun ScreenFrame(
    title: String, onBack: (() -> Unit)? = null, scroll: Boolean = true,
    actions: @Composable RowScope.() -> Unit = {}, content: @Composable ColumnScope.() -> Unit
) {
    val pal = LocalPalette.current
    Column(Modifier.fillMaxSize().background(pal.bg)) {
        Row(
            Modifier.fillMaxWidth().padding(start = if (onBack != null) 12.dp else 16.dp, end = 8.dp, top = 8.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(onBack, Modifier.size(38.dp).softShadow(3.dp, androidx.compose.foundation.shape.CircleShape).background(pal.card, androidx.compose.foundation.shape.CircleShape)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = pal.accent, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(10.dp))
            }
            Text(
                title, Modifier.weight(1f), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = pal.accent,
                maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically, content = actions)
        }
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
            Column(
                Modifier.widthIn(max = 640.dp).fillMaxWidth().fillMaxHeight()
                    .then(if (scroll) Modifier.verticalScroll(rememberScrollState()) else Modifier)
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp), content = content
            )
        }
    }
}

@Composable
fun NHCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val pal = LocalPalette.current
    Card(
        modifier.fillMaxWidth().softShadow(6.dp, RoundedCornerShape(20.dp)), shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = pal.card),
        border = BorderStroke(1.dp, pal.ink.copy(alpha = 0.10f))
    ) { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content) }
}

@Composable
fun SectionTitle(t: String) {
    val pal = LocalPalette.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(4.dp).height(18.dp).background(pal.primary, RoundedCornerShape(2.dp)))
        Spacer(Modifier.width(8.dp))
        Text(t, color = pal.accent, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
    }
}

@Composable
fun PrimaryButton(text: String, enabled: Boolean = true, modifier: Modifier = Modifier, onClick: () -> Unit) =
    Button(
        onClick, modifier.fillMaxWidth().heightIn(min = 48.dp).softShadow(6.dp, RoundedCornerShape(16.dp)), enabled = enabled, shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = LocalPalette.current.dark, contentColor = Color.White),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
    ) { Text(text, fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center) }

@Composable
fun GoldButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) =
    Button(
        onClick, modifier.fillMaxWidth().heightIn(min = 48.dp).softShadow(4.dp, RoundedCornerShape(16.dp)), enabled = enabled, shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = LocalPalette.current.chip, contentColor = LocalPalette.current.accent),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
    ) { Text(text, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center) }

/** Same-package replacement for material3 OutlinedButton: one uniform tonal style, text wraps instead of clipping. */
@Composable
fun OutlinedButton(onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, content: @Composable RowScope.() -> Unit) {
    val pal = LocalPalette.current
    Button(
        onClick, modifier.heightIn(min = 44.dp).softShadow(3.dp, RoundedCornerShape(14.dp)), enabled = enabled, shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = pal.chip, contentColor = pal.accent,
            disabledContainerColor = pal.chip.copy(alpha = 0.5f), disabledContentColor = pal.ink.copy(alpha = 0.4f)
        ),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
    ) { content() }
}

@Composable
fun fieldColors(): TextFieldColors {
    val pal = LocalPalette.current
    return OutlinedTextFieldDefaults.colors(
        focusedContainerColor = pal.chip, unfocusedContainerColor = pal.chip, errorContainerColor = pal.chip,
        focusedBorderColor = pal.dark, unfocusedBorderColor = Color.Transparent,
        focusedLabelColor = pal.dark, unfocusedLabelColor = pal.label,
        cursorColor = pal.dark
    )
}

@Composable
fun NumField(
    label: String, value: Double, key: Any?, suffix: String, modifier: Modifier = Modifier,
    allowZero: Boolean = false, onValue: (Double) -> Unit
) {
    var text by remember(key) { mutableStateOf(fmt(value)) }
    val parsed = parseNum(text)
    val bad = parsed == null || parsed < 0 || (!allowZero && parsed <= 0)
    OutlinedTextField(
        value = text, onValueChange = { t -> text = t; parseNum(t)?.let { if (it >= 0) onValue(it) } },
        label = { Text(label) }, suffix = { Text(suffix) }, singleLine = true, isError = bad,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = fieldColors(),
        supportingText = if (bad) { { Text(if (allowZero) "Enter 0 or more" else "Enter a number above 0", fontSize = 11.sp) } } else null
    )
}

/** Field showing a metre value in the project's unit. */
@Composable
fun DimField(label: String, meters: Double, unit: LengthUnit, modifier: Modifier = Modifier, onMeters: (Double) -> Unit) =
    NumField(label, unit.fromM(meters), unit, unit.label, modifier) { onMeters(unit.toM(it)) }

@Composable
fun IntField(label: String, value: Int, min: Int, modifier: Modifier = Modifier, onValue: (Int) -> Unit) {
    var text by remember { mutableStateOf(value.toString()) }
    val parsed = text.toIntOrNull()
    OutlinedTextField(
        value = text, onValueChange = { t -> text = t.filter { it.isDigit() }.take(6); text.toIntOrNull()?.let { if (it >= min) onValue(it) } },
        label = { Text(label) }, singleLine = true, isError = parsed == null || parsed < min,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = fieldColors(),
        supportingText = if (parsed == null || parsed < min) { { Text("Minimum $min", fontSize = 11.sp) } } else null
    )
}

@Composable
fun <T> ChipRow(options: List<T>, selected: T, label: (T) -> String, onSelect: (T) -> Unit) {
    val pal = LocalPalette.current
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { o ->
            FilterChip(
                selected = o == selected, onClick = { onSelect(o) }, label = { Text(label(o)) },
                shape = RoundedCornerShape(16.dp), border = null,
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = pal.chip, labelColor = pal.ink,
                    selectedContainerColor = pal.dark, selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
fun StatRow(label: String, value: String, big: Boolean = false) {
    val pal = LocalPalette.current
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f, fill = false), color = pal.label, fontSize = 13.sp)
        Spacer(Modifier.width(10.dp))
        Text(value, fontWeight = FontWeight.Bold, color = if (big) pal.highlight else pal.ink, fontSize = if (big) 18.sp else 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.End)
    }
}

@Composable
fun ColorDot(c: Int, selected: Boolean, onClick: () -> Unit) {
    val pal = LocalPalette.current
    val shape = androidx.compose.foundation.shape.CircleShape
    Box(
        Modifier.size(36.dp).clip(shape).background(Color(c))
            .border(if (selected) 3.dp else 1.dp, if (selected) pal.dark else pal.ink.copy(alpha = 0.3f), shape)
            .clickable(onClick = onClick)
    )
}

/** Small rounded label used on top of 3D views. */
@Composable
fun Tag(text: String, modifier: Modifier = Modifier) {
    val pal = LocalPalette.current
    Text(
        text, modifier.background(pal.card.copy(alpha = 0.88f), RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 6.dp),
        color = pal.accent, fontWeight = FontWeight.Bold, fontSize = 11.sp
    )
}

/** Circular icon badge used on tiles. */
@Composable
fun IconBadge(icon: androidx.compose.ui.graphics.vector.ImageVector, size: Int = 36) {
    val pal = LocalPalette.current
    Box(Modifier.size(size.dp).background(pal.chip, androidx.compose.foundation.shape.CircleShape), contentAlignment = Alignment.Center) {
        Icon(icon, null, tint = pal.dark, modifier = Modifier.size((size * 0.5f).dp))
    }
}

/** Theme preview tile: pastel square with accent dots; selected one gets a ring and a check. */
@Composable
fun ThemeSwatch(p: NHPalette, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(24.dp)
    Box(
        Modifier.size(84.dp).softShadow(if (selected) 8.dp else 3.dp, shape).clip(shape).background(p.bg)
            .border(if (selected) 3.dp else 0.dp, if (selected) p.dark else Color.Transparent, shape)
            .clickable(onClick = onClick)
    ) {
        Box(Modifier.align(Alignment.TopStart).padding(10.dp).size(14.dp).background(p.primary, RoundedCornerShape(5.dp)))
        Box(Modifier.align(Alignment.BottomStart).padding(10.dp).size(9.dp).background(p.soft, androidx.compose.foundation.shape.CircleShape))
        Box(Modifier.align(Alignment.BottomEnd).padding(10.dp).size(14.dp).background(p.dark, RoundedCornerShape(5.dp)))
        if (selected) Box(Modifier.align(Alignment.Center).size(30.dp).background(p.dark, androidx.compose.foundation.shape.CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun MaterialSwatch(id: String, name: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val pal = LocalPalette.current
    val bmp by produceState<Bitmap?>(null, id) { value = withContext(Dispatchers.Default) { Materials.bitmap(id) } }
    Column(modifier.clickable(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.size(68.dp).softShadow(3.dp, RoundedCornerShape(16.dp)).clip(RoundedCornerShape(16.dp))
                .border(if (selected) 3.dp else 1.dp, if (selected) pal.dark else pal.ink.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
        ) { bmp?.let { Image(it.asImageBitmap(), name, Modifier.fillMaxSize(), contentScale = androidx.compose.ui.layout.ContentScale.Crop) } }
        Text(name, Modifier.width(72.dp), fontSize = 10.sp, maxLines = 2, textAlign = androidx.compose.ui.text.style.TextAlign.Center, lineHeight = 12.sp, color = if (selected) pal.dark else pal.ink, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}

/** Computes (debounced, cached) surface textures off the UI thread. Keeps the previous result while recomputing. */
@Composable
fun rememberSceneBitmaps(p: ProjectData, designs: Map<Surf, SurfaceDesign>, enabled: Boolean = true): State<Map<Surf, Bitmap>?> =
    produceState<Map<Surf, Bitmap>?>(null, p.sections, p.height, p.openings, designs, enabled) {
        if (!enabled) { value = null; return@produceState }
        delay(60)
        value = withContext(Dispatchers.Default) { Scene.surfaceBitmaps(p, designs) }
    }

/** Interactive perspective room view. If [before] is set, draws a split before/after comparison at [split]. */
@Composable
fun RoomCanvas(
    after: Map<Surf, Bitmap>?, before: Map<Surf, Bitmap>?, p: ProjectData,
    yaw: Float, pitch: Float, zoom: Float, split: Float = 0.5f, modifier: Modifier = Modifier,
    boxes: List<Box3> = emptyList(), rounded: Boolean = true,
    onGesture: ((Offset, Float) -> Unit)? = null
) {
    val pal = LocalPalette.current
    val stageTop = pal.soft; val stageBottom = pal.bg; val divider = pal.dark
    val m = if (onGesture != null) modifier.pointerInput(Unit) { detectTransformGestures { _, pan, z, _ -> onGesture(pan, z) } } else modifier
    Canvas(if (rounded) m.softShadow(8.dp, RoundedCornerShape(20.dp)).clip(RoundedCornerShape(20.dp)) else m) {
        drawRect(Brush.verticalGradient(listOf(stageTop, stageBottom)))
        if (after == null) return@Canvas
        drawIntoCanvas { cv ->
            val n = cv.nativeCanvas
            val L = p.roomL.toFloat(); val W = p.roomW.toFloat(); val H = p.height.toFloat()
            if (before != null) {
                n.save(); n.clipRect(0f, 0f, size.width * split, size.height)
                RoomRenderer.render(n, size.width, size.height, L, W, H, after, yaw, pitch, zoom, boxes); n.restore()
                n.save(); n.clipRect(size.width * split, 0f, size.width, size.height)
                RoomRenderer.render(n, size.width, size.height, L, W, H, before, yaw, pitch, zoom); n.restore()
            } else RoomRenderer.render(n, size.width, size.height, L, W, H, after, yaw, pitch, zoom, boxes)
        }
        if (before != null) drawLine(divider, Offset(size.width * split, 0f), Offset(size.width * split, size.height), 4f)
    }
}
