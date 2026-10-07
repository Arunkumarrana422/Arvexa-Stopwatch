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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ThemeMode
import com.example.ui.theme.AppTheme
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandPurple
import com.example.util.HapticHelper

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    borderBrush: Brush? = null,
    backgroundColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val isDark = AppTheme.isDark
    val finalBg = backgroundColor ?: AppTheme.cardBackground
    val finalBorder = borderBrush ?: if (isDark) {
        Brush.linearGradient(
            listOf(
                Color(0x336C5CE7),
                Color(0x2200C2FF),
                Color(0x11FFFFFF)
            )
        )
    } else {
        Brush.linearGradient(
            listOf(
                Color(0x336C5CE7),
                Color(0x2200C2FF),
                Color(0x44CBD5E1)
            )
        )
    }

    val shape = RoundedCornerShape(cornerRadius)

    val boxModifier = if (onClick != null) {
        modifier
            .shadow(
                elevation = if (isDark) 8.dp else 6.dp,
                shape = shape,
                spotColor = if (isDark) Color(0x3300C2FF) else Color(0x22000000)
            )
            .clip(shape)
            .clickable(onClick = onClick)
            .background(finalBg)
            .border(1.dp, finalBorder, shape)
            .padding(16.dp)
    } else {
        modifier
            .shadow(
                elevation = if (isDark) 8.dp else 6.dp,
                shape = shape,
                spotColor = if (isDark) Color(0x3300C2FF) else Color(0x22000000)
            )
            .clip(shape)
            .background(finalBg)
            .border(1.dp, finalBorder, shape)
            .padding(16.dp)
    }

    Box(modifier = boxModifier) {
        content()
    }
}

@Composable
fun ThemePillToggle(
    currentTheme: ThemeMode,
    onThemeChanged: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = currentTheme == ThemeMode.DARK || (currentTheme == ThemeMode.SYSTEM && AppTheme.isDark)

    val rotation by animateFloatAsState(
        targetValue = if (isDark) 0f else 180f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "theme_icon_rotation"
    )

    val thumbOffset by animateDpAsState(
        targetValue = if (isDark) 4.dp else 36.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "pill_thumb_offset"
    )

    val pillTrackGradient = if (isDark) {
        Brush.horizontalGradient(listOf(Color(0xFF131D33), Color(0xFF1E2942)))
    } else {
        Brush.horizontalGradient(listOf(Color(0xFFE2E8F0), Color(0xFFF1F5F9)))
    }

    val iconColor = if (isDark) BrandCyan else Color(0xFFFF9800)

    Box(
        modifier = modifier
            .size(width = 68.dp, height = 34.dp)
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(17.dp))
            .clip(RoundedCornerShape(17.dp))
            .background(brush = pillTrackGradient)
            .border(
                1.2.dp,
                if (isDark) Color(0x4400C2FF) else Color(0x446C5CE7),
                RoundedCornerShape(17.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                HapticHelper.trigger(context, true, HapticHelper.HapticType.LIGHT)
                val newMode = if (isDark) ThemeMode.LIGHT else ThemeMode.DARK
                onThemeChanged(newMode)
            }
            .testTag("theme_pill_toggle"),
        contentAlignment = Alignment.CenterStart
    ) {
        // Sliding circular pill thumb with icon
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(26.dp)
                .shadow(4.dp, CircleShape)
                .background(
                    if (isDark) Color(0xFF0B1222) else Color(0xFFFFFFFF),
                    CircleShape
                )
                .border(
                    1.dp,
                    if (isDark) Color(0x5500C2FF) else Color(0x33000000),
                    CircleShape
                )
        ) {
            Icon(
                imageVector = if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                contentDescription = if (isDark) "Dark Mode" else "Light Mode",
                tint = iconColor,
                modifier = Modifier
                    .size(15.dp)
                    .rotate(rotation)
            )
        }
    }
}

