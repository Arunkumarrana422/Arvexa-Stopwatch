package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SettingsRepository
import com.example.model.PrecisionMode
import com.example.model.ThemeMode
import com.example.model.VolumeAction
import com.example.ui.components.CustomSegmentedPicker
import com.example.ui.components.CustomSwitch
import com.example.ui.components.GlassCard
import com.example.ui.theme.AppTheme
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandPink
import com.example.ui.theme.BrandPurple

@Composable
fun SettingsScreen(
    settingsRepository: SettingsRepository,
    modifier: Modifier = Modifier
) {
    val settings by settingsRepository.settings.collectAsState()
    val isDark = AppTheme.isDark
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 12.dp),
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .padding(horizontal = 16.dp)
        ) {
            // TOP HEADER
            item {
                Column(modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)) {
                    Text(
                        text = "SETTINGS",
                        color = AppTheme.textPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Configure hardware buttons, timing, & style",
                        color = BrandCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // APPEARANCE (Dark / Light Mode)
            item {
                SettingsSectionHeader(title = "APPEARANCE & THEME", icon = Icons.Default.Palette, color = BrandCyan)
                Spacer(modifier = Modifier.height(8.dp))
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column {
                            Text(
                                text = "Theme Mode",
                                color = AppTheme.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            CustomSegmentedPicker(
                                options = ThemeMode.values().toList(),
                                selectedOption = settings.themeMode,
                                onOptionSelected = { settingsRepository.updateSettings(settings.copy(themeMode = it)) },
                                labelProvider = { it.label },
                                iconProvider = { mode ->
                                    when (mode) {
                                        ThemeMode.DARK -> Icons.Default.DarkMode
                                        ThemeMode.LIGHT -> Icons.Default.LightMode
                                        ThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                                    }
                                }
                            )
                        }

                        SettingsToggleRow(
                            title = "Smooth UI Animations",
                            subtitle = "Ring animations, particle glows, & transitions",
                            checked = settings.animationsEnabled,
                            onCheckedChange = { settingsRepository.updateSettings(settings.copy(animationsEnabled = it)) },
                            testTag = "toggle_animations"
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

        // STOPWATCH SECTION
        item {
            SettingsSectionHeader(title = "STOPWATCH ENGINE", icon = Icons.Default.Timer, color = BrandCyan)
            Spacer(modifier = Modifier.height(8.dp))
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Keep Screen Awake
                    SettingsToggleRow(
                        title = "Keep Screen Awake",
                        subtitle = "Prevents display from sleeping while timing",
                        checked = settings.keepScreenAwake,
                        onCheckedChange = { settingsRepository.updateSettings(settings.copy(keepScreenAwake = it)) },
                        testTag = "toggle_keep_awake"
                    )

                    // Auto Start
                    SettingsToggleRow(
                        title = "Auto Start on Launch",
                        subtitle = "Automatically starts stopwatch when opening app",
                        checked = settings.autoStartOnLaunch,
                        onCheckedChange = { settingsRepository.updateSettings(settings.copy(autoStartOnLaunch = it)) },
                        testTag = "toggle_auto_start"
                    )

                    // Precision Selector
                    Column {
                        Text(
                            text = "Timer Precision",
                            color = AppTheme.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Choose level of fraction resolution",
                            color = AppTheme.textSecondary,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        CustomSegmentedPicker(
                            options = PrecisionMode.values().toList(),
                            selectedOption = settings.precision,
                            onOptionSelected = { settingsRepository.updateSettings(settings.copy(precision = it)) },
                            labelProvider = { it.label }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // VOLUME BUTTON CONTROLS SECTION
        item {
            SettingsSectionHeader(title = "HARDWARE VOLUME BUTTONS", icon = Icons.Default.VolumeUp, color = BrandEmerald)
            Spacer(modifier = Modifier.height(8.dp))
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Volume button master toggle
                    SettingsToggleRow(
                        title = "Use Volume Buttons",
                        subtitle = "Trigger stopwatch using physical side volume buttons",
                        checked = settings.volumeControlEnabled,
                        onCheckedChange = { settingsRepository.updateSettings(settings.copy(volumeControlEnabled = it)) },
                        testTag = "toggle_volume_buttons"
                    )

                    if (settings.volumeControlEnabled) {
                        // Volume Up Action
                        VolumeActionSelector(
                            title = "Volume Up Action",
                            selectedAction = settings.volumeUpAction,
                            onActionSelected = { settingsRepository.updateSettings(settings.copy(volumeUpAction = it)) }
                        )

                        // Volume Down Action
                        VolumeActionSelector(
                            title = "Volume Down Action",
                            selectedAction = settings.volumeDownAction,
                            onActionSelected = { settingsRepository.updateSettings(settings.copy(volumeDownAction = it)) }
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isDark) Color(0xFF0F1A2E) else Color(0xFFE8EEF8))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = "Tip",
                                    tint = BrandCyan,
                                    modifier = Modifier.size(16.dp).padding(top = 1.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Tip: While running, press Volume Up to Pause/Resume, and Volume Down to take instant Laps without looking at the screen.",
                                    color = BrandCyan,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // NOTIFICATIONS & BACKGROUND
        item {
            SettingsSectionHeader(title = "NOTIFICATION CONTROLS", icon = Icons.Default.Notifications, color = BrandPurple)
            Spacer(modifier = Modifier.height(8.dp))
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SettingsToggleRow(
                        title = "Ongoing Lock-Screen Notification",
                        subtitle = "Show live stopwatch in background & lock screen",
                        checked = settings.showOngoingNotification,
                        onCheckedChange = { settingsRepository.updateSettings(settings.copy(showOngoingNotification = it)) },
                        testTag = "toggle_notification"
                    )

                    SettingsToggleRow(
                        title = "Show Lap Split in Notification",
                        subtitle = "Include current lap count and split timer",
                        checked = settings.showLapInNotification,
                        onCheckedChange = { settingsRepository.updateSettings(settings.copy(showLapInNotification = it)) },
                        testTag = "toggle_notif_lap"
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // HAPTICS & SOUND
        item {
            SettingsSectionHeader(title = "HAPTICS & AUDIO", icon = Icons.Default.Vibration, color = BrandPink)
            Spacer(modifier = Modifier.height(8.dp))
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SettingsToggleRow(
                        title = "Haptic Vibration",
                        subtitle = "Tactile feedback for button presses and laps",
                        checked = settings.hapticsEnabled,
                        onCheckedChange = { settingsRepository.updateSettings(settings.copy(hapticsEnabled = it)) },
                        testTag = "toggle_haptics"
                    )

                    SettingsToggleRow(
                        title = "Audio Beep Cues",
                        subtitle = "Short auditory cues for start, split, and stop",
                        checked = settings.soundCuesEnabled,
                        onCheckedChange = { settingsRepository.updateSettings(settings.copy(soundCuesEnabled = it)) },
                        testTag = "toggle_sound_cues"
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ABOUT & LEGAL
        item {
            SettingsSectionHeader(title = "ABOUT ARVEXA STOPWATCH", icon = Icons.Default.Info, color = AppTheme.textPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAboutDialog = true }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("About Arvexa Stopwatch", color = AppTheme.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text("v1.0.4 Pro", color = BrandCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPrivacyDialog = true }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Privacy Policy", color = AppTheme.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text("Offline / 100% Private", color = AppTheme.textSecondary, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

    // About Dialog
    if (showAboutDialog) {
        val aboutScrollState = rememberScrollState()
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            containerColor = if (isDark) Color(0xFF121B2F) else Color(0xFFFFFFFF),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = BrandCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "About Arvexa Stopwatch",
                        color = AppTheme.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(aboutScrollState)
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "Arvexa Stopwatch • Pro Edition",
                        color = BrandCyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Version 1.0.4 Pro",
                        color = AppTheme.textSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Overview",
                        color = AppTheme.textPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Arvexa Stopwatch is a high-precision, zero-latency timing utility built specifically for runners, track & field athletes, fitness workouts, coaches, and everyday timekeeping.",
                        color = AppTheme.textSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Key Capabilities",
                        color = AppTheme.textPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "• Sub-Millisecond Precision: Accurate monotonic timing engine guarantees zero clock drift.\n" +
                               "• Physical Volume Buttons: Start, pause, or record laps instantly using hardware volume keys.\n" +
                               "• Full Screen Lock: Prevent accidental taps during intense runs with a 5-second secure unlock system.\n" +
                               "• Deep Lap Analytics: Instant identification of Best and Slowest laps with difference deltas.\n" +
                               "• Persistent Background Timing: Runs accurately via Foreground Service even when screen is locked.\n" +
                               "• Material 3 Adaptive Theming: Seamless Dark & Light themes with automatic status bar icon matching.\n" +
                               "• 100% Offline & Private: No ads, no tracking, and zero data leaves your device.",
                        color = AppTheme.textSecondary,
                        fontSize = 11.5.sp,
                        lineHeight = 16.5.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Developer & Support",
                        color = AppTheme.textPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Support: ranaarunkumar5@gmail.com\nBuilt with Kotlin & Jetpack Compose for Android.",
                        color = AppTheme.textSecondary,
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAboutDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPurple)
                ) {
                    Text("Close", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Privacy Dialog
    if (showPrivacyDialog) {
        val privacyScrollState = rememberScrollState()
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            containerColor = if (isDark) Color(0xFF121B2F) else Color(0xFFFFFFFF),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = BrandEmerald,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Privacy Policy",
                        color = AppTheme.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(privacyScrollState)
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "Effective Date: October 2026",
                        color = AppTheme.textSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "1. Privacy Commitment",
                        color = AppTheme.textPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Arvexa Stopwatch is committed to protecting user privacy. This application is designed with an offline-first, privacy-by-design architecture.",
                        color = AppTheme.textSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "2. Zero Personal Data Collection",
                        color = AppTheme.textPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "We do NOT collect, transmit, sell, or store any personal information. We do not access names, email addresses, phone numbers, location data, contacts, photos, or unique advertising IDs. No account registration or sign-in is required.",
                        color = AppTheme.textSecondary,
                        fontSize = 11.5.sp,
                        lineHeight = 16.5.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "3. Local On-Device Storage",
                        color = AppTheme.textPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "All stopwatch timings, lap records, workout sessions, and user settings (sound cues, vibration, precision, theme preferences) are stored strictly on your local device storage using Android Room SQLite and SharedPreferences. No data ever leaves your device.",
                        color = AppTheme.textSecondary,
                        fontSize = 11.5.sp,
                        lineHeight = 16.5.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "4. Device Permissions & Purpose",
                        color = AppTheme.textPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "• FOREGROUND SERVICE: Required to keep stopwatch timing accurately running when the app is minimized.\n" +
                               "• POST NOTIFICATIONS: Used solely to display the active stopwatch notification with live timing controls.\n" +
                               "• VIBRATE: Delivers tactile haptic feedback on button presses.\n" +
                               "• WAKE LOCK: Optional feature to keep the display awake during an active run.",
                        color = AppTheme.textSecondary,
                        fontSize = 11.5.sp,
                        lineHeight = 16.5.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "5. Third-Party Services & Ads",
                        color = AppTheme.textPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Arvexa Stopwatch contains NO advertisements, NO third-party ad networks, NO analytics tracking SDKs, and NO third-party data harvesting tools.",
                        color = AppTheme.textSecondary,
                        fontSize = 11.5.sp,
                        lineHeight = 16.5.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "6. User Data Control & Deletion",
                        color = AppTheme.textPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "You maintain complete ownership of your data. You can clear laps, delete workout sessions, or reset app data at any time from within the app. Uninstalling the app permanently erases all locally saved records.",
                        color = AppTheme.textSecondary,
                        fontSize = 11.5.sp,
                        lineHeight = 16.5.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "7. Contact & Inquiries",
                        color = AppTheme.textPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "If you have any questions or inquiries regarding this Privacy Policy, please contact developer support at: ranaarunkumar5@gmail.com",
                        color = AppTheme.textSecondary,
                        fontSize = 11.5.sp,
                        lineHeight = 16.5.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPrivacyDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandEmerald)
                ) {
                    Text("Understood", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun SettingsSectionHeader(
    title: String,
    icon: ImageVector,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                color = AppTheme.textPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = subtitle,
                color = AppTheme.textSecondary,
                fontSize = 10.5.sp,
                lineHeight = 14.sp
            )
        }

        CustomSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            testTag = testTag
        )
    }
}

@Composable
fun VolumeActionSelector(
    title: String,
    selectedAction: VolumeAction,
    onActionSelected: (VolumeAction) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val isDark = AppTheme.isDark

    Column {
        Text(
            text = title,
            color = AppTheme.textPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (isDark) Color(0xFF0F1726) else Color(0xFFF1F5F9))
                .border(1.dp, if (isDark) Color(0x3300C2FF) else Color(0x336C5CE7), RoundedCornerShape(12.dp))
                .clickable { expanded = true }
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedAction.label,
                    color = if (isDark) BrandCyan else BrandPurple,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("▼", color = AppTheme.textSecondary, fontSize = 10.sp)
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(if (isDark) Color(0xFF131D33) else Color(0xFFFFFFFF))
            ) {
                VolumeAction.values().forEach { action ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                action.label,
                                color = if (isDark) Color.White else Color(0xFF0F172A)
                            )
                        },
                        onClick = {
                            onActionSelected(action)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
