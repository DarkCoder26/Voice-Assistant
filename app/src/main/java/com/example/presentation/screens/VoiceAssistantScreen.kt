package com.example.presentation.screens

import android.Manifest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.VoiceState
import com.example.presentation.AssistantUiState
import com.example.presentation.components.ActionFeedbackCard
import com.example.presentation.components.AuraOrb
import com.example.ui.theme.AuraCyan
import com.example.ui.theme.AuraDarkBg
import com.example.ui.theme.AuraDarkBorder
import com.example.ui.theme.AuraDarkCard
import com.example.ui.theme.AuraDarkSurface
import com.example.ui.theme.AuraError
import com.example.ui.theme.AuraMagenta
import com.example.ui.theme.AuraSuccess
import com.example.ui.theme.AuraTextMuted
import com.example.ui.theme.AuraTextPrimary
import com.example.ui.theme.AuraTextSecondary
import com.example.ui.theme.AuraViolet

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VoiceAssistantScreen(
    uiState: AssistantUiState,
    onMicClick: () -> Unit,
    onSubmitText: (String) -> Unit,
    onClarificationChosen: (String) -> Unit,
    onConfirmAction: () -> Unit,
    onCancelAction: () -> Unit,
    onOpenSettings: (String) -> Unit,
    onToggleMute: () -> Unit,
    modifier: Modifier = Modifier
) {
    var textInput by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    val quickPrompts = listOf(
        "Call Mom",
        "WhatsApp Rahul",
        "Volume 50%",
        "Play Music",
        "Open YouTube",
        "Set 5 min timer",
        "What's the weather",
        "Battery level"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AuraDarkBg)
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar: Branding & Mute toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                when (uiState.voiceState) {
                                    is VoiceState.Listening -> AuraCyan
                                    is VoiceState.Processing -> AuraViolet
                                    is VoiceState.Executing -> AuraMagenta
                                    is VoiceState.Error -> AuraError
                                    else -> AuraCyan.copy(alpha = 0.6f)
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Aura Voice",
                            color = AuraTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Created By DarkCoder",
                            color = AuraCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.4.sp
                        )
                    }
                }

                IconButton(
                    onClick = onToggleMute,
                    modifier = Modifier.testTag("mute_button")
                ) {
                    Icon(
                        imageVector = if (uiState.isVoiceMuted) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = if (uiState.isVoiceMuted) "Voice is muted" else "Voice is active",
                        tint = if (uiState.isVoiceMuted) AuraTextMuted else AuraCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Microphone Permission Banner (when not granted)
            val micPermission = uiState.permissions.find { it.key == Manifest.permission.RECORD_AUDIO }
            val isMicGranted = micPermission?.isGranted ?: true

            if (!isMicGranted) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .testTag("card_mic_permission_required"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = AuraViolet.copy(alpha = 0.15f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AuraViolet.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.MicOff,
                                contentDescription = "Microphone Permission Required",
                                tint = AuraViolet,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Microphone Permission Required",
                                    color = AuraTextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Tap to grant audio access for speech recognition & system control",
                                    color = AuraTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onMicClick,
                            colors = ButtonDefaults.buttonColors(containerColor = AuraViolet),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                            modifier = Modifier.height(32.dp).testTag("grant_mic_permission_button")
                        ) {
                            Text("Grant", fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
            } else {
                // Active status pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(AuraDarkCard)
                        .border(1.dp, AuraCyan.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = AuraSuccess,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Speech Recognition & System Control Active",
                        color = AuraTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // The Glowing Animated Aura Orb
            AuraOrb(
                voiceState = uiState.voiceState,
                audioLevel = uiState.audioLevel,
                onClick = onMicClick,
                size = 190.dp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Assistant Status Label
            Text(
                text = uiState.voiceState.label,
                color = when (uiState.voiceState) {
                    is VoiceState.Error -> AuraError
                    is VoiceState.Listening -> AuraCyan
                    is VoiceState.Processing -> AuraViolet
                    else -> AuraTextSecondary
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            // Live Transcript Bubble (if user spoke or typed)
            AnimatedVisibility(
                visible = uiState.currentTranscript.isNotBlank(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .testTag("transcript_card"),
                    colors = CardDefaults.cardColors(containerColor = AuraDarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AuraDarkBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = AuraCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "\"${uiState.currentTranscript}\"",
                            color = AuraTextPrimary,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dynamic Action Feedback Card (Calls, SMS, Clarifications, Confirmation, Errors)
            ActionFeedbackCard(
                result = uiState.lastResult,
                pendingClarification = uiState.pendingClarification,
                pendingConfirmation = uiState.pendingConfirmation,
                onClarificationChosen = onClarificationChosen,
                onConfirmAction = onConfirmAction,
                onCancelAction = onCancelAction,
                onOpenSettings = onOpenSettings
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Action Prompt Chips
            Text(
                text = "Suggested Commands",
                color = AuraTextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickPrompts.forEach { prompt ->
                    SuggestionChip(
                        onClick = { onSubmitText(prompt) },
                        label = { Text(prompt, fontSize = 13.sp, color = AuraTextPrimary) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = AuraDarkCard
                        ),
                        border = SuggestionChipDefaults.suggestionChipBorder(
                            enabled = true,
                            borderColor = AuraDarkBorder
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Signature Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_developer_credits_main"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AuraDarkCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, AuraCyan.copy(alpha = 0.25f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Created By DarkCoder",
                            color = AuraCyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Hands-Free Speech Recognition & Overall System Control",
                            color = AuraTextMuted,
                            fontSize = 11.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(AuraCyan.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "AURA",
                            color = AuraCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Bottom Docked Controls: Text Input & Microphone Floating Button
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = AuraDarkSurface.copy(alpha = 0.95f),
            shadowElevation = 8.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, AuraDarkBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Direct Keyboard Input Fallback
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("text_query_input"),
                        placeholder = { Text("Ask Aura or enter command...", color = AuraTextMuted, fontSize = 14.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AuraCyan,
                            unfocusedBorderColor = AuraDarkBorder,
                            focusedTextColor = AuraTextPrimary,
                            unfocusedTextColor = AuraTextPrimary,
                            cursorColor = AuraCyan
                        ),
                        shape = RoundedCornerShape(24.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (textInput.isNotBlank()) {
                                    onSubmitText(textInput)
                                    textInput = ""
                                    focusManager.clearFocus()
                                }
                            }
                        ),
                        trailingIcon = {
                            if (textInput.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        onSubmitText(textInput)
                                        textInput = ""
                                        focusManager.clearFocus()
                                    },
                                    modifier = Modifier.testTag("send_query_button")
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = AuraCyan)
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    // Primary Docked Microphone Button
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = if (uiState.isMicListening)
                                        listOf(AuraMagenta, AuraViolet)
                                    else
                                        listOf(AuraCyan, AuraViolet)
                                )
                            )
                            .border(
                                width = if (uiState.isMicListening) 2.dp else 0.dp,
                                color = Color.White,
                                shape = CircleShape
                            )
                            .testTag("main_mic_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = onMicClick,
                            modifier = Modifier.size(52.dp)
                        ) {
                            Icon(
                                imageVector = if (uiState.isMicListening) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = if (uiState.isMicListening) "Stop Listening" else "Start Listening",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
