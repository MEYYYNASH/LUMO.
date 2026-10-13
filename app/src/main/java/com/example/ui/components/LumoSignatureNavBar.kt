package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalColorReactiveState
import com.example.ui.theme.LocalLumoLanguage
import com.example.ui.theme.LocalLumoPalette
import com.example.ui.theme.LumoDestination
import com.example.ui.theme.LumoIcons

@Composable
fun LumoSignatureBottomBar(
    currentDestination: LumoDestination,
    activeDownloadsCount: Int,
    onSelectDestination: (LumoDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalLumoPalette.current
    val reactive = LocalColorReactiveState.current
    val mult = reactive.intensityMode.reactionMultiplier

    Surface(
        color = palette.surface.copy(alpha = 0.96f),
        tonalElevation = 8.dp,
        shadowElevation = 16.dp,
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("signature_bottom_nav")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(palette.surface)
                .navigationBarsPadding()
        ) {
            // Subtle music-reactive top hairline divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                palette.divider.copy(alpha = 0.45f),
                                lerp(palette.divider, reactive.reactiveAccentColor, (0.35f + reactive.beatPulse * 0.35f) * mult),
                                palette.divider.copy(alpha = 0.45f)
                            )
                        )
                    )
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LumoDestination.entries.forEach { destination ->
                    val isSelected = destination == currentDestination
                    val baseTabAccent = palette.accentForTab(destination)
                    // Harmonize the active navigation pill with the current song's artwork palette
                    val reactiveTabAccent = if (reactive.isPlaying) {
                        lerp(baseTabAccent, reactive.reactiveAccentColor, 0.42f * mult)
                    } else {
                        baseTabAccent
                    }

                    SignatureExpandingPillItem(
                        destination = destination,
                        isSelected = isSelected,
                        accentColor = reactiveTabAccent,
                        inactiveColor = palette.secondaryText,
                        beatPulse = if (isSelected && reactive.isPlaying) reactive.beatPulse * mult else 0f,
                        bassLevel = if (isSelected && reactive.isPlaying) reactive.bassLevel * mult else 0f,
                        badgeCount = if (destination == LumoDestination.DOWNLOADS) activeDownloadsCount else 0,
                        onClick = { onSelectDestination(destination) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SignatureExpandingPillItem(
    destination: LumoDestination,
    isSelected: Boolean,
    accentColor: Color,
    inactiveColor: Color,
    beatPulse: Float,
    bassLevel: Float,
    badgeCount: Int,
    onClick: () -> Unit
) {
    val pillHorizontalPadding by animateDpAsState(
        targetValue = if (isSelected) 18.dp else 12.dp,
        animationSpec = spring(
            dampingRatio = 0.78f,
            stiffness = 340f
        ),
        label = "pillHorizontalPadding"
    )

    val reactiveBgAlpha = (0.16f + bassLevel * 0.08f + beatPulse * 0.08f).coerceIn(0.14f, 0.32f)
    val reactiveBorderAlpha = (0.45f + beatPulse * 0.32f).coerceIn(0.40f, 0.85f)

    val pillBgColor by animateColorAsState(
        targetValue = if (isSelected) accentColor.copy(alpha = reactiveBgAlpha) else Color.Transparent,
        animationSpec = tween(durationMillis = 240),
        label = "pillBgColor"
    )

    val pillBorderColor by animateColorAsState(
        targetValue = if (isSelected) accentColor.copy(alpha = reactiveBorderAlpha) else Color.Transparent,
        animationSpec = tween(durationMillis = 240),
        label = "pillBorderColor"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) accentColor else inactiveColor,
        animationSpec = tween(durationMillis = 240),
        label = "pillContentColor"
    )

    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.06f + (beatPulse * 0.04f) else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "iconScale"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .scale(if (isSelected) 1.0f + beatPulse * 0.025f else 1.0f)
            .drawBehind {
                if (isSelected && (bassLevel > 0.05f || beatPulse > 0.05f)) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                accentColor.copy(alpha = (0.14f + beatPulse * 0.16f).coerceIn(0f, 0.32f)),
                                Color.Transparent
                            )
                        ),
                        radius = size.maxDimension * 0.65f
                    )
                }
            }
            .clip(CircleShape)
            .background(pillBgColor, CircleShape)
            .border(width = 1.dp, color = pillBorderColor, shape = CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = accentColor),
                role = Role.Tab,
                onClick = onClick
            )
            .semantics {
                selected = isSelected
                contentDescription = destination.label
            }
            .padding(horizontal = pillHorizontalPadding, vertical = 10.dp)
            .testTag(destination.testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            BadgedBox(
                badge = {
                    if (badgeCount > 0) {
                        Badge(
                            containerColor = accentColor,
                            contentColor = Color.White
                        ) {
                            Text(text = badgeCount.toString())
                        }
                    }
                }
            ) {
                Icon(
                    imageVector = destinationIcon(destination, isSelected),
                    contentDescription = destination.label,
                    tint = contentColor,
                    modifier = Modifier
                        .size(22.dp)
                        .scale(iconScale)
                )
            }

            val language = LocalLumoLanguage.current
            AnimatedVisibility(
                visible = isSelected,
                enter = expandHorizontally(
                    animationSpec = spring(
                        dampingRatio = 0.80f,
                        stiffness = 340f
                    ),
                    expandFrom = Alignment.Start
                ) + fadeIn(animationSpec = tween(durationMillis = 240)),
                exit = shrinkHorizontally(
                    animationSpec = spring(
                        dampingRatio = 0.85f,
                        stiffness = 380f
                    ),
                    shrinkTowards = Alignment.Start
                ) + fadeOut(animationSpec = tween(durationMillis = 180))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = destination.displayLabel(language),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = contentColor,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun LumoAdaptiveSideRail(
    currentDestination: LumoDestination,
    activeDownloadsCount: Int,
    onSelectDestination: (LumoDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalLumoPalette.current
    val reactive = LocalColorReactiveState.current
    val mult = reactive.intensityMode.reactionMultiplier

    Surface(
        color = palette.surface,
        modifier = modifier
            .fillMaxHeight()
            .width(196.dp)
            .testTag("signature_side_rail")
    ) {
        Row(modifier = Modifier.fillMaxHeight()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 14.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .scale(if (reactive.isPlaying) 1f + reactive.beatPulse * 0.25f * mult else 1f)
                            .clip(CircleShape)
                            .background(reactive.reactiveAccentColor)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "LUMO",
                        style = MaterialTheme.typography.headlineMedium,
                        color = palette.primaryText
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LumoDestination.entries.forEach { destination ->
                    val isSelected = destination == currentDestination
                    val baseAccent = palette.accentForTab(destination)
                    val tabAccent = if (reactive.isPlaying) {
                        lerp(baseAccent, reactive.reactiveAccentColor, 0.42f * mult)
                    } else baseAccent

                    val bg by animateColorAsState(
                        targetValue = if (isSelected) tabAccent.copy(alpha = 0.16f + reactive.beatPulse * 0.08f * mult) else Color.Transparent,
                        label = "railBg"
                    )
                    val tint by animateColorAsState(
                        targetValue = if (isSelected) tabAccent else palette.secondaryText,
                        label = "railTint"
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(20.dp))
                            .background(bg)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) tabAccent.copy(alpha = 0.45f + reactive.beatPulse * 0.25f * mult) else Color.Transparent,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { onSelectDestination(destination) }
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                            .testTag("${destination.testTag}_rail")
                    ) {
                        Icon(
                            imageVector = destinationIcon(destination, isSelected),
                            contentDescription = destination.label,
                            tint = tint,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = destination.displayLabel(LocalLumoLanguage.current),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isSelected) tint else palette.secondaryText,
                            modifier = Modifier.weight(1f)
                        )
                        if (destination == LumoDestination.DOWNLOADS && activeDownloadsCount > 0) {
                            Badge(containerColor = tabAccent, contentColor = Color.White) {
                                Text(activeDownloadsCount.toString())
                            }
                        }
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .background(palette.divider)
            )
        }
    }
}

private fun destinationIcon(destination: LumoDestination, isSelected: Boolean): ImageVector =
    when (destination) {
        LumoDestination.HOME -> if (isSelected) Icons.Filled.Home else Icons.Outlined.Home
        LumoDestination.SEARCH -> if (isSelected) Icons.Filled.Search else Icons.Outlined.Search
        LumoDestination.LIBRARY -> LumoIcons.MusicNote
        LumoDestination.DOWNLOADS -> LumoIcons.Download
    }
