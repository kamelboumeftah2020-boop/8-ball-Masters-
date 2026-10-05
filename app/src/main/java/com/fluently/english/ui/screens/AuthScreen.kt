package com.fluently.english.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MarkEmailUnread
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.fluently.english.account.AuthException
import com.fluently.english.account.AuthValidation
import com.fluently.english.ui.components.AppCard
import com.fluently.english.ui.components.HSpace
import com.fluently.english.ui.components.PrimaryButton
import com.fluently.english.ui.components.SecondaryButton
import com.fluently.english.ui.components.VSpace
import com.fluently.english.ui.components.ltr
import com.fluently.english.ui.theme.AppTheme
import com.fluently.english.ui.theme.Danger
import com.fluently.english.ui.theme.Success
import kotlinx.coroutines.launch

/**
 * First screen: create an account (name, email, password) or sign in, so the
 * learner's progress is saved in their own account.
 */
@Composable
fun AuthScreen(
    cloud: Boolean,
    onSignUp: suspend (name: String, email: String, password: String) -> Unit,
    onSignIn: suspend (email: String, password: String) -> Unit,
    onResetPassword: suspend (email: String) -> Unit,
    onGuest: () -> Unit = {},
) {
    var confirmGuest by rememberSaveable { mutableStateOf(false) }
    var signUp by rememberSaveable { mutableStateOf(true) }
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var info by rememberSaveable { mutableStateOf<String?>(null) }
    var busy by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun submit() {
        error = if (signUp) AuthValidation.signUpError(name, email, password, confirm) else AuthValidation.signInError(email, password)
        info = null
        if (error != null) return
        busy = true
        scope.launch {
            try {
                if (signUp) onSignUp(name, email, password) else onSignIn(email, password)
            } catch (e: AuthException) {
                error = e.message
            } finally {
                busy = false
            }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 24.dp),
    ) {
        VSpace(28.dp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            BrandMark(40.dp)
            HSpace(12.dp)
            Column {
                Text("طلاقة", style = MaterialTheme.typography.titleLarge)
                Text("Fluently", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        VSpace(32.dp)
        Text(
            if (signUp) "أنشئ حسابك" else "أهلاً بعودتك",
            style = MaterialTheme.typography.headlineMedium,
        )
        VSpace(6.dp)
        Text(
            if (signUp) "حسابك يحفظ تقدّمك ومستواك ونقاطك، فلا يضيع شيء مما تعلمته."
            else "سجّل الدخول لتكمل من حيث توقفت.",
            style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        VSpace(22.dp)

        // Segmented switch between the two forms.
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(AppTheme.extra.subtle).padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            listOf(true to "حساب جديد", false to "تسجيل الدخول").forEach { (isSignUp, label) ->
                val selected = signUp == isSignUp
                Box(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selected) MaterialTheme.colorScheme.surface else Color.Transparent)
                        .clickable { signUp = isSignUp; error = null; info = null }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        label, style = MaterialTheme.typography.titleSmall,
                        color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        VSpace(18.dp)

        if (signUp) {
            Field(name, { name = it }, "الاسم", Icons.Rounded.Person, KeyboardType.Text, ltr = false)
            VSpace(12.dp)
        }
        Field(email, { email = it.trim() }, "البريد الإلكتروني", Icons.Rounded.Email, KeyboardType.Email)
        AuthValidation.suggestion(email)?.let { fixed ->
            Text(
                "هل تقصد ${ltr(fixed)}؟ اضغط للتصحيح",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 6.dp).clip(RoundedCornerShape(10.dp)).clickable { email = fixed }.padding(6.dp),
            )
        }
        VSpace(12.dp)
        PasswordField(password, { password = it }, "كلمة السر", last = !signUp, onDone = ::submit)
        if (signUp) {
            VSpace(12.dp)
            PasswordField(confirm, { confirm = it }, "تأكيد كلمة السر", last = true, onDone = ::submit)
        }

        error?.let {
            VSpace(12.dp)
            Text(it, style = MaterialTheme.typography.bodyMedium, color = Danger)
        }
        info?.let {
            VSpace(12.dp)
            Text(it, style = MaterialTheme.typography.bodyMedium, color = Success)
        }
        VSpace(20.dp)
        if (busy) {
            Box(Modifier.fillMaxWidth().padding(vertical = 14.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(Modifier.size(28.dp), strokeWidth = 3.dp)
            }
        } else {
            PrimaryButton(
                if (signUp) "إنشاء الحساب" else "دخول",
                onClick = ::submit,
                icon = Icons.AutoMirrored.Rounded.ArrowForward,
            )
        }
        if (!signUp && cloud) {
            VSpace(6.dp)
            Text(
                "نسيت كلمة السر؟",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable {
                    if (AuthValidation.signInError(email, "x") != null) {
                        error = "اكتب بريدك الإلكتروني أولاً"
                        return@clickable
                    }
                    scope.launch {
                        try {
                            onResetPassword(email)
                            error = null
                            info = "أرسلنا رابط تغيير كلمة السر إلى بريدك"
                        } catch (e: AuthException) {
                            error = e.message
                        }
                    }
                }.padding(12.dp),
            )
        }
        VSpace(4.dp)
        Text(
            "تخطَّ الآن وادخل كضيف",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { confirmGuest = true }.padding(12.dp),
        )
        VSpace(12.dp)
        AppCard(color = AppTheme.extra.subtle, bordered = false, padding = 14.dp) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (cloud) Icons.Rounded.Cloud else Icons.Rounded.PhoneAndroid, null,
                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp),
                )
                HSpace(10.dp)
                Text(
                    if (cloud) "يُحفظ تقدّمك في حسابك على الإنترنت، فتجده كما هو على أي هاتف تسجّل منه."
                    else "يُحفظ الحساب وتقدّمك على هذا الهاتف. احفظ نسخة احتياطية من صفحة «حسابي» عند تغيير الهاتف.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        VSpace(28.dp)
    }

    if (confirmGuest) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { confirmGuest = false },
            title = { Text("الدخول كضيف") },
            text = {
                Text(
                    "يمكنك استخدام التطبيق كاملاً، لكن تقدّمك لن يُحفظ في حساب: إذا حذفت التطبيق أو غيّرت هاتفك ستفقد كل ما تعلمته، ولن تظهر في ترتيب المتعلمين.\n\nيمكنك إنشاء حساب في أي وقت من صفحة «حسابي»، وسيُنقل إليه تقدّمك.",
                )
            },
            confirmButton = { androidx.compose.material3.TextButton(onClick = { confirmGuest = false; onGuest() }) { Text("متابعة كضيف") } },
            dismissButton = { androidx.compose.material3.TextButton(onClick = { confirmGuest = false }) { Text("إنشاء حساب") } },
        )
    }
}

@Composable
private fun Field(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    type: KeyboardType,
    ltr: Boolean = true,
) {
    TextBox(value, onChange, label, icon, KeyboardOptions(keyboardType = type, imeAction = ImeAction.Next), ltr)
}

@Composable
private fun PasswordField(value: String, onChange: (String) -> Unit, label: String, last: Boolean, onDone: () -> Unit) {
    var visible by rememberSaveable { mutableStateOf(false) }
    TextBox(
        value, onChange, label, Icons.Rounded.Lock,
        KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = if (last) ImeAction.Done else ImeAction.Next),
        ltr = true,
        transformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        onDone = if (last) onDone else null,
        trailing = {
            IconButton(onClick = { visible = !visible }) {
                Icon(if (visible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, "إظهار كلمة السر")
            }
        },
    )
}

@Composable
private fun TextBox(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    keyboard: KeyboardOptions,
    ltr: Boolean,
    transformation: VisualTransformation = VisualTransformation.None,
    onDone: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null) },
        trailingIcon = trailing,
        singleLine = true,
        visualTransformation = transformation,
        keyboardOptions = keyboard,
        keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { onDone?.invoke() }),
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            textDirection = if (ltr) androidx.compose.ui.text.style.TextDirection.Ltr else androidx.compose.ui.text.style.TextDirection.Content,
        ),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = AppTheme.extra.border,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}
