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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.permissions.PermissionItem
import com.example.ui.theme.AuraCyan
import com.example.ui.theme.AuraDarkBg
import com.example.ui.theme.AuraDarkBorder
import com.example.ui.theme.AuraDarkCard
import com.example.ui.theme.AuraError
import com.example.ui.theme.AuraSuccess
import com.example.ui.theme.AuraTextMuted
import com.example.ui.theme.AuraTextPrimary
import com.example.ui.theme.AuraTextSecondary
import com.example.ui.theme.AuraViolet

@Composable
fun SettingsScreen(
    permissions: List<PermissionItem>,
    speechSpeed: Float,
    speechPitch: Float,
    isVoiceMuted: Boolean,
    onSpeedChange: (Float) -> Unit,
    onPitchChange: (Float) -> Unit,
    onToggleMute: () -> Unit,
    onRequestPermission: (PermissionItem) -> Unit,
    onOpenAppSettings: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onOpenNotificationListenerSettings: () -> Unit,
    onOpenOverlaySettings: () -> Unit,
    onClearAllData: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AuraDarkBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            Text(
                text = "Settings & Privacy",
                color = AuraTextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Created By DarkCoder • Full On-Device System Control",
                color = AuraCyan,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // --- Voice Synthesis Settings ---
        item {
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
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(AuraCyan.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = AuraCyan, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Voice Synthesis",
                            color = AuraTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Mute Voice toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Mute Spoken Feedback", color = AuraTextPrimary, fontSize = 14.sp)
                            Text("Aura will only display responses visually", color = AuraTextSecondary, fontSize = 12.sp)
                        }
                        Switch(
                            checked = isVoiceMuted,
                            onCheckedChange = { onToggleMute() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AuraCyan,
                                checkedTrackColor = AuraCyan.copy(alpha = 0.4f)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Speed slider
                    Text(
                        text = "Speech Rate: ${(speechSpeed * 10).toInt() / 10f}x",
                        color = AuraTextPrimary,
                        fontSize = 13.sp
                    )
                    Slider(
                        value = speechSpeed,
                        onValueChange = onSpeedChange,
                        valueRange = 0.6f..1.8f,
                        steps = 5,
                        colors = SliderDefaults.colors(
                            thumbColor = AuraCyan,
                            activeTrackColor = AuraCyan,
                            inactiveTrackColor = AuraDarkBorder
                        )
                    )

                    // Pitch slider
                    Text(
                        text = "Voice Pitch: ${(speechPitch * 10).toInt() / 10f}x",
                        color = AuraTextPrimary,
                        fontSize = 13.sp
                    )
                    Slider(
                        value = speechPitch,
                        onValueChange = onPitchChange,
                        valueRange = 0.6f..1.6f,
                        steps = 4,
                        colors = SliderDefaults.colors(
                            thumbColor = AuraViolet,
                            activeTrackColor = AuraViolet,
                            inactiveTrackColor = AuraDarkBorder
                        )
                    )
                }
            }
        }

        // --- Permissions Manager Section ---
        item {
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
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(AuraViolet.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = AuraViolet, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Device Permissions",
                                color = AuraTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Required for legitimate hands-free device actions",
                                color = AuraTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    permissions.forEach { perm ->
                        PermissionRow(
                            item = perm,
                            onRequest = { onRequestPermission(perm) },
                            onOpenApp = onOpenAppSettings,
                            onOpenAccess = onOpenAccessibilitySettings,
                            onOpenNotif = onOpenNotificationListenerSettings,
                            onOpenOverlay = onOpenOverlaySettings
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }

        // --- AI Intelligence & Privacy Section ---
        item {
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
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(AuraCyan.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.SmartToy, contentDescription = null, tint = AuraCyan, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "AI Engine & Privacy",
                            color = AuraTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "• Model: Gemini 3.5 Flash (Direct REST)\n• Offline-First: All phone controls, calls, volume, and app launches execute 100% on-device.\n• Privacy: No unauthorized background recordings or cloud data logging.",
                        color = AuraTextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = onClearAllData,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AuraError),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Delete All Local Command History")
                    }
                }
            }
        }

        // --- Dedicated About Section ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_about_section"),
                colors = CardDefaults.cardColors(containerColor = AuraDarkCard),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, AuraCyan.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "About",
                        color = AuraTextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Developed by DarkCoder",
                        color = AuraCyan,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Aura Voice • Advanced Android System Controller",
                        color = AuraTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun PermissionRow(
    item: PermissionItem,
    onRequest: () -> Unit,
    onOpenApp: () -> Unit,
    onOpenAccess: () -> Unit,
    onOpenNotif: () -> Unit,
    onOpenOverlay: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (item.isGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (item.isGranted) AuraSuccess else AuraError,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = item.title,
                    color = AuraTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = item.description,
                    color = AuraTextMuted,
                    fontSize = 11.sp,
                    maxLines = 2
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        if (item.isGranted) {
            Text("Granted", color = AuraSuccess, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        } else {
            Button(
                onClick = {
                    when (item.actionType) {
                        PermissionItem.ActionType.RUNTIME_REQUEST -> onRequest()
                        PermissionItem.ActionType.ACCESSIBILITY_SETTINGS -> onOpenAccess()
                        PermissionItem.ActionType.NOTIFICATION_LISTENER_SETTINGS -> onOpenNotif()
                        PermissionItem.ActionType.OVERLAY_SETTINGS -> onOpenOverlay()
                        PermissionItem.ActionType.APP_SETTINGS -> onOpenApp()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AuraViolet),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                modifier = Modifier.height(30.dp)
            ) {
                Text("Enable", fontSize = 11.sp, color = Color.White)
            }
        }
    }
}
