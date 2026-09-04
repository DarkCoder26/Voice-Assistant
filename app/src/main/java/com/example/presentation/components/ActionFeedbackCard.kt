package com.example.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
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
import com.example.domain.model.ActionResult
import com.example.domain.model.VisualCardType
import com.example.ui.theme.AuraCyan
import com.example.ui.theme.AuraDarkBorder
import com.example.ui.theme.AuraDarkCard
import com.example.ui.theme.AuraError
import com.example.ui.theme.AuraSuccess
import com.example.ui.theme.AuraTextMuted
import com.example.ui.theme.AuraTextPrimary
import com.example.ui.theme.AuraTextSecondary
import com.example.ui.theme.AuraViolet

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActionFeedbackCard(
    result: ActionResult?,
    pendingClarification: ActionResult.NeedsClarification?,
    pendingConfirmation: ActionResult.NeedsConfirmation?,
    onClarificationChosen: (String) -> Unit,
    onConfirmAction: () -> Unit,
    onCancelAction: () -> Unit,
    onOpenSettings: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // 1. Prioritize Clarification choices if waiting
    if (pendingClarification != null) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("clarification_card"),
            colors = CardDefaults.cardColors(containerColor = AuraDarkCard),
            shape = RoundedCornerShape(18.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, AuraCyan.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(AuraCyan.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = null,
                            tint = AuraCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Clarification Needed",
                        color = AuraCyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = pendingClarification.question,
                    color = AuraTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal
                )

                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    pendingClarification.options.forEach { option ->
                        SuggestionChip(
                            onClick = { onClarificationChosen(option) },
                            label = { Text(option, color = AuraTextPrimary) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = Color(0xFF242C4C)
                            ),
                            border = SuggestionChipDefaults.suggestionChipBorder(
                                enabled = true,
                                borderColor = AuraCyan.copy(alpha = 0.4f)
                            )
                        )
                    }
                }
            }
        }
        return
    }

    // 2. Pending Confirmation
    if (pendingConfirmation != null) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("confirmation_card"),
            colors = CardDefaults.cardColors(containerColor = AuraDarkCard),
            shape = RoundedCornerShape(18.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, AuraViolet.copy(alpha = 0.6f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Confirm Action",
                    color = AuraViolet,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = pendingConfirmation.question,
                    color = AuraTextPrimary,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onCancelAction,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AuraTextSecondary)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = onConfirmAction,
                        colors = ButtonDefaults.buttonColors(containerColor = AuraCyan, contentColor = Color.Black)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Confirm")
                    }
                }
            }
        }
        return
    }

    if (result == null) return

    // 3. Render Completed Action Result Card
    when (result) {
        is ActionResult.Success -> {
            val (icon, tintColor) = getCardIconAndTint(result.visualCardType)
            Card(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("action_result_card"),
                colors = CardDefaults.cardColors(containerColor = AuraDarkCard),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, AuraDarkBorder)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(tintColor.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = tintColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = result.spokenMessage,
                            color = AuraTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                        if (result.details.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = result.details,
                                color = AuraTextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
        is ActionResult.MissingPermission -> {
            Card(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("missing_permission_card"),
                colors = CardDefaults.cardColors(containerColor = AuraDarkCard),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, AuraError.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = AuraError,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Permission Required",
                            color = AuraError,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = result.explanation,
                        color = AuraTextPrimary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { onOpenSettings(result.permissionKey) },
                        colors = ButtonDefaults.buttonColors(containerColor = AuraError),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Grant Permission", color = Color.White)
                    }
                }
            }
        }
        is ActionResult.Error -> {
            Card(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("error_card"),
                colors = CardDefaults.cardColors(containerColor = AuraDarkCard),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, AuraError.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(AuraError.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = AuraError,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = result.errorMessage,
                            color = AuraTextPrimary,
                            fontSize = 14.sp
                        )
                        if (result.technicalReason != null) {
                            Text(
                                text = result.technicalReason,
                                color = AuraTextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
        else -> Unit
    }
}

private fun getCardIconAndTint(cardType: VisualCardType): Pair<ImageVector, Color> {
    return when (cardType) {
        VisualCardType.CALL -> Pair(Icons.Default.Call, AuraSuccess)
        VisualCardType.SMS -> Pair(Icons.AutoMirrored.Filled.Message, AuraCyan)
        VisualCardType.WHATSAPP -> Pair(Icons.AutoMirrored.Filled.Send, Color(0xFF25D366))
        VisualCardType.VOLUME -> Pair(Icons.AutoMirrored.Filled.VolumeUp, AuraCyan)
        VisualCardType.MEDIA -> Pair(Icons.Default.PlayArrow, AuraViolet)
        VisualCardType.APP_LAUNCH -> Pair(Icons.Default.Settings, AuraCyan)
        VisualCardType.SYSTEM_SETTING -> Pair(Icons.Default.Settings, AuraViolet)
        VisualCardType.INFO -> Pair(Icons.Default.SmartToy, AuraCyan)
        VisualCardType.AI_ANSWER -> Pair(Icons.Default.SmartToy, AuraViolet)
        VisualCardType.NONE -> Pair(Icons.Default.Check, AuraSuccess)
    }
}
