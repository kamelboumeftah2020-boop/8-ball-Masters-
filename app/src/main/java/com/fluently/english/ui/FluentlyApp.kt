package com.fluently.english.ui

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Route
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.fluently.english.data.content.CefrLevel
import com.fluently.english.tts.LocalSpeaker
import com.fluently.english.ui.screens.ExamScreen
import com.fluently.english.ui.screens.HomeScreen
import com.fluently.english.ui.screens.LessonScreen
import com.fluently.english.ui.screens.LevelScreen
import com.fluently.english.ui.screens.MethodsScreen
import com.fluently.english.ui.screens.PathScreen
import com.fluently.english.ui.screens.PlacementScreen
import com.fluently.english.ui.screens.ProfileScreen
import com.fluently.english.ui.screens.ReviewScreen
import com.fluently.english.ui.screens.WelcomeScreen

private enum class Tab(val route: String, val label: String, val icon: ImageVector) {
    HOME("home", "الرئيسية", Icons.Rounded.Home),
    PATH("path", "المسار", Icons.Rounded.Route),
    REVIEW("review", "المراجعة", Icons.Rounded.Style),
    PROFILE("profile", "حسابي", Icons.Rounded.Person),
}

@Composable
fun FluentlyApp(vm: AppViewModel = viewModel()) {
    val progress by vm.progress.collectAsStateWithLifecycle()
    val speaker = LocalSpeaker.current
    LaunchedEffect(progress.speechRate) { speaker.baseRate = progress.speechRate }

    val nav = rememberNavController()
    // Fixed for the lifetime of the NavHost; changing it would rebuild the graph.
    val startDestination = remember { if (progress.onboarded) Tab.HOME.route else "welcome" }
    val backStack by nav.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val tab = Tab.entries.firstOrNull { it.route == route }
    val dueCount = progress.dueCards(vm.today()).size

    Scaffold(
        bottomBar = {
            if (tab != null) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    Tab.entries.forEach { t ->
                        NavigationBarItem(
                            selected = t == tab,
                            onClick = { nav.switchTab(t.route) },
                            icon = {
                                if (t == Tab.REVIEW && dueCount > 0) {
                                    BadgedBox(badge = { Badge { Text("$dueCount") } }) { Icon(t.icon, null) }
                                } else {
                                    Icon(t.icon, null)
                                }
                            },
                            label = { Text(t.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = startDestination,
            modifier = Modifier.padding(padding).consumeWindowInsets(padding),
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() },
        ) {
            composable("welcome") {
                WelcomeScreen(
                    onPlacement = { name ->
                        vm.setName(name)
                        nav.navigate("placement")
                    },
                    onStartFromZero = { name ->
                        vm.finishOnboarding(name)
                        nav.navigate(Tab.HOME.route) { popUpTo("welcome") { inclusive = true } }
                    },
                )
            }
            composable(Tab.HOME.route) {
                HomeScreen(
                    progress = progress,
                    dueCount = dueCount,
                    onOpenLesson = { nav.navigate("lesson/$it") },
                    onOpenExam = { nav.navigate("exam/${it.name}") },
                    onOpenLevel = { nav.navigate("level/${it.name}") },
                    onReview = { nav.switchTab(Tab.REVIEW.route) },
                    onPlacement = { nav.navigate("placement") },
                )
            }
            composable(Tab.PATH.route) {
                PathScreen(progress = progress, onOpenLevel = { nav.navigate("level/${it.name}") })
            }
            composable(Tab.REVIEW.route) {
                ReviewScreen(
                    progress = progress,
                    today = vm.today(),
                    onReview = vm::reviewCard,
                    onGoToPath = { nav.switchTab(Tab.PATH.route) },
                )
            }
            composable(Tab.PROFILE.route) {
                ProfileScreen(
                    progress = progress,
                    onNameChange = vm::setName,
                    onGoalChange = vm::setDailyGoal,
                    onSpeechRateChange = vm::setSpeechRate,
                    onPlacement = { nav.navigate("placement") },
                    onMethods = { nav.navigate("methods") },
                    onReset = {
                        vm.reset()
                        nav.navigate("welcome") { popUpTo(0) }
                    },
                )
            }
            composable("level/{code}") { entry ->
                val level = CefrLevel.valueOf(entry.arguments?.getString("code") ?: "A1")
                LevelScreen(
                    level = level,
                    progress = progress,
                    onBack = { nav.popBackStack() },
                    onOpenLesson = { nav.navigate("lesson/$it") },
                    onOpenExam = { nav.navigate("exam/${level.name}") },
                )
            }
            composable("lesson/{id}") { entry ->
                LessonScreen(
                    lessonId = entry.arguments?.getString("id").orEmpty(),
                    onComplete = vm::completeLesson,
                    onClose = { nav.popBackStack() },
                )
            }
            composable("exam/{code}") { entry ->
                val level = CefrLevel.valueOf(entry.arguments?.getString("code") ?: "A1")
                ExamScreen(
                    level = level,
                    onComplete = { correct, total -> vm.completeExam(level, correct, total) },
                    onClose = { nav.popBackStack() },
                )
            }
            composable("placement") {
                PlacementScreen(
                    onApply = vm::applyPlacement,
                    onClose = {
                        if (!nav.popBackStack(Tab.HOME.route, inclusive = false)) {
                            nav.navigate(Tab.HOME.route) { popUpTo(0) }
                        }
                    },
                )
            }
            composable("methods") {
                MethodsScreen(onBack = { nav.popBackStack() })
            }
        }
    }
}

private fun NavHostController.switchTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
