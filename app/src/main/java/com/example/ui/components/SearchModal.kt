package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.AnatomicalConcept
import com.example.ui.theme.ClinicalIce
import com.example.ui.theme.ClinicalMuted
import com.example.ui.theme.SlateCard
import com.example.ui.theme.SlateHairline
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SurgicalCyan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBottomSheet(
    query: String,
    results: List<AnatomicalConcept>,
    onQueryChange: (String) -> Unit,
    onSelectConcept: (AnatomicalConcept) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SlateSurface,
        contentColor = ClinicalIce,
    ) {
        SearchPanelContent(
            query = query,
            results = results,
            onQueryChange = onQueryChange,
            onSelectConcept = onSelectConcept,
            onClose = onDismiss,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun SearchPanelContent(
    query: String,
    results: List<AnatomicalConcept>,
    onQueryChange: (String) -> Unit,
    onSelectConcept: (AnatomicalConcept) -> Unit,
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
                    text = "FMA ANATOMY INDEX",
                    style = MaterialTheme.typography.labelSmall,
                    color = SurgicalCyan,
                )
                Text(
                    text = "Find a Structure",
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
                        contentDescription = "Close search",
                        tint = ClinicalMuted,
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Heart, femur, cranial nerve, FMA7088…") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    tint = SurgicalCyan,
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = { onQueryChange("") },
                        modifier = Modifier.minimumInteractiveComponentSize(),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Clear search query",
                            tint = ClinicalMuted,
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
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
                .testTag("search_input"),
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = if (query.isBlank()) {
                "Featured organs & landmarks — or search 3,432 named FMA concepts."
            } else {
                "Showing ${results.size} match(es) — tap to highlight in 3D."
            },
            style = MaterialTheme.typography.bodySmall,
            color = ClinicalMuted,
        )

        Spacer(Modifier.height(10.dp))

        LazyColumn(
            contentPadding = PaddingValues(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (results.isEmpty()) {
                item {
                    Surface(
                        color = SlateCard,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, SlateHairline),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = "No anatomical concepts match \"$query\". Try an organ, bone, vessel, or FMA ID.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ClinicalMuted,
                            modifier = Modifier.padding(18.dp),
                        )
                    }
                }
            } else {
                items(results, key = { it.id }) { concept ->
                    Surface(
                        color = SlateCard,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, SlateHairline),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onSelectConcept(concept) }
                            .testTag("search_result_${concept.id}"),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = concept.name.replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.titleSmall,
                                    color = ClinicalIce,
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = "${concept.id} · ${concept.elements.size} ${if (concept.elements.size == 1) "mesh" else "meshes"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ClinicalMuted,
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Inspect ${concept.name}",
                                tint = SurgicalCyan,
                            )
                        }
                    }
                }
            }
        }
    }
}
