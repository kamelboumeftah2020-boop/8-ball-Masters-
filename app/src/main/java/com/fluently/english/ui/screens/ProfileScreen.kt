package com.fluently.english.ui.screens

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Login
import androidx.compose.material.icons.rounded.MarkEmailRead
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.LayoutDirection
import com.fluently.english.account.AuthException
import com.fluently.english.account.AuthValidation
import kotlinx.coroutines.launch

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.ui.platform.LocalContext
import com.fluently.english.data.progress.LearningGoal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Hearing
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fluently.english.data.content.CefrLevel
import com.fluently.english.data.content.LearningMethods
import com.fluently.english.data.progress.Progress
import com.fluently.english.data.progress.achievements
import com.fluently.english.tts.LocalSpeaker
import com.fluently.english.ui.components.AppCard
import com.fluently.english.ui.components.CircleIconButton
import com.fluently.english.ui.components.HSpace
import com.fluently.english.ui.components.IconTile
import com.fluently.english.ui.components.LevelBadge
import com.fluently.english.ui.components.Pill
import com.fluently.english.ui.components.ScreenHeader
import com.fluently.english.ui.components.SectionHeader
import com.fluently.english.ui.components.StatItem
import com.fluently.english.ui.components.VSpace
import com.fluently.english.ui.components.ltr
import com.fluently.english.ui.components.SecondaryButton
import com.fluently.english.ui.components.isolateLatin
import com.fluently.english.ui.theme.AppTheme
import com.fluently.english.ui.theme.Coral
import com.fluently.english.ui.theme.Danger
import com.fluently.english.ui.theme.Emerald
import com.fluently.english.ui.theme.Gold
import com.fluently.english.ui.theme.color

