package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesomeMotion
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.CameraView
import com.example.ui.ActiveSheet
import com.example.ui.AtlasUiState
import com.example.ui.theme.ClinicalIce
import com.example.ui.theme.ClinicalMuted
import com.example.ui.theme.SlateCard
import com.example.ui.theme.SlateElevated
import com.example.ui.theme.SlateHairline
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SurgicalCyan
import com.example.ui.theme.SurgicalCyanDeep
import com.example.ui.theme.VitalAmber
import kotlin.math.roundToInt

@Composable
fun TopHudBar(
    uiState: AtlasUiState,
    onSearchClick: () -> Unit,
    onAboutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            color = SlateSurface.copy(alpha = 0.88f),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, SlateHairline),
            tonalElevation = 4.dp,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (uiState.isSceneReady) SurgicalCyan else VitalAmber),
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Human Atlas",
                            style = MaterialTheme.typography.titleMedium,
                            color = ClinicalIce,
                        )
                        Spacer(Modifier.width(6.dp))
                        Surface(
                            color = SurgicalCyan.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(6.dp),
                        ) {
                            Text(
                                text = "3D",
                                style = MaterialTheme.typography.labelSmall,
                                color = SurgicalCyan,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                            )
                        }
                    }
                    val subtitle = when {
                        uiState.isIsolated -> "ISOLATED · ${uiState.selectedParts.size} PIECE(S)"
                        uiState.explode > 0.05f -> "EXPLODED · ${(uiState.explode * 100).roundToInt()}%"
                        else -> "${uiState.visiblePartCount} / ${uiState.catalog?.parts?.size ?: 2234} MESHES"
                    }
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = ClinicalMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                color = SlateSurface.copy(alpha = 0.90f),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, SlateHairline),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable(onClick = onSearchClick)
                    .testTag("search_button"),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search anatomical structures",
                        tint = SurgicalCyan,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Find",
                        style = MaterialTheme.typography.labelLarge,
                        color = ClinicalIce,
                    )
                }
            }

            Surface(
                color = SlateSurface.copy(alpha = 0.90f),
                shape = CircleShape,
                border = BorderStroke(1.dp, SlateHairline),
            ) {
                IconButton(
                    onClick = onAboutClick,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("about_button"),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "About and credits",
                        tint = ClinicalMuted,
                    )
                }
            }
        }
    }
}

@Composable
fun CameraQuickControls(
    currentView: CameraView,
    explode: Float,
    autoRotate: Boolean,
    isIsolated: Boolean,
    onSelectView: (CameraView) -> Unit,
    onToggleAutoRotate: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = SlateSurface.copy(alpha = 0.86f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, SlateHairline),
        tonalElevation = 4.dp,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            CameraView.entries.forEach { view ->
                val enabled = !(explode > 0.8f && view != CameraView.FRONT)
                val selected = currentView == view
                Surface(
                    color = if (selected) SurgicalCyan.copy(alpha = 0.18f) else SlateSurface.copy(alpha = 0f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(enabled = enabled) { onSelectView(view) }
                        .testTag("camera_${view.jsValue}"),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = view.shortLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = when {
                                !enabled -> ClinicalMuted.copy(alpha = 0.35f)
                                selected -> SurgicalCyan
                                else -> ClinicalIce
                            },
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .width(28.dp)
                    .height(1.dp)
                    .background(SlateHairline),
            )

            val rotateEnabled = explode < 0.4f && !isIsolated
            IconButton(
                onClick = onToggleAutoRotate,
                enabled = rotateEnabled,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("auto_rotate_button"),
            ) {
                Icon(
                    imageVector = if (autoRotate) Icons.Filled.Pause else Icons.Filled.RotateRight,
                    contentDescription = if (autoRotate) "Pause auto rotation" else "Auto rotate model",
                    tint = when {
                        !rotateEnabled -> ClinicalMuted.copy(alpha = 0.35f)
                        autoRotate -> SurgicalCyan
                        else -> ClinicalIce
                    },
                )
            }

            IconButton(
                onClick = onReset,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("reset_camera_button"),
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Reset camera and layers",
                    tint = ClinicalMuted,
                )
            }
        }
    }
}

