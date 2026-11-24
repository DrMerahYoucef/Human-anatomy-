package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.BookmarkedStructure
import com.example.data.model.SystemId
import com.example.ui.theme.ClinicalIce
import com.example.ui.theme.ClinicalMuted
import com.example.ui.theme.SlateCard
import com.example.ui.theme.SlateHairline
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SurgicalCyan
import com.example.ui.theme.SurgicalCyanDeep
import com.example.ui.theme.VitalAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksBottomSheet(
    bookmarks: List<BookmarkedStructure>,
    onOpenBookmark: (BookmarkedStructure) -> Unit,
    onUpdateNote: (String, String) -> Unit,
    onDeleteBookmark: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SlateSurface,
        contentColor = ClinicalIce,
    ) {
        BookmarksPanelContent(
            bookmarks = bookmarks,
            onOpenBookmark = onOpenBookmark,
            onUpdateNote = onUpdateNote,
            onDeleteBookmark = onDeleteBookmark,
            onClose = onDismiss,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun BookmarksPanelContent(
    bookmarks: List<BookmarkedStructure>,
    onOpenBookmark: (BookmarkedStructure) -> Unit,
    onUpdateNote: (String, String) -> Unit,
    onDeleteBookmark: (String) -> Unit,
    onClose: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    var editingTarget by remember { mutableStateOf<BookmarkedStructure?>(null) }

    Column(modifier = modifier.padding(horizontal = 20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "LOCAL STUDY LIBRARY",
                    style = MaterialTheme.typography.labelSmall,
                    color = VitalAmber,
                )
                Text(
                    text = "Bookmarks & Notes",
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
                        contentDescription = "Close bookmarks",
                        tint = ClinicalMuted,
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        if (bookmarks.isEmpty()) {
            Surface(
                color = SlateCard,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, SlateHairline),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 28.dp),
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_bookmarks_empty),
                        contentDescription = "Anatomical wireframe illustration",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(14.dp)),
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "No Saved Structures Yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = ClinicalIce,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Tap any structure on the 3D body or via Search, then tap the Bookmark icon to save it with study notes offline on your device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ClinicalMuted,
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(bookmarks, key = { it.structureId }) { item ->
                    val sysColor = SystemId.fromId(item.systemId)?.swatchColor ?: SurgicalCyan
                    Surface(
                        color = SlateCard,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, SlateHairline),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onOpenBookmark(item) }
                            .testTag("bookmark_item_${item.structureId}"),
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
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
                                            .background(sysColor),
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = item.name.replaceFirstChar { it.uppercase() },
                                            style = MaterialTheme.typography.titleSmall,
                                            color = ClinicalIce,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        Text(
                                            text = "${item.systemName} · ${item.conceptId}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ClinicalMuted,
                                        )
                                    }
                                }

                                Row {
                                    IconButton(
                                        onClick = { editingTarget = item },
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .testTag("bookmark_edit_${item.structureId}"),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.EditNote,
                                            contentDescription = "Edit study note",
                                            tint = SurgicalCyan,
                                        )
                                    }
                                    IconButton(
                                        onClick = { onDeleteBookmark(item.structureId) },
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .testTag("bookmark_delete_${item.structureId}"),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.DeleteOutline,
                                            contentDescription = "Delete bookmark",
                                            tint = ClinicalMuted,
                                        )
                                    }
                                }
                            }

                            if (item.studyNote.isNotBlank()) {
                                Spacer(Modifier.height(6.dp))
                                Surface(
                                    color = SlateSurface,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, SlateHairline),
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.Top,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Bookmark,
                                            contentDescription = null,
                                            tint = VitalAmber,
                                            modifier = Modifier.size(14.dp),
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = item.studyNote,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = ClinicalIce.copy(alpha = 0.9f),
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

    editingTarget?.let { target ->
        var draft by remember(target.structureId) { mutableStateOf(target.studyNote) }
        AlertDialog(
            onDismissRequest = { editingTarget = null },
            containerColor = SlateSurface,
            titleContentColor = ClinicalIce,
            textContentColor = ClinicalMuted,
            title = {
                Text(
                    text = target.name.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleLarge,
                )
            },
            text = {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    label = { Text("Study Note") },
                    minLines = 3,
                    maxLines = 6,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SurgicalCyan,
                        unfocusedBorderColor = SlateHairline,
                        focusedContainerColor = SlateCard,
                        unfocusedContainerColor = SlateCard,
                        focusedTextColor = ClinicalIce,
                        unfocusedTextColor = ClinicalIce,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateNote(target.structureId, draft)
                        editingTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SurgicalCyan,
                        contentColor = SurgicalCyanDeep,
                    ),
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { editingTarget = null },
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text("Cancel", color = ClinicalMuted)
                }
            },
        )
    }
}
