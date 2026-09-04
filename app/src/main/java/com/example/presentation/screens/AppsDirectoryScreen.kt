package com.example.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AuraCyan
import com.example.ui.theme.AuraDarkBg
import com.example.ui.theme.AuraDarkBorder
import com.example.ui.theme.AuraDarkCard
import com.example.ui.theme.AuraMagenta
import com.example.ui.theme.AuraSuccess
import com.example.ui.theme.AuraTextMuted
import com.example.ui.theme.AuraTextPrimary
import com.example.ui.theme.AuraTextSecondary
import com.example.ui.theme.AuraViolet

data class VoiceCommandCategory(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconColor: Color,
    val sampleCommands: List<String>
)

@Composable
fun AppsDirectoryScreen(
    onTestCommand: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf(
        VoiceCommandCategory(
            title = "Phone Calls & Contacts",
            description = "Make calls hands-free with contact disambiguation",
            icon = Icons.Default.Call,
            iconColor = AuraSuccess,
            sampleCommands = listOf("Call Mom", "Rahul ko call karo", "Dial 1234567890")
        ),
        VoiceCommandCategory(
            title = "WhatsApp & Messaging",
            description = "Send WhatsApp messages or SMS seamlessly",
            icon = Icons.AutoMirrored.Filled.Message,
            iconColor = Color(0xFF25D366),
            sampleCommands = listOf(
                "WhatsApp Rahul saying I'll reach in 10 minutes",
                "Rahul ko SMS karo: Call me back",
                "Open WhatsApp"
            )
        ),
        VoiceCommandCategory(
            title = "Volume & Sound Control",
            description = "Adjust device sound levels instantly",
            icon = Icons.AutoMirrored.Filled.VolumeUp,
            iconColor = AuraCyan,
            sampleCommands = listOf(
                "Volume 50 percent",
                "Volume badha do",
                "Mute phone",
                "Unmute"
            )
        ),
        VoiceCommandCategory(
            title = "Music & YouTube Playback",
            description = "Control media or search videos on YouTube",
            icon = Icons.Default.PlayArrow,
            iconColor = AuraMagenta,
            sampleCommands = listOf(
                "Play Arijit Singh on YouTube",
                "Play music",
                "Pause music",
                "Next song"
            )
        ),
        VoiceCommandCategory(
            title = "App Launching",
            description = "Launch any installed application by voice",
            icon = Icons.Default.Settings,
            iconColor = AuraViolet,
            sampleCommands = listOf(
                "Open YouTube",
                "Camera kholo",
                "Open Chrome",
                "Open Settings"
            )
        ),
        VoiceCommandCategory(
            title = "Screen Navigation & Accessibility",
            description = "Navigate Home, Back, Recents, or lock screen hands-free",
            icon = Icons.Default.Navigation,
            iconColor = AuraCyan,
            sampleCommands = listOf(
                "Go home",
                "Back jao",
                "Open recents",
                "Notification panel kholo"
            )
        ),
        VoiceCommandCategory(
            title = "Alarms & Timers",
            description = "Set wake up alarms and countdown timers",
            icon = Icons.Default.Alarm,
            iconColor = AuraViolet,
            sampleCommands = listOf(
                "Set a timer for 10 minutes",
                "Set alarm for 7 AM",
                "5 minute ka timer lagao"
            )
        ),
        VoiceCommandCategory(
            title = "Device Info & Web Search",
            description = "Check battery status, time, date, or search Google",
            icon = Icons.Default.Info,
            iconColor = AuraCyan,
            sampleCommands = listOf(
                "Battery level",
                "What time is it",
                "Search Google for Android Jetpack Compose",
                "What's the weather"
            )
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AuraDarkBg)
            .padding(16.dp)
    ) {
        Text(
            text = "Voice Commands Directory",
            color = AuraTextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Explore what Aura can do on your device",
            color = AuraTextMuted,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(categories) { category ->
                CommandCategoryCard(category = category, onTestCommand = onTestCommand)
            }
        }
    }
}

@Composable
fun CommandCategoryCard(
    category: VoiceCommandCategory,
    onTestCommand: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AuraDarkCard),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, AuraDarkBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(category.iconColor.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = null,
                        tint = category.iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = category.title,
                        color = AuraTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = category.description,
                        color = AuraTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            category.sampleCommands.forEach { cmd ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "• \"$cmd\"",
                        color = AuraTextPrimary,
                        fontSize = 13.sp,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedButton(
                        onClick = { onTestCommand(cmd) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AuraCyan),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("Try", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
