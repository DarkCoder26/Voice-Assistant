package com.example

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.permissions.PermissionItem
import com.example.presentation.MainViewModel
import com.example.presentation.screens.AppsDirectoryScreen
import com.example.presentation.screens.HistoryScreen
import com.example.presentation.screens.SettingsScreen
import com.example.presentation.screens.VoiceAssistantScreen
import com.example.ui.theme.AuraCyan
import com.example.ui.theme.AuraDarkBorder
import com.example.ui.theme.AuraDarkSurface
import com.example.ui.theme.AuraTextMuted
import com.example.ui.theme.AuraTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AuraTheme {
                val uiState by viewModel.uiState.collectAsState()
                val historyList by viewModel.history.collectAsState()
                var currentTab by remember { mutableIntStateOf(0) }

                // Runtime permissions launcher
                val permissionsLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) {
                    viewModel.refreshPermissions()
                }

                // Refresh permissions when returning to the app from system settings
                val lifecycleOwner = LocalLifecycleOwner.current
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            viewModel.refreshPermissions()
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }

                fun requestRequiredPermissions() {
                    val needed = mutableListOf(
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.SEND_SMS,
                        Manifest.permission.CAMERA
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        needed.add(Manifest.permission.BLUETOOTH_CONNECT)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        needed.add(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    permissionsLauncher.launch(needed.toTypedArray())
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar(
                            containerColor = AuraDarkSurface,
                            tonalElevation = 8.dp,
                            modifier = Modifier.testTag("bottom_nav_bar")
                        ) {
                            NavigationBarItem(
                                selected = currentTab == 0,
                                onClick = { currentTab = 0 },
                                icon = { Icon(Icons.Default.Mic, contentDescription = "Aura Assistant", modifier = Modifier.size(24.dp)) },
                                label = { Text("Aura") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = AuraCyan,
                                    selectedTextColor = AuraCyan,
                                    unselectedIconColor = AuraTextMuted,
                                    unselectedTextColor = AuraTextMuted,
                                    indicatorColor = AuraCyan.copy(alpha = 0.16f)
                                ),
                                modifier = Modifier.testTag("nav_tab_aura")
                            )
                            NavigationBarItem(
                                selected = currentTab == 1,
                                onClick = { currentTab = 1 },
                                icon = { Icon(Icons.Default.Apps, contentDescription = "Command Directory", modifier = Modifier.size(24.dp)) },
                                label = { Text("Directory") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = AuraCyan,
                                    selectedTextColor = AuraCyan,
                                    unselectedIconColor = AuraTextMuted,
                                    unselectedTextColor = AuraTextMuted,
                                    indicatorColor = AuraCyan.copy(alpha = 0.16f)
                                ),
                                modifier = Modifier.testTag("nav_tab_directory")
                            )
                            NavigationBarItem(
                                selected = currentTab == 2,
                                onClick = { currentTab = 2 },
                                icon = { Icon(Icons.Default.History, contentDescription = "History", modifier = Modifier.size(24.dp)) },
                                label = { Text("History") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = AuraCyan,
                                    selectedTextColor = AuraCyan,
                                    unselectedIconColor = AuraTextMuted,
                                    unselectedTextColor = AuraTextMuted,
                                    indicatorColor = AuraCyan.copy(alpha = 0.16f)
                                ),
                                modifier = Modifier.testTag("nav_tab_history")
                            )
                            NavigationBarItem(
                                selected = currentTab == 3,
                                onClick = { currentTab = 3 },
                                icon = { Icon(Icons.Default.Settings, contentDescription = "Settings", modifier = Modifier.size(24.dp)) },
                                label = { Text("Settings") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = AuraCyan,
                                    selectedTextColor = AuraCyan,
                                    unselectedIconColor = AuraTextMuted,
                                    unselectedTextColor = AuraTextMuted,
                                    indicatorColor = AuraCyan.copy(alpha = 0.16f)
                                ),
                                modifier = Modifier.testTag("nav_tab_settings")
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentTab) {
                            0 -> VoiceAssistantScreen(
                                uiState = uiState,
                                onMicClick = {
                                    if (!viewModel.permissionManager.isAudioRecordGranted()) {
                                        requestRequiredPermissions()
                                    } else {
                                        viewModel.onMicClicked()
                                    }
                                },
                                onSubmitText = { viewModel.submitTextQuery(it) },
                                onClarificationChosen = { viewModel.selectClarificationOption(it) },
                                onConfirmAction = { viewModel.confirmPendingAction() },
                                onCancelAction = { viewModel.cancelPendingAction() },
                                onOpenSettings = { key ->
                                    if (key == "ACCESSIBILITY_SERVICE") {
                                        viewModel.permissionManager.openAccessibilitySettings()
                                    } else if (key == "NOTIFICATION_LISTENER") {
                                        viewModel.permissionManager.openNotificationListenerSettings()
                                    } else if (key == "SYSTEM_ALERT_WINDOW" || key == "OVERLAY") {
                                        viewModel.permissionManager.openOverlaySettings()
                                    } else {
                                        requestRequiredPermissions()
                                    }
                                },
                                onToggleMute = { viewModel.toggleVoiceMute() }
                            )
                            1 -> AppsDirectoryScreen(
                                onTestCommand = { cmd ->
                                    currentTab = 0
                                    viewModel.submitTextQuery(cmd)
                                }
                            )
                            2 -> HistoryScreen(
                                conversations = historyList,
                                onClearHistory = { viewModel.clearHistory() },
                                onDeleteItem = { viewModel.deleteHistoryItem(it) }
                            )
                            3 -> SettingsScreen(
                                permissions = uiState.permissions,
                                speechSpeed = uiState.speechSpeed,
                                speechPitch = uiState.speechPitch,
                                isVoiceMuted = uiState.isVoiceMuted,
                                onSpeedChange = { viewModel.setSpeechSpeed(it) },
                                onPitchChange = { viewModel.setSpeechPitch(it) },
                                onToggleMute = { viewModel.toggleVoiceMute() },
                                onRequestPermission = { perm ->
                                    if (perm.key.startsWith("android.permission")) {
                                        permissionsLauncher.launch(arrayOf(perm.key))
                                    } else if (perm.actionType == com.example.permissions.PermissionItem.ActionType.OVERLAY_SETTINGS) {
                                        viewModel.permissionManager.openOverlaySettings()
                                    } else {
                                        viewModel.permissionManager.openAppSettings()
                                    }
                                },
                                onOpenAppSettings = { viewModel.permissionManager.openAppSettings() },
                                onOpenAccessibilitySettings = { viewModel.permissionManager.openAccessibilitySettings() },
                                onOpenNotificationListenerSettings = { viewModel.permissionManager.openNotificationListenerSettings() },
                                onOpenOverlaySettings = { viewModel.permissionManager.openOverlaySettings() },
                                onClearAllData = { viewModel.clearHistory() }
                            )
                        }
                    }
                }
            }
        }
    }
}

// Retained for backward-compatibility with GreetingScreenshotTest
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