private fun achievementIcon(key: String): ImageVector = when (key) {
    "start" -> Icons.Rounded.Flag
    "compass" -> Icons.Rounded.Explore
    "flame" -> Icons.Rounded.LocalFireDepartment
    "book" -> Icons.Rounded.Style
    "brain" -> Icons.Rounded.Psychology
    "bolt" -> Icons.Rounded.Bolt
    "cap" -> Icons.Rounded.School
    "chat" -> Icons.Rounded.Forum
    "ear" -> Icons.Rounded.Hearing
    "game" -> Icons.Rounded.SportsEsports
    "reader" -> Icons.AutoMirrored.Rounded.MenuBook
    "pen" -> Icons.Rounded.EditNote
    else -> Icons.Rounded.EmojiEvents
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    progress: Progress,
    onNameChange: (String) -> Unit,
    onGoalChange: (Int) -> Unit,
    onSpeechRateChange: (Float) -> Unit,
    onPlacement: () -> Unit,
    onMethods: () -> Unit,
    onReset: () -> Unit,
    onReminderChange: (Int) -> Unit = {},
    account: AccountInfo? = null,
    accountActions: AccountActions = AccountActions(),
    onSignOut: () -> Unit = {},
    onReport: () -> Unit = {},
    onLeaderboard: () -> Unit = {},
    onPrivacy: () -> Unit = {},
    onLearningGoal: (LearningGoal) -> Unit = {},
    exportBackup: () -> String = { "" },
    importBackup: (String) -> Boolean = { false },
) {
    var editingName by remember { mutableStateOf(false) }
    var confirmSignOut by remember { mutableStateOf(false) }
    var changingEmail by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }
    var accountMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(account?.verified) {
        if (account != null && account.cloud && !account.guest && !account.verified) runCatching { accountActions.checkVerified() }
    }
    var pickingGoal by remember { mutableStateOf(false) }
    var backupMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val exporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            backupMessage = runCatching {
                context.contentResolver.openOutputStream(uri)?.use { it.write(exportBackup().toByteArray()) }
                "تم حفظ النسخة الاحتياطية ✓"
            }.getOrDefault("تعذّر حفظ الملف")
        }
    }
    val importer = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val raw = runCatching { context.contentResolver.openInputStream(uri)?.use { it.readBytes().decodeToString() } }.getOrNull()
            backupMessage = if (raw != null && importBackup(raw)) "تمت استعادة تقدّمك ✓" else "الملف ليس نسخة احتياطية صالحة"
        }
    }
    var confirmReset by remember { mutableStateOf(false) }
    val border = AppTheme.extra.border

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        VSpace(20.dp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(64.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    progress.name.firstOrNull()?.uppercase() ?: "ط",
                    color = Color.White, style = MaterialTheme.typography.headlineSmall,
                )
            }
            HSpace(16.dp)
            Column(Modifier.weight(1f)) {
                Text(progress.name.ifBlank { "متعلم طموح" }, style = MaterialTheme.typography.headlineSmall)
                VSpace(4.dp)
                Pill(
                    "المستوى ${progress.currentLevel.code} · ${progress.currentLevel.titleAr}",
                    progress.currentLevel.color(),
                )
            }
            CircleIconButton(Icons.Rounded.Edit, { editingName = true })
        }

        VSpace(24.dp)
        AppCard(padding = 20.dp) {
            Row {
                StatItem(Icons.Rounded.Bolt, Gold, "${progress.xp}", "نقطة", Modifier.weight(1f))
                StatItem(Icons.Rounded.LocalFireDepartment, Coral, "${progress.streak}", "سلسلة", Modifier.weight(1f))
                StatItem(Icons.Rounded.EmojiEvents, Gold, "${progress.bestStreak}", "أطول سلسلة", Modifier.weight(1f))
            }
            VSpace(20.dp)
            Row {
                StatItem(Icons.AutoMirrored.Rounded.MenuBook, Emerald, "${progress.lessonsCompleted}", "درس", Modifier.weight(1f))
                StatItem(Icons.Rounded.Style, Emerald, "${progress.wordsLearned}", "كلمة", Modifier.weight(1f))
                StatItem(
                    Icons.Rounded.WorkspacePremium, Emerald,
                    "${CefrLevel.entries.count { progress.isLevelPassed(it) }}", "شهادة", Modifier.weight(1f),
                )
            }
        }

        if (account != null) {
            SectionHeader("حسابي")
            AppCard(padding = 0.dp) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconTile(
                        when {
                            account.guest -> Icons.Rounded.PersonOutline
                            account.cloud -> Icons.Rounded.Cloud
                            else -> Icons.Rounded.PhoneAndroid
                        },
                        if (account.guest || account.expired) Gold else MaterialTheme.colorScheme.primary, size = 40.dp,
                    )
                    HSpace(12.dp)
                    Column(Modifier.weight(1f)) {
                        if (!account.guest) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(ltr(account.email), style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f, fill = false))
                                if (account.cloud) {
                                    HSpace(6.dp)
                                    Pill(if (account.verified) "مؤكَّد ✓" else "غير مؤكَّد", if (account.verified) Emerald else Gold)
                                }
                            }
                        } else {
                            Text("ضيف", style = MaterialTheme.typography.titleSmall)
                        }
                        Text(account.status, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                accountMessage?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp))
                }
                HorizontalDivider(color = border)
                when {
                    account.guest -> SettingRow(Icons.Rounded.PersonAdd, Emerald, "إنشاء حساب أو تسجيل الدخول (يُنقل تقدّمك)", accountActions.createAccount)
                    account.expired -> SettingRow(Icons.Rounded.Login, Gold, "سجّل الدخول من جديد", accountActions.signInAgain)
                    else -> {
                        if (account.cloud && !account.verified) {
                            SettingRow(Icons.Rounded.MarkEmailRead, Emerald, "تأكيد البريد الإلكتروني (اختياري)", {
                                scope.launch {
                                    accountMessage = try {
                                        accountActions.sendVerification()
                                        "أرسلنا رابط التأكيد إلى بريدك (تحقق من مجلد Spam أيضاً)."
                                    } catch (e: AuthException) { e.message }
                                }
                            })
                            HorizontalDivider(color = border)
                        }
                        SettingRow(Icons.Rounded.AlternateEmail, MaterialTheme.colorScheme.primary, "تغيير البريد الإلكتروني", { changingEmail = true })
                        HorizontalDivider(color = border)
                        SettingRow(Icons.AutoMirrored.Rounded.Logout, Danger, "تسجيل الخروج", { confirmSignOut = true }, textColor = Danger)
                    }
                }
                if (!account.expired) {
                    HorizontalDivider(color = border)
                    SettingRow(
                        Icons.Rounded.DeleteForever, Danger,
                        if (account.guest) "حذف بياناتي من هذا الهاتف" else "حذف الحساب نهائياً",
                        { deleting = true }, textColor = Danger,
                    )
                }
            }
        }

        SectionHeader("تقدّمي")
        AppCard(padding = 0.dp) {
            SettingRow(Icons.Rounded.Insights, Emerald, "تقريري الأسبوعي ونقاط ضعفي", onReport)
            HorizontalDivider(color = border)
            SettingRow(Icons.Rounded.EmojiEvents, Gold, "ترتيب المتعلمين هذا الأسبوع", onLeaderboard)
            HorizontalDivider(color = border)
            SettingRow(
                progress.learningGoal?.icon() ?: Icons.Rounded.Flag, Coral,
                "هدفي: ${progress.learningGoal?.titleAr ?: "لم أختر بعد"}", { pickingGoal = true },
            )
            HorizontalDivider(color = border)
            SettingRow(Icons.Rounded.Download, Gold, "حفظ نسخة احتياطية في ملف", { exporter.launch("fluently-backup.json") })
            HorizontalDivider(color = border)
            SettingRow(Icons.Rounded.Upload, Gold, "استعادة من نسخة احتياطية", { importer.launch(arrayOf("application/json", "text/plain", "application/octet-stream")) })
            backupMessage?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 14.dp))
            }
        }

        SectionHeader("الشهادات")
        AppCard {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                CefrLevel.entries.forEach { level ->
                    LevelBadge(level, size = 44.dp, locked = !progress.isLevelPassed(level), filled = progress.isLevelPassed(level))
                }
            }
            if (progress.placementTaken) {
                VSpace(14.dp)
                Text(
                    "نتيجة اختبار تحديد المستوى: ${ltr(progress.placementLevel?.code ?: "Pre-A1")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        SectionHeader("الإنجازات")
        val achievements = progress.achievements()
        Text(
            "${achievements.count { it.unlocked }} من ${achievements.size}",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            maxItemsInEachRow = 4,
        ) {
            achievements.forEach { a ->
                Column(
                    Modifier.weight(1f).alpha(if (a.unlocked) 1f else 0.38f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    IconTile(
                        achievementIcon(a.icon),
                        if (a.unlocked) Gold else MaterialTheme.colorScheme.onSurfaceVariant,
                        size = 52.dp,
                    )
                    VSpace(6.dp)
                    Text(a.title, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
                    Text(
                        a.description, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        SectionHeader("الإعدادات")
        AppCard(padding = 0.dp) {
            Column(Modifier.padding(16.dp)) {
                Text("الهدف اليومي", style = MaterialTheme.typography.titleSmall)
                VSpace(10.dp)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(20 to "خفيف", 50 to "متوسط", 100 to "جاد", 200 to "مكثف").forEach { (goal, label) ->
                        FilterChip(
                            selected = progress.dailyGoal == goal,
                            onClick = { onGoalChange(goal) },
                            label = { Text("$label · $goal") },
                            shape = CircleShape,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White,
                            ),
                        )
                    }
                }
            }
            HorizontalDivider(color = border)
            ReminderSetting(progress.reminderHour, onReminderChange)
            HorizontalDivider(color = border)
            SpeechRateSetting(progress.speechRate, onSpeechRateChange)
            HorizontalDivider(color = border)
            SettingRow(Icons.Rounded.Explore, Emerald, "إعادة اختبار تحديد المستوى", onPlacement)
            HorizontalDivider(color = border)
            SettingRow(Icons.AutoMirrored.Rounded.MenuBook, Gold, "المنهجية والمصادر العالمية", onMethods)
            HorizontalDivider(color = border)
            SettingRow(Icons.Rounded.PrivacyTip, MaterialTheme.colorScheme.primary, "سياسة الخصوصية", onPrivacy)
            HorizontalDivider(color = border)
            var personalized by remember { mutableStateOf(com.fluently.english.ads.Ads.personalized(context)) }
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("إعلانات مخصصة", style = MaterialTheme.typography.titleSmall)
                    Text("عند الإيقاف تظهر إعلانات عامة غير مرتبطة باهتماماتك", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                androidx.compose.material3.Switch(checked = personalized, onCheckedChange = {
                    personalized = it
                    com.fluently.english.ads.Ads.setPersonalized(context, it)
                })
            }
            HorizontalDivider(color = border)
            SettingRow(Icons.Rounded.DeleteOutline, Danger, "إعادة ضبط التقدم", { confirmReset = true }, textColor = Danger)
        }
        VSpace(28.dp)
    }

    if (editingName) {
        var text by remember { mutableStateOf(progress.name) }
        AlertDialog(
            onDismissRequest = { editingName = false },
            title = { Text("اسمك") },
            text = { OutlinedTextField(text, { text = it }, singleLine = true) },
            confirmButton = { TextButton(onClick = { onNameChange(text); editingName = false }) { Text("حفظ") } },
            dismissButton = { TextButton(onClick = { editingName = false }) { Text("إلغاء") } },
        )
    }
    if (confirmSignOut) {
        AlertDialog(
            onDismissRequest = { confirmSignOut = false },
            title = { Text("تسجيل الخروج؟") },
            text = {
                Text(
                    if (account?.cloud == true) "تقدّمك محفوظ في حسابك، وستجده عند تسجيل الدخول مرة أخرى."
                    else "تقدّمك محفوظ في حسابك على هذا الهاتف، وستجده عند تسجيل الدخول مرة أخرى.",
                )
            },
            confirmButton = { TextButton(onClick = { confirmSignOut = false; onSignOut() }) { Text("خروج", color = Danger) } },
            dismissButton = { TextButton(onClick = { confirmSignOut = false }) { Text("إلغاء") } },
        )
    }
    if (deleting && account != null) {
        var password by remember { mutableStateOf("") }
        var error by remember { mutableStateOf<String?>(null) }
        var busy by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { if (!busy) deleting = false },
            title = { Text(if (account.guest) "حذف بياناتك؟" else "حذف الحساب نهائياً؟") },
            text = {
                Column {
                    Text(
                        if (account.guest) "سيُحذف كل تقدّمك من هذا الهاتف. لا يمكن التراجع."
                        else "سيُحذف حسابك وكل تقدّمك ونقاطك وشهاداتك من الخادم ومن هذا الهاتف، ويُزال اسمك من الترتيب. لا يمكن التراجع عن هذا.",
                    )
                    if (!account.guest) {
                        VSpace(10.dp)
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            OutlinedTextField(password, { password = it; error = null }, label = { Text("Password") }, singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))
                        }
                        Text("اكتب كلمة السر للتأكيد.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    error?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = Danger) }
                }
            },
            confirmButton = {
                TextButton(enabled = !busy, onClick = {
                    if (!account.guest && password.isEmpty()) { error = "اكتب كلمة السر"; return@TextButton }
                    busy = true
                    scope.launch {
                        try {
                            accountActions.deleteAccount(password)
                            deleting = false
                        } catch (e: AuthException) {
                            error = e.message
                        } finally {
                            busy = false
                        }
                    }
                }) { Text(if (busy) "…" else "حذف نهائياً", color = Danger) }
            },
            dismissButton = { TextButton(enabled = !busy, onClick = { deleting = false }) { Text("إلغاء") } },
        )
    }
    if (changingEmail && account != null) {
        var newEmail by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var error by remember { mutableStateOf<String?>(null) }
        var busy by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { if (!busy) changingEmail = false },
            title = { Text("تغيير البريد الإلكتروني") },
            text = {
                Column {
                    Text("البريد الحالي: ${ltr(account.email)}", style = MaterialTheme.typography.bodySmall)
                    VSpace(10.dp)
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Column {
                            OutlinedTextField(newEmail, { newEmail = it.trim(); error = null }, label = { Text("New email") }, singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
                            VSpace(8.dp)
                            OutlinedTextField(password, { password = it; error = null }, label = { Text("Password") }, singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))
                        }
                    }
                    AuthValidation.suggestion(newEmail)?.let { fixed ->
                        Text("هل تقصد ${ltr(fixed)}؟", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 6.dp).clickable { newEmail = fixed })
                    }
                    VSpace(6.dp)
                    Text("للأمان نحتاج كلمة السر الحالية.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    error?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = Danger) }
                }
            },
            confirmButton = {
                TextButton(enabled = !busy, onClick = {
                    AuthValidation.emailError(newEmail)?.let { error = it; return@TextButton }
                    if (password.isEmpty()) { error = "اكتب كلمة السر"; return@TextButton }
                    busy = true
                    scope.launch {
                        try {
                            val pending = accountActions.changeEmail(newEmail, password)
                            accountMessage = if (pending) "أرسلنا رابطاً إلى ${ltr(newEmail)} — سيتغيّر بريدك بعد الضغط عليه، ثم سجّل الدخول بالبريد الجديد."
                            else "تم تغيير بريدك ✓"
                            changingEmail = false
                        } catch (e: AuthException) {
                            error = e.message
                        } finally {
                            busy = false
                        }
                    }
                }) { Text(if (busy) "…" else "تغيير") }
            },
            dismissButton = { TextButton(enabled = !busy, onClick = { changingEmail = false }) { Text("إلغاء") } },
        )
    }
    if (pickingGoal) {
        AlertDialog(
            onDismissRequest = { pickingGoal = false },
            title = { Text("لماذا تتعلم الإنجليزية؟") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    GoalPicker(progress.learningGoal) { onLearningGoal(it); pickingGoal = false }
                }
            },
            confirmButton = { TextButton(onClick = { pickingGoal = false }) { Text("إغلاق") } },
        )
    }
    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("إعادة ضبط التقدم؟") },
            text = { Text("سيتم حذف جميع الدروس المكتملة والنقاط والكلمات والشهادات. لا يمكن التراجع عن هذا.") },
            confirmButton = {
                TextButton(onClick = { confirmReset = false; onReset() }) { Text("حذف كل شيء", color = Danger) }
            },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("إلغاء") } },
        )
    }
}