@Composable
fun BottomControlDock(
    uiState: AtlasUiState,
    onExplodeChange: (Float) -> Unit,
    onToggleExplodeShortcut: () -> Unit,
    onOpenSystems: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.widthIn(max = 560.dp).fillMaxWidth(),
        color = SlateSurface.copy(alpha = 0.94f),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, SlateHairline),
        tonalElevation = 8.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "EXPLODE ANATOMY",
                    style = MaterialTheme.typography.labelSmall,
                    color = ClinicalMuted,
                )
                Text(
                    text = "${(uiState.explode * 100).roundToInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (uiState.explode > 0.05f) SurgicalCyan else ClinicalIce,
                )
            }

            Slider(
                value = uiState.explode,
                onValueChange = onExplodeChange,
                valueRange = 0f..1f,
                colors = SliderDefaults.colors(
                    thumbColor = SurgicalCyan,
                    activeTrackColor = SurgicalCyan,
                    inactiveTrackColor = SlateElevated,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("explode_slider"),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DockActionButton(
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Layers,
                            contentDescription = "Systems",
                            modifier = Modifier.size(18.dp),
                        )
                    },
                    label = "Systems",
                    active = uiState.activeSheet == ActiveSheet.SYSTEMS,
                    onClick = onOpenSystems,
                    modifier = Modifier.testTag("systems_button"),
                )

                DockActionButton(
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesomeMotion,
                            contentDescription = "Toggle exploded view",
                            modifier = Modifier.size(18.dp),
                        )
                    },
                    label = if (uiState.explode > 0.1f) "Assemble" else "Explode",
                    active = uiState.explode > 0.1f,
                    onClick = onToggleExplodeShortcut,
                    modifier = Modifier.testTag("explode_toggle_button"),
                )

                DockActionButton(
                    icon = {
                        BadgedBox(
                            badge = {
                                if (uiState.bookmarks.isNotEmpty()) {
                                    Badge(
                                        containerColor = VitalAmber,
                                        contentColor = SlateSurface,
                                    ) {
                                        Text("${uiState.bookmarks.size}")
                                    }
                                }
                            },
                        ) {
                            Icon(
                                imageVector = if (uiState.bookmarks.isNotEmpty()) {
                                    Icons.Filled.Bookmark
                                } else {
                                    Icons.Outlined.BookmarkBorder
                                },
                                contentDescription = "Bookmarks",
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    },
                    label = "Saved",
                    active = uiState.activeSheet == ActiveSheet.BOOKMARKS,
                    onClick = onOpenBookmarks,
                    modifier = Modifier.testTag("bookmarks_button"),
                )

                DockActionButton(
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Reset atlas",
                            modifier = Modifier.size(18.dp),
                        )
                    },
                    label = "Reset",
                    active = false,
                    onClick = onReset,
                    modifier = Modifier.testTag("dock_reset_button"),
                )
            }
        }
    }
}

@Composable
private fun DockActionButton(
    icon: @Composable () -> Unit,
    label: String,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = if (active) SurgicalCyan.copy(alpha = 0.16f) else SlateCard.copy(alpha = 0.55f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (active) SurgicalCyan.copy(alpha = 0.55f) else SlateHairline,
        ),
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                androidx.compose.runtime.CompositionLocalProvider(
                    androidx.compose.material3.LocalContentColor provides if (active) SurgicalCyan else ClinicalIce,
                ) {
                    icon()
                }
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (active) SurgicalCyan else ClinicalIce,
                maxLines = 1,
            )
        }
    }
}

@Composable
fun LoadingProgressOverlay(
    visible: Boolean,
    progress: Int,
    partCount: Int,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier,
    ) {
        Surface(
            color = SlateSurface.copy(alpha = 0.94f),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, SlateHairline),
            tonalElevation = 8.dp,
            modifier = Modifier.widthIn(max = 320.dp).fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Hydrating 3D Anatomy",
                        style = MaterialTheme.typography.titleSmall,
                        color = ClinicalIce,
                    )
                    Text(
                        text = "$progress%",
                        style = MaterialTheme.typography.labelMedium,
                        color = SurgicalCyan,
                    )
                }
                LinearProgressIndicator(
                    progress = { (progress / 100f).coerceIn(0f, 1f) },
                    color = SurgicalCyan,
                    trackColor = SlateElevated,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                )
                Text(
                    text = "Streaming $partCount BodyParts3D meshes into WebGL buffers",
                    style = MaterialTheme.typography.bodySmall,
                    color = ClinicalMuted,
                )
            }
        }
    }
}

@Composable
fun SceneErrorOverlay(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.widthIn(max = 340.dp).fillMaxWidth(),
        color = SlateSurface.copy(alpha = 0.96f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "3D Viewport Interrupted",
                style = MaterialTheme.typography.titleMedium,
                color = ClinicalIce,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = ClinicalMuted,
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SurgicalCyan,
                    contentColor = SurgicalCyanDeep,
                ),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("retry_scene_button"),
            ) {
                Text("Reload 3D Engine")
            }
        }
    }
}
