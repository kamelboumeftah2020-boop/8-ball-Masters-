package com.fluently.english.ui.components

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.fluently.english.tts.LocalSpeaker
import com.fluently.english.tts.VoiceStatus
import com.fluently.english.ui.theme.Danger

/**
 * Explains — and helps fix — a phone that cannot speak English, instead of the
 * app silently playing nothing. Renders nothing when the voice works.
 */
@Composable
fun VoiceWarning(modifier: Modifier = Modifier) {
    val speaker = LocalSpeaker.current
    val status = speaker.status
    if (status == VoiceStatus.READY || status == VoiceStatus.LOADING) return
    val context = LocalContext.current

    fun open(vararg intents: Intent) {
        for (intent in intents) {
            try {
                context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                return
            } catch (e: ActivityNotFoundException) {
                // try the next option
            }
        }
    }

    AppCard(modifier = modifier, color = Danger.copy(alpha = 0.08f), bordered = false, padding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.AutoMirrored.Rounded.VolumeOff, null, tint = Danger, modifier = Modifier.size(22.dp))
            HSpace(10.dp)
            Text("الصوت الإنجليزي غير متاح في هاتفك", style = MaterialTheme.typography.titleSmall)
        }
        VSpace(6.dp)
        Text(
            if (status == VoiceStatus.NO_ENGINE) {
                "لا يوجد محرك لتحويل النص إلى كلام، لذلك لن تسمع الكلمات والجمل. ثبّت «Speech Services by Google» (مجاني) ثم ارجع واضغط «تحقّق مرة أخرى»."
            } else {
                "محرك الصوت في هاتفك لا يحتوي على الإنجليزية، لذلك لن تسمع الكلمات والجمل. نزّل صوت English (US) ثم اضغط «تحقّق مرة أخرى»."
            },
            style = MaterialTheme.typography.bodySmall,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            TextButton(onClick = {
                if (status == VoiceStatus.NO_ENGINE) {
                    open(
                        Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.tts")),
                        Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.tts")),
                    )
                } else {
                    open(Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA), Intent("com.android.settings.TTS_SETTINGS"))
                }
            }) { Text(if (status == VoiceStatus.NO_ENGINE) "تثبيت محرك الصوت" else "تنزيل الصوت الإنجليزي") }
            TextButton(onClick = { speaker.recheck() }) { Text("تحقّق مرة أخرى") }
        }
    }
}
