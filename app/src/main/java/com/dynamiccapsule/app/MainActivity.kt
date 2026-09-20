package com.dynamiccapsule.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dynamiccapsule.app.model.CapsuleStore
import com.dynamiccapsule.app.model.CapsuleKind
import com.dynamiccapsule.app.model.CapsuleState
import com.dynamiccapsule.app.overlay.CapsuleService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val Ink = Color(0xFF080A0D)
private val Panel = Color(0xFF12161B)
private val Mint = Color(0xFF8CF7C5)
private val Muted = Color(0xFF8B949E)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { CapsuleApp() }
    }
}

@androidx.compose.runtime.Composable
private fun CapsuleApp() {
    val context = LocalContext.current
    var enabled by remember { mutableStateOf(false) }
    var timerJob by remember { mutableStateOf<Job?>(null) }
    val scope = rememberCoroutineScope()
    val state by CapsuleStore.state.collectAsStateWithLifecycle()

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = Ink) {
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 28.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Text("Dynamic Capsule", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text("Your live moments, one glance away.", color = Muted, fontSize = 15.sp)

                CapsulePreview(state)

                Button(
                    onClick = {
                        timerJob?.cancel()
                        timerJob = scope.launch {
                            for (seconds in 60 downTo 0) {
                                CapsuleStore.set(CapsuleState(CapsuleKind.TIMER, "Timer", "$seconds sec left", seconds / 60f))
                                delay(1000)
                            }
                            CapsuleStore.set(CapsuleState())
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Mint),
                ) { Text("Start 60-second timer", color = Ink, fontWeight = FontWeight.Bold) }

                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Panel).padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text("Show capsule over other apps", color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text("Keep it visible for live activity", color = Muted, fontSize = 13.sp)
                    }
                    Switch(checked = enabled, onCheckedChange = { value ->
                        enabled = value
                        if (value) {
                            if (!Settings.canDrawOverlays(context)) {
                                context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}")))
                            } else {
                                ContextCompat.startForegroundService(context, Intent(context, CapsuleService::class.java))
                            }
                        } else context.stopService(Intent(context, CapsuleService::class.java))
                    })
                }

                Text("Live sources", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                SourceRow("Timer", "Create a timer that stays in reach")
                SourceRow("Media", "Control what is playing without leaving your app")
                SourceRow("Calls", "See call status at a glance")
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun CapsulePreview(state: com.dynamiccapsule.app.model.CapsuleState) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("PREVIEW", color = Muted, fontSize = 11.sp, letterSpacing = 1.5.sp)
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier.animateContentSize().clip(RoundedCornerShape(if (state.expanded) 28.dp else 50.dp)).background(Color.Black).padding(horizontal = 18.dp, vertical = if (state.expanded) 20.dp else 11.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.size(8.dp).clip(RoundedCornerShape(50)).background(Mint))
                    Text(state.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text("·", color = Muted)
                    Text(state.subtitle, color = Muted, fontSize = 12.sp)
                }
                AnimatedVisibility(state.expanded) {
                    Text("Tap the capsule to expand live controls", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 12.dp))
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick = { CapsuleStore.toggleExpanded() }, colors = ButtonDefaults.buttonColors(containerColor = Panel)) {
            Text(if (state.expanded) "Collapse preview" else "Try expanded state", color = Mint)
        }
    }
}

@androidx.compose.runtime.Composable
private fun SourceRow(title: String, description: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(9.dp).clip(RoundedCornerShape(50)).background(Mint))
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Medium)
            Text(description, color = Muted, fontSize = 13.sp)
        }
    }
}