@Composable
fun CustomSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "custom_switch"
) {
    val isDark = AppTheme.isDark
    val trackWidth = 52.dp
    val trackHeight = 30.dp
    val thumbSize = 22.dp

    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 25.dp else 4.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "switch_thumb_offset"
    )

    val trackGradient = if (checked) {
        Brush.horizontalGradient(listOf(BrandPurple, BrandCyan))
    } else {
        if (isDark) {
            Brush.linearGradient(listOf(Color(0xFF1F293D), Color(0xFF161F30)))
        } else {
            Brush.linearGradient(listOf(Color(0xFFCBD5E1), Color(0xFFE2E8F0)))
        }
    }

    val thumbColor by animateColorAsState(
        targetValue = if (checked) Color.White else (if (isDark) Color(0xFFA0AEC0) else Color(0xFF64748B)),
        animationSpec = tween(durationMillis = 200),
        label = "switch_thumb_color"
    )

    Box(
        modifier = modifier
            .size(width = trackWidth, height = trackHeight)
            .clip(RoundedCornerShape(15.dp))
            .background(brush = trackGradient)
            .border(
                1.dp,
                if (checked) Color(0x6600E5A8) else (if (isDark) Color(0x33FFFFFF) else Color(0x33000000)),
                RoundedCornerShape(15.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onCheckedChange(!checked)
            }
            .testTag(testTag),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(thumbSize)
                .shadow(4.dp, CircleShape)
                .background(thumbColor, CircleShape)
        )
    }
}

@Composable
fun <T> CustomSegmentedPicker(
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    labelProvider: (T) -> String,
    modifier: Modifier = Modifier,
    iconProvider: ((T) -> ImageVector?)? = null
) {
    val context = LocalContext.current
    val isDark = AppTheme.isDark
    val containerBg = if (isDark) Color(0xFF0D1424) else Color(0xFFE2E8F0)
    val containerBorder = if (isDark) Color(0x336C5CE7) else Color(0x336C5CE7)

    val selectedIndex = options.indexOf(selectedOption).coerceAtLeast(0)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .shadow(elevation = if (isDark) 6.dp else 3.dp, shape = RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(containerBg)
            .border(1.2.dp, containerBorder, RoundedCornerShape(24.dp))
            .padding(4.dp)
    ) {
        val totalWidth = maxWidth
        val count = options.size.coerceAtLeast(1)
        val itemWidth = totalWidth / count

        val animatedOffsetX by animateDpAsState(
            targetValue = itemWidth * selectedIndex,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "pill_slider_offset"
        )

        // Smoothly Sliding Pill Indicator
        Box(
            modifier = Modifier
                .offset(x = animatedOffsetX)
                .width(itemWidth)
                .fillMaxHeight()
                .shadow(elevation = 6.dp, shape = RoundedCornerShape(20.dp), spotColor = BrandCyan)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(BrandPurple, BrandCyan)
                    )
                )
                .border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(20.dp))
        )

        // Single clean text + icon row with no overlapping/double layers
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            options.forEachIndexed { index, option ->
                val isSelected = index == selectedIndex

                val textColor by animateColorAsState(
                    targetValue = if (isSelected) Color.White else (if (isDark) Color(0xFFA7B0C0) else Color(0xFF475569)),
                    animationSpec = tween(durationMillis = 200),
                    label = "pill_text_color"
                )

                val icon = iconProvider?.invoke(option)

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(20.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (!isSelected) {
                                HapticHelper.trigger(context, true, HapticHelper.HapticType.LIGHT)
                                onOptionSelected(option)
                            }
                        }
                        .padding(horizontal = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (icon != null) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = textColor,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                        }
                        Text(
                            text = labelProvider(option),
                            color = textColor,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color = BrandCyan,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    GlassCard(
        modifier = modifier,
        cornerRadius = 18.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title.uppercase(),
                    color = AppTheme.textSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .background(iconColor.copy(alpha = 0.15f), CircleShape)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                color = AppTheme.textPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            if (subtitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    color = AppTheme.textSecondary.copy(alpha = 0.8f),
                    fontSize = 11.sp
                )
            }
        }
    }
}
