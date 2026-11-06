package com.nh.tilesmarbledesigner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.nh.tilesmarbledesigner.ui.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm: AppViewModel = viewModel()
            val settings by vm.settings.collectAsState()
            NHTheme(Palettes.byId(settings.themeId)) { App(vm) }
        }
    }
}

private data class Tab(val route: String, val label: String, val icon: ImageVector)

@Composable
fun App(vm: AppViewModel) {
    val pal = LocalPalette.current
    val nav = rememberNavController()
    val snack = remember { SnackbarHostState() }
    LaunchedEffect(vm.toast) { vm.toast?.let { snack.showSnackbar(it); vm.toast = null } }
    val route = nav.currentBackStackEntryAsState().value?.destination?.route
    val tabs = listOf(
        Tab("home", "Home", Icons.Filled.Home), Tab("measure", "Measure", Icons.Filled.Straighten),
        Tab("studio/wall", "Studio", Icons.Filled.Palette), Tab("projects", "Projects", Icons.Filled.Folder),
        Tab("settings", "Settings", Icons.Filled.Settings)
    )
    val tabRoutes = tabs.map { it.route }.toSet()
    val showBar = route != null && (route in setOf("home", "measure", "projects", "settings") || route.startsWith("studio"))
    val go: (String) -> Unit = { r ->
        if (r in tabRoutes) nav.navigate(r) { popUpTo("home"); launchSingleTop = true } else nav.navigate(r)
    }
    val back: () -> Unit = { nav.popBackStack() }

    Scaffold(
        containerColor = pal.bg,
        snackbarHost = { SnackbarHost(snack) },
        bottomBar = {
            if (showBar) NavigationBar(modifier = Modifier.softShadow(16.dp, RectangleShape), containerColor = pal.card, tonalElevation = 0.dp) {
                tabs.forEach { t ->
                    NavigationBarItem(
                        selected = route!!.substringBefore('/') == t.route.substringBefore('/'),
                        onClick = { go(t.route) }, icon = { Icon(t.icon, t.label) }, label = { Text(t.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White, selectedTextColor = pal.dark, indicatorColor = pal.dark,
                            unselectedIconColor = pal.label, unselectedTextColor = pal.label
                        )
                    )
                }
            }
        }
    ) { pad ->
        NavHost(
            nav, "splash", Modifier.padding(pad).background(pal.bg),
            enterTransition = { fadeIn(tween(220)) + slideInHorizontally(tween(220)) { it / 14 } },
            exitTransition = { fadeOut(tween(160)) },
            popEnterTransition = { fadeIn(tween(220)) },
            popExitTransition = { fadeOut(tween(160)) + slideOutHorizontally(tween(160)) { it / 14 } }
        ) {
            composable("splash") { SplashScreen { nav.navigate("home") { popUpTo("splash") { inclusive = true } } } }
            composable("home") { HomeScreen(vm, go) }
            composable("create") { CreateRoomScreen(vm, go, back) }
            composable("measure") { MeasurementScreen(vm, go) }
            composable("wall") { WallScreen(vm, back) }
            composable("floor") { FloorScreen(vm, back) }
            composable("calc") { CalculatorScreen(vm, go, back) }
            composable("results") { ResultsScreen(vm, go, back) }
            composable("library") { LibraryScreen(vm, go, back) }
            composable("studio/{mode}", listOf(navArgument("mode") { type = NavType.StringType })) {
                StudioScreen(vm, it.arguments?.getString("mode") ?: "wall", go)
            }
            composable("crop") { CropScreen(vm, go, back) }
            composable("texture") { TextureScreen(vm, go, back) }
            composable("preview") { PreviewScreen(vm, go, back) }
            composable("projects") { ProjectsScreen(vm, go) }
            composable("details/{id}", listOf(navArgument("id") { type = NavType.LongType })) {
                DetailsScreen(vm, it.arguments?.getLong("id") ?: 0L, go, back)
            }
            composable("templates") { TemplatesScreen(vm, go, back) }
            composable("furniture") { FurnitureScreen(vm, go, back) }
            composable("fullview") { FullViewScreen(vm, back) }
            composable("export") { ExportScreen(vm, back) }
            composable("settings") { SettingsScreen(vm) }
        }
    }
}
