package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AtlasPreset
import com.example.data.model.SystemId
import com.example.data.model.SystemRenderMode
import com.example.ui.theme.BioTeal
import com.example.ui.theme.ClinicalIce
import com.example.ui.theme.ClinicalMuted
import com.example.ui.theme.SlateCard
import com.example.ui.theme.SlateElevated
import com.example.ui.theme.SlateHairline
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SurgicalCyan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemsBottomSheet(
    systemModes: Map<SystemId, SystemRenderMode>,
    partCounts: Map<SystemId, Int>,
    activePreset: AtlasPreset?,
    onApplyPreset: (AtlasPreset) -> Unit,
    onSetSystemMode: (SystemId, SystemRenderMode) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SlateSurface,
        contentColor = ClinicalIce,
    ) {
        SystemsPanelContent(
            systemModes = systemModes,
            partCounts = partCounts,
            activePreset = activePreset,
            onApplyPreset = onApplyPreset,
            onSetSystemMode = onSetSystemMode,
            onClose = onDismiss,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun SystemsPanelContent(
    systemModes: Map<SystemId, SystemRenderMode>,
    partCounts: Map<SystemId, Int>,
    activePreset: AtlasPreset?,
    onApplyPreset: (AtlasPreset) -> Unit,
    onSetSystemMode: (SystemId, SystemRenderMode) -> Unit,
    onClose: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(horizontal = 20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "ANATOMICAL LAYERS",
                    style = MaterialTheme.typography.labelSmall,
                    color = SurgicalCyan,
                )
                Text(
                    text = "Systems & X-Ray",
                    style = MaterialTheme.typography.headlineSmall,
                    color = ClinicalIce,
                )
            }
            if (onClose != null) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.minimumInteractiveComponentSize(),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close systems panel",
                        tint = ClinicalMuted,
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AtlasPreset.entries.forEach { preset ->
                val selected = activePreset == preset
                Surface(
                    color = if (selected) SurgicalCyan.copy(alpha = 0.18f) else SlateCard,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(
                        1.dp,
                        if (selected) SurgicalCyan else SlateHairline,
                    ),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onApplyPreset(preset) }
                        .testTag("preset_${preset.name.lowercase()}"),
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = preset.label,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selected) SurgicalCyan else ClinicalIce,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        LazyColumn(
            contentPadding = PaddingValues(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(SystemId.entries, key = { it.id }) { sys ->
                val mode = systemModes[sys] ?: SystemRenderMode.HIDDEN
                val count = partCounts[sys] ?: 0
                Surface(
                    color = SlateCard,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        1.dp,
                        if (mode == SystemRenderMode.HIDDEN) SlateHairline else SlateElevated,
                    ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (mode == SystemRenderMode.HIDDEN) {
                                            sys.swatchColor.copy(alpha = 0.3f)
                                        } else {
                                            sys.swatchColor
                                        },
                                    ),
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = sys.displayName,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (mode == SystemRenderMode.HIDDEN) ClinicalMuted else ClinicalIce,
                                )
                                Text(
                                    text = "$count structures",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ClinicalMuted,
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            SystemRenderMode.entries.forEach { targetMode ->
                                val isSelected = mode == targetMode
                                val accent = when (targetMode) {
                                    SystemRenderMode.SOLID -> SurgicalCyan
                                    SystemRenderMode.XRAY -> BioTeal
                                    SystemRenderMode.HIDDEN -> ClinicalMuted
                                }
                                Surface(
                                    color = if (isSelected) accent.copy(alpha = 0.2f) else SlateSurface,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) accent else SlateHairline,
                                    ),
                                    modifier = Modifier
                                        .heightIn(min = 48.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { onSetSystemMode(sys, targetMode) }
                                        .testTag("system_${sys.id}_${targetMode.name.lowercase()}"),
                                ) {
                                    Box(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = targetMode.label,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isSelected) accent else ClinicalMuted,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
