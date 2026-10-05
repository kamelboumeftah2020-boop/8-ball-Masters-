package com.fluently.english.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fluently.english.ui.components.VSpace
import com.fluently.english.ui.theme.AppTheme
import com.fluently.english.ui.theme.Coral
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
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
import com.fluently.english.ui.screens.AccountInfo
import com.fluently.english.ui.screens.ReaderListScreen
import com.fluently.english.ui.screens.ReaderScreen
import com.fluently.english.ui.screens.ReportScreen
import com.fluently.english.ui.screens.GoalScreen
import com.fluently.english.account.SyncState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.fluently.english.ui.screens.AuthScreen
import com.fluently.english.account.Session
import com.fluently.english.ui.screens.MockExamScreen
import com.fluently.english.ui.screens.MockListScreen
import com.fluently.english.ui.screens.HomeScreen
import com.fluently.english.ui.screens.LessonScreen
import com.fluently.english.ui.screens.LevelScreen
import com.fluently.english.ui.screens.MethodsScreen
import com.fluently.english.ui.screens.PathScreen
import com.fluently.english.ui.screens.PlacementScreen
import com.fluently.english.ui.screens.ProfileScreen
import com.fluently.english.ui.screens.ReviewScreen
import com.fluently.english.ui.screens.WelcomeScreen
import com.fluently.english.ui.screens.ConversationListScreen
import com.fluently.english.ui.screens.ConversationScreen
import com.fluently.english.ui.screens.DailyChallengeScreen
import com.fluently.english.ui.screens.DictationGame
import com.fluently.english.ui.screens.GrammarReferenceScreen
import com.fluently.english.ui.screens.GuideScreen
import com.fluently.english.ui.screens.MistakesScreen
import com.fluently.english.ui.screens.PracticeScreen
import com.fluently.english.ui.screens.ScrambleGame
import com.fluently.english.ui.screens.SoundListScreen
import com.fluently.english.ui.screens.SoundScreen
import com.fluently.english.ui.screens.SpeedGame
import com.fluently.english.ui.screens.VerbsScreen
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.rounded.Extension

private enum class Tab(val route: String, val label: String, val icon: ImageVector, val iconOutlined: ImageVector) {
    HOME("home", "الرئيسية", Icons.Rounded.Home, Icons.Outlined.Home),
    PATH("path", "المسار", Icons.Rounded.Map, Icons.Outlined.Map),
    PRACTICE("practice", "تدرّب", Icons.Rounded.Extension, Icons.Outlined.Extension),
    REVIEW("review", "المراجعة", Icons.Rounded.Style, Icons.Outlined.Style),
    PROFILE("profile", "حسابي", Icons.Rounded.Person, Icons.Outlined.Person),
}

