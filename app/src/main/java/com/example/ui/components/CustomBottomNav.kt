package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppScreen
import com.example.ui.theme.AppTheme
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandPurple
import com.example.util.HapticHelper

data class NavItem(
    val screen: AppScreen,
    val icon: ImageVector,
    val label: String
)

@Composable
fun CustomBottomNav(
    currentScreen: AppScreen,
    onScreenSelected: (AppScreen) -> Unit,
    lapCount: Int = 0,
    hapticsEnabled: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = AppTheme.isDark
    val navItems = listOf(
        NavItem(AppScreen.STOPWATCH, Icons.Default.Timer, "Timer"),
        NavItem(AppScreen.LAPS, Icons.Default.Flag, "Laps"),
        NavItem(AppScreen.STATS, Icons.Default.BarChart, "Stats"),
        NavItem(AppScreen.SETTINGS, Icons.Default.Settings, "Settings")
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 12.dp)
    ) {
        // Floating glassmorphic pill background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(
                    elevation = if (isDark) 20.dp else 14.dp,
                    shape = RoundedCornerShape(32.dp),
                    spotColor = if (isDark) BrandPurple.copy(alpha = 0.5f) else Color(0x33000000),
                    ambientColor = if (isDark) Color.Black else Color(0x22000000)
                )
                .clip(RoundedCornerShape(32.dp))
                .background(AppTheme.bottomNavBg)
                .border(
                    width = 1.2.dp,
                    brush = AppTheme.bottomNavBorder,
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                navItems.forEach { item ->
                    val isSelected = currentScreen == item.screen

                    val itemWidth by animateDpAsState(
                        targetValue = if (isSelected) 96.dp else 50.dp,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "nav_width"
                    )

                    val iconTint by animateColorAsState(
                        targetValue = if (isSelected) Color.White else (if (isDark) Color(0xFF8E9BAE) else Color(0xFF64748B)),
                        animationSpec = tween(durationMillis = 200),
                        label = "icon_color"
                    )

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .height(48.dp)
                            .width(itemWidth)
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                brush = if (isSelected) {
                                    Brush.horizontalGradient(
                                        listOf(
                                            BrandPurple.copy(alpha = 0.90f),
                                            BrandCyan.copy(alpha = 0.90f)
                                        )
                                    )
                                } else {
                                    Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                                },
                                shape = RoundedCornerShape(24.dp)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                if (!isSelected) {
                                    HapticHelper.trigger(context, hapticsEnabled, HapticHelper.HapticType.LIGHT)
                                    onScreenSelected(item.screen)
                                }
                            }
                            .testTag("nav_tab_${item.screen.name.lowercase()}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        ) {
                            Box(contentAlignment = Alignment.TopEnd) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    tint = iconTint,
                                    modifier = Modifier.size(22.dp)
                                )

                                // Lap badge count
                                if (item.screen == AppScreen.LAPS && lapCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(BrandEmerald, CircleShape)
                                    )
                                }
                            }

                            if (isSelected) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = item.label,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