@Composable
private fun SpeechRateSetting(rate: Float, onChange: (Float) -> Unit) {
    val speaker = LocalSpeaker.current
    var value by remember(rate) { mutableFloatStateOf(rate) }
    Column(Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("سرعة النطق", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            Pill("×${"%.1f".format(java.util.Locale.US, value)}", MaterialTheme.colorScheme.primary)
        }
        Slider(
            value = value,
            onValueChange = { value = it },
            valueRange = 0.5f..1.3f,
            steps = 7,
            onValueChangeFinished = {
                onChange(value)
                speaker.baseRate = value
                speaker.speak("This is how I sound now.")
            },
        )
        Row {
            Text("أبطأ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
            Text("أسرع", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SettingRow(icon: ImageVector, tint: Color, title: String, onClick: () -> Unit, textColor: Color = Color.Unspecified) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconTile(icon, tint, size = 36.dp)
        HSpace(14.dp)
        Text(title, style = MaterialTheme.typography.titleSmall, color = textColor, modifier = Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun MethodsScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        ScreenHeader("المنهجية والمصادر", onBack = onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            VSpace(8.dp)
            Text(
                "صُمم «طلاقة» بالاعتماد على أشهر المعايير والطرق العلمية في تعليم الإنجليزية حول العالم.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            VSpace(12.dp)
            LearningMethods.forEachIndexed { i, (title, body) ->
                AppCard(modifier = Modifier.padding(vertical = 6.dp)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                            Modifier.size(28.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center,
                        ) { Text("${i + 1}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary) }
                        HSpace(12.dp)
                        Column {
                            Text(title, style = MaterialTheme.typography.titleSmall)
                            VSpace(4.dp)
                            Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            SectionHeader("جدول المستويات")
            AppCard(padding = 0.dp) {
                CefrLevel.entries.forEachIndexed { i, level ->
                    if (i > 0) HorizontalDivider(color = AppTheme.extra.border)
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                        LevelBadge(level, filled = true)
                        HSpace(12.dp)
                        Column(Modifier.weight(1f)) {
                            Text("${level.titleAr} · ${ltr(level.cambridge)}", style = MaterialTheme.typography.titleSmall)
                            Text(level.canDoAr, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            VSpace(4.dp)
                            Text(
                                "IELTS ${ltr(level.ielts)} · TOEFL ${ltr(level.toefl)}",
                                style = MaterialTheme.typography.labelSmall, color = level.color(),
                            )
                        }
                    }
                }
            }
            VSpace(28.dp)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReminderSetting(hour: Int, onChange: (Int) -> Unit) {
    var pending by remember { mutableStateOf(-1) }
    val permission = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
    ) { granted -> if (granted) onChange(pending) }
    fun choose(h: Int) {
        if (h >= 0 && android.os.Build.VERSION.SDK_INT >= 33) {
            pending = h
            permission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        } else {
            onChange(h)
        }
    }
    Column(Modifier.padding(16.dp)) {
        Text("تذكير يومي", style = MaterialTheme.typography.titleSmall)
        Text(
            "إشعار لطيف في الوقت الذي تختاره — فقط في الأيام التي لم تدرس فيها بعد.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        VSpace(10.dp)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(-1 to "إيقاف", 8 to "8 صباحاً", 13 to "1 ظهراً", 18 to "6 مساءً", 21 to "9 مساءً").forEach { (h, label) ->
                FilterChip(
                    selected = hour == h,
                    onClick = { choose(h) },
                    label = { Text(label) },
                    shape = CircleShape,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White,
                    ),
                )
            }
        }
    }
}

/** What the profile shows about the signed-in account. */
data class AccountInfo(
    val email: String,
    val cloud: Boolean,
    val status: String,
    val guest: Boolean = false,
    val verified: Boolean = true,
    val expired: Boolean = false,
)

/** Account operations the profile can trigger. */
class AccountActions(
    val sendVerification: suspend () -> Unit = {},
    val checkVerified: suspend () -> Boolean = { true },
    /** Returns true when the change waits for a link sent to the new address. */
    val changeEmail: suspend (newEmail: String, password: String) -> Boolean = { _, _ -> false },
    val createAccount: () -> Unit = {},
    val signInAgain: () -> Unit = {},
    /** Deletes the account (password required; ignored for guests). */
    val deleteAccount: suspend (password: String) -> Unit = {},
)

@Composable
fun PrivacyScreen(onBack: () -> Unit) {
    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState()),
    ) {
        ScreenHeader("سياسة الخصوصية", onBack = onBack, subtitle = "آخر تحديث: ${com.fluently.english.data.content.PRIVACY_UPDATED}")
        Column(Modifier.padding(horizontal = 20.dp)) {
            com.fluently.english.data.content.PrivacySections.forEach { (title, body) ->
                SectionHeader(title)
                Text(isolateLatin(body), style = MaterialTheme.typography.bodyMedium)
            }
            VSpace(20.dp)
            val context = LocalContext.current
            SecondaryButton("فتح النسخة المنشورة على الإنترنت", onClick = {
                runCatching {
                    context.startActivity(
                        android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(com.fluently.english.data.content.PRIVACY_URL)),
                    )
                }
            })
            VSpace(10.dp)
            SecondaryButton("راسلنا بخصوص بياناتك", onClick = {
                runCatching {
                    context.startActivity(
                        android.content.Intent(
                            android.content.Intent.ACTION_SENDTO,
                            android.net.Uri.parse("mailto:${com.fluently.english.data.content.PRIVACY_CONTACT}"),
                        ).putExtra(android.content.Intent.EXTRA_SUBJECT, "طلاقة — طلب بخصوص بياناتي"),
                    )
                }
            })
            VSpace(28.dp)
        }
    }
}