@Composable
private fun BottomBar(current: Tab, dueCount: Int, onSelect: (Tab) -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Column {
            HorizontalDivider(color = AppTheme.extra.border)
            Row(
                Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 8.dp, vertical = 8.dp),
            ) {
                Tab.entries.forEach { t ->
                    val selected = t == current
                    val tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    Column(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onSelect(t) }
                            .padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            Modifier
                                .clip(CircleShape)
                                .background(if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                .padding(horizontal = 14.dp, vertical = 5.dp),
                        ) {
                            if (t == Tab.REVIEW && dueCount > 0) {
                                BadgedBox(badge = { Badge(containerColor = Coral) { Text("$dueCount") } }) {
                                    Icon(if (selected) t.icon else t.iconOutlined, null, tint = tint)
                                }
                            } else {
                                Icon(if (selected) t.icon else t.iconOutlined, null, tint = tint)
                            }
                        }
                        VSpace(4.dp)
                        Text(
                            t.label, color = tint,
                            style = if (selected) MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                            else MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FluentlyApp(vm: AppViewModel = viewModel()) {
    val session by vm.session.collectAsStateWithLifecycle()
    val current = session
    if (current == null) {
        AuthScreen(
            cloud = vm.cloudAccounts,
            onSignUp = vm::signUp,
            onSignIn = vm::signIn,
            onResetPassword = vm::sendPasswordReset,
        )
        return
    }
    // A new account gets a fresh navigation graph (and start destination).
    key(current.uid) { MainApp(vm, current) }
}

@Composable
private fun MainApp(vm: AppViewModel, session: Session) {
    val progress by vm.progress.collectAsStateWithLifecycle()
    val sync by vm.sync.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
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
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (tab != null) BottomBar(tab, dueCount) { nav.switchTab(it.route) }
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
                    name = progress.name.ifBlank { session.name },
                    goal = progress.learningGoal,
                    onGoal = vm::setGoal,
                    onPlacement = { nav.navigate("placement") },
                    onStartFromZero = {
                        vm.finishOnboarding(progress.name.ifBlank { session.name })
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
                    onDaily = { nav.navigate("daily") },
                    onAddWord = vm::addWordToReview,
                    onOpenRoute = { nav.navigate(it) },
                    onChooseGoal = { nav.navigate("goal") },
                    onReport = { nav.navigate("report") },
                )
            }
            composable(Tab.PRACTICE.route) {
                PracticeScreen(
                    progress = progress,
                    onConversations = { nav.navigate("conversations") },
                    onSounds = { nav.navigate("sounds") },
                    onScramble = { nav.navigate("game/scramble") },
                    onSpeed = { nav.navigate("game/speed") },
                    onDictation = { nav.navigate("game/dictation") },
                    onMistakes = { nav.navigate("mistakes") },
                    onVerbs = { nav.navigate("verbs") },
                    onGrammar = { nav.navigate("grammar") },
                    onMocks = { nav.navigate("mocks") },
                    onReaders = { nav.navigate("readers") },
                )
            }
            composable("readers") {
                ReaderListScreen(progress, onBack = { nav.popBackStack() }, onOpen = { nav.navigate("reader/$it") })
            }
            composable("reader/{id}") { entry ->
                val id = entry.arguments?.getString("id").orEmpty()
                ReaderScreen(
                    id = id,
                    progress = progress,
                    onComplete = { ch, words, right, total -> vm.completeReaderChapter(id, ch, words, right, total) },
                    onClose = { nav.popBackStack() },
                )
            }
            composable("goal") {
                GoalScreen(progress.learningGoal, onSelect = { vm.setGoal(it); nav.popBackStack() }, onBack = { nav.popBackStack() })
            }
            composable("report") {
                ReportScreen(progress, vm.today(), onBack = { nav.popBackStack() }, onOpen = { nav.navigate(it) })
            }
            composable("mocks") {
                MockListScreen(progress, onBack = { nav.popBackStack() }, onOpen = { nav.navigate("mock/$it") })
            }
            composable("mock/{id}") { entry ->
                val id = entry.arguments?.getString("id").orEmpty()
                MockExamScreen(
                    id = id,
                    onComplete = { score, correct, skills -> vm.completeMock(id, score, correct, skills) },
                    onClose = { nav.popBackStack() },
                )
            }
            composable("conversations") {
                ConversationListScreen(progress, onBack = { nav.popBackStack() }, onOpen = { nav.navigate("conversation/$it") })
            }
            composable("conversation/{id}") { entry ->
                ConversationScreen(
                    id = entry.arguments?.getString("id").orEmpty(),
                    onComplete = vm::completeConversation,
                    onClose = { nav.popBackStack() },
                )
            }
            composable("sounds") {
                SoundListScreen(progress, onBack = { nav.popBackStack() }, onOpen = { nav.navigate("sound/$it") })
            }
            composable("sound/{id}") { entry ->
                SoundScreen(
                    id = entry.arguments?.getString("id").orEmpty(),
                    onComplete = vm::completeSound,
                    onClose = { nav.popBackStack() },
                )
            }
            composable("game/scramble") {
                ScrambleGame(progress, onComplete = { vm.completeGame(it) }, onClose = { nav.popBackStack() })
            }
            composable("game/speed") {
                SpeedGame(progress, onComplete = { correct, score -> vm.completeGame(correct, score) }, onClose = { nav.popBackStack() })
            }
            composable("game/dictation") {
                DictationGame(progress, onComplete = { vm.completeGame(it) }, onClose = { nav.popBackStack() })
            }
            composable("mistakes") {
                MistakesScreen(progress, onResolve = vm::resolveMistakes, onClose = { nav.popBackStack() })
            }
            composable("verbs") {
                VerbsScreen(onComplete = { vm.completeGame(it) }, onClose = { nav.popBackStack() })
            }
            composable("grammar") {
                GrammarReferenceScreen(onBack = { nav.popBackStack() }, onOpen = { nav.navigate("guide/$it") })
            }
            composable("guide/{id}") { entry ->
                GuideScreen(entry.arguments?.getString("id").orEmpty(), onClose = { nav.popBackStack() })
            }
            composable("daily") {
                DailyChallengeScreen(progress, vm.today(), onComplete = vm::completeDailyChallenge, onClose = { nav.popBackStack() })
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
                    onReminderChange = vm::setReminderHour,
                    onReset = {
                        vm.reset()
                        nav.navigate("welcome") { popUpTo(0) }
                    },
                    account = AccountInfo(
                        email = session.email,
                        cloud = session.cloud,
                        status = when {
                            !session.cloud -> "حساب محفوظ على هذا الهاتف"
                            sync == SyncState.SYNCING -> "جارٍ حفظ تقدّمك…"
                            sync == SyncState.OFFLINE -> "غير متصل — سيُحفظ تقدّمك عند عودة الإنترنت"
                            else -> "تقدّمك محفوظ في حسابك ✓"
                        },
                    ),
                    onSignOut = { scope.launch { vm.signOut() } },
                    onReport = { nav.navigate("report") },
                    onLearningGoal = vm::setGoal,
                    exportBackup = vm::exportBackup,
                    importBackup = vm::importBackup,
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
                    onAnswers = vm::recordLessonAnswers,
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
