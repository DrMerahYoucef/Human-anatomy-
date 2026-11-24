package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.BookmarkedStructure
import com.example.data.model.AnatomicalConcept
import com.example.data.model.AnatomicalPart
import com.example.data.model.AtlasExplanations
import com.example.data.model.SystemId
import com.example.ui.theme.BioTeal
import com.example.ui.theme.ClinicalIce
import com.example.ui.theme.ClinicalMuted
import com.example.ui.theme.SlateCard
import com.example.ui.theme.SlateHairline
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SurgicalCyan
import com.example.ui.theme.SurgicalCyanDeep
import com.example.ui.theme.VitalAmber

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StructureInspectorCard(
    concept: AnatomicalConcept,
    selectedParts: List<AnatomicalPart>,
    system: SystemId,
    isIsolated: Boolean,
    bookmark: BookmarkedStructure?,
    bookmarkKey: String,
    onToggleIsolate: () -> Unit,
    onClearSelection: () -> Unit,
    onSelectSubPart: (String) -> Unit,
    onToggleBookmark: (String) -> Unit,
    onSaveStudyNote: (String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable(concept.id) { mutableStateOf(true) }
    var noteText by rememberSaveable(bookmarkKey) { mutableStateOf(bookmark?.studyNote.orEmpty()) }
    var noteSavedFeedback by rememberSaveable(bookmarkKey) { mutableStateOf(false) }

    LaunchedEffect(bookmark?.studyNote, bookmarkKey) {
        if (bookmark != null && noteText != bookmark.studyNote) {
            noteText = bookmark.studyNote
        }
    }

    val (explanationText, isCurated) = AtlasExplanations.resolve(concept.name, system)

    Surface(
        modifier = modifier
            .widthIn(max = 560.dp)
            .fillMaxWidth()
            .testTag("structure_inspector_card"),
        color = SlateSurface.copy(alpha = 0.96f),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, SlateHairline),
        tonalElevation = 10.dp,
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 18.dp, vertical = 14.dp)
                .heightIn(max = 420.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(system.swatchColor),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = system.displayName.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = BioTeal,
                    )
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        color = SlateCard,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, SlateHairline),
                    ) {
                        Text(
                            text = concept.id,
                            style = MaterialTheme.typography.labelSmall,
                            color = ClinicalMuted,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onToggleBookmark(noteText) },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("inspector_bookmark_button"),
                    ) {
                        Icon(
                            imageVector = if (bookmark != null) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = if (bookmark != null) "Remove bookmark" else "Bookmark structure",
                            tint = if (bookmark != null) VitalAmber else ClinicalMuted,
                        )
                    }
                    IconButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("inspector_expand_button"),
                    ) {
                        Icon(
                            imageVector = if (expanded) Icons.Filled.ExpandMore else Icons.Filled.ExpandLess,
                            contentDescription = if (expanded) "Collapse details" else "Expand details",
                            tint = ClinicalMuted,
                        )
                    }
                    IconButton(
                        onClick = onClearSelection,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("inspector_close_button"),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Clear selection",
                            tint = ClinicalMuted,
                        )
                    }
                }
            }

            Text(
                text = concept.name.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.headlineSmall,
                color = ClinicalIce,
                maxLines = if (expanded) 3 else 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    onClick = onToggleIsolate,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isIsolated) BioTeal else SurgicalCyan,
                        contentColor = SurgicalCyanDeep,
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("inspector_isolate_button"),
                ) {
                    Icon(
                        imageVector = Icons.Filled.CenterFocusStrong,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isIsolated) "Show Surrounding" else "Isolate Structure",
                        maxLines = 1,
                    )
                }

                OutlinedButton(
                    onClick = onClearSelection,
                    border = BorderStroke(1.dp, SlateHairline),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("inspector_clear_button"),
                ) {
                    Text("Clear", color = ClinicalIce)
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text(
                        text = explanationText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = ClinicalIce.copy(alpha = 0.9f),
                    )
                    if (!isCurated) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "System overview · ${selectedParts.size} modeled ${if (selectedParts.size == 1) "mesh" else "meshes"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = ClinicalMuted,
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = noteText,
                        onValueChange = {
                            noteText = it
                            noteSavedFeedback = false
                        },
                        label = { Text("Personal Study Note") },
                        placeholder = { Text("Add clinical notes, attachments, or exam cues…") },
                        minLines = 2,
                        maxLines = 4,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SurgicalCyan,
                            unfocusedBorderColor = SlateHairline,
                            focusedContainerColor = SlateCard,
                            unfocusedContainerColor = SlateCard,
                            focusedTextColor = ClinicalIce,
                            unfocusedTextColor = ClinicalIce,
                            cursorColor = SurgicalCyan,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("inspector_note_input"),
                    )

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = when {
                                noteSavedFeedback -> "Saved to device"
                                bookmark != null -> "Bookmarked locally"
                                else -> "Saving note also bookmarks structure"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (noteSavedFeedback) BioTeal else ClinicalMuted,
                        )
                        Button(
                            onClick = {
                                onSaveStudyNote(bookmarkKey, noteText)
                                noteSavedFeedback = true
                            },
                            enabled = noteText.isNotBlank() || bookmark != null,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SlateCard,
                                contentColor = SurgicalCyan,
                            ),
                            border = BorderStroke(1.dp, SurgicalCyan.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("inspector_save_note_button"),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Save,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Save Note")
                        }
                    }

                    if (selectedParts.size > 1) {
                        Spacer(Modifier.height(14.dp))
                        Text(
                            text = "INCLUDED MESHES (${selectedParts.size})",
                            style = MaterialTheme.typography.labelSmall,
                            color = ClinicalMuted,
                        )
                        Spacer(Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            selectedParts.take(20).forEach { part ->
                                Surface(
                                    color = SlateCard,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, SlateHairline),
                                    modifier = Modifier
                                        .heightIn(min = 40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { onSelectSubPart(part.id) }
                                        .testTag("subpart_${part.id}"),
                                ) {
                                    Text(
                                        text = part.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ClinicalIce,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
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
