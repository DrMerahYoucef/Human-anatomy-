package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesomeMotion
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.SceneViewportInsets
import com.example.data.model.SystemId
import com.example.ui.components.AboutCreditsBottomSheet
import com.example.ui.components.AtlasWebView
import com.example.ui.components.BookmarksBottomSheet
import com.example.ui.components.BookmarksPanelContent
import com.example.ui.components.BottomControlDock
import com.example.ui.components.CameraQuickControls
import com.example.ui.components.LoadingProgressOverlay
import com.example.ui.components.SceneErrorOverlay
import com.example.ui.components.SearchBottomSheet
import com.example.ui.components.SearchPanelContent
import com.example.ui.components.StructureInspectorCard
import com.example.ui.components.SystemsBottomSheet
import com.example.ui.components.SystemsPanelContent
import com.example.ui.components.TopHudBar
import com.example.ui.theme.ClinicalIce
import com.example.ui.theme.ClinicalMuted
import com.example.ui.theme.SlateAbyss
import com.example.ui.theme.SlateElevated
import com.example.ui.theme.SlateHairline
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SurgicalCyan
import com.example.ui.theme.SurgicalCyanDeep
import com.example.ui.theme.VitalAmber
import kotlin.math.roundToInt

@Composable
fun AtlasScreen(
    viewModel: AtlasViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sceneJson = remember(
        uiState.explode,
        uiState.systemModes,
        uiState.selectedPartIds,
        uiState.isIsolated,
        uiState.cameraView,
        uiState.autoRotate,
        uiState.resetCounter,
        uiState.activeSheet,
        uiState.viewportInsets,
    ) {
        uiState.toSceneStateJson()
    }

    BackHandler(
        enabled = uiState.activeSheet != ActiveSheet.NONE || uiState.selectedPartIds.isNotEmpty(),
    ) {
        when {
            uiState.activeSheet == ActiveSheet.INSPECTOR -> viewModel.clearSelection()
            uiState.activeSheet != ActiveSheet.NONE -> viewModel.closeSheet()
            else -> viewModel.clearSelection()
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(SlateAbyss),
    ) {
        val isWide = maxWidth >= 700.dp
        val sidePanelVisible = isWide && (
            uiState.activeSheet == ActiveSheet.SYSTEMS ||
                uiState.activeSheet == ActiveSheet.SEARCH ||
                uiState.activeSheet == ActiveSheet.BOOKMARKS ||
                (uiState.activeSheet == ActiveSheet.INSPECTOR && uiState.selectedConcept != null)
            )

        LaunchedEffect(isWide, sidePanelVisible, uiState.activeSheet) {
            val insets = when {
                isWide && sidePanelVisible -> SceneViewportInsets(left = 96, top = 72, right = 380, bottom = 96)
                isWide -> SceneViewportInsets(left = 96, top = 72, right = 88, bottom = 96)
                uiState.activeSheet == ActiveSheet.INSPECTOR -> SceneViewportInsets(left = 16, top = 88, right = 16, bottom = 320)
                else -> SceneViewportInsets(left = 16, top = 88, right = 72, bottom = 190)
            }
            viewModel.updateViewportInsets(insets)
        }

        // Full-bleed Three.js + BodyParts3D WebGL2 Canvas
        AtlasWebView(
            sceneStateJson = sceneJson,
            reloadToken = uiState.webReloadToken,
            onProgress = viewModel::onWebProgress,
            onReady = viewModel::onSceneReady,
            onPartSelected = viewModel::onPartTappedInScene,
            onError = viewModel::onSceneError,
            modifier = Modifier.fillMaxSize(),
        )

        if (isWide) {
            WideAtlasLayout(
                uiState = uiState,
                sidePanelVisible = sidePanelVisible,
                viewModel = viewModel,
            )
        } else {
            CompactAtlasLayout(
                uiState = uiState,
                viewModel = viewModel,
            )
        }

        // Loading / Error Overlays
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            val errorMsg = uiState.sceneError ?: (uiState.catalogState as? CatalogState.Error)?.message
            when {
                errorMsg != null -> {
                    SceneErrorOverlay(
                        message = errorMsg,
                        onRetry = {
                            if (uiState.catalogState is CatalogState.Error) {
                                viewModel.loadCatalog()
                            } else {
                                viewModel.retryWebScene()
                            }
                        },
                    )
                }
                !uiState.isSceneReady -> {
                    LoadingProgressOverlay(
                        visible = true,
                        progress = uiState.webLoadProgress,
                        partCount = uiState.catalog?.parts?.size ?: 2234,
                    )
                }
            }
        }

        if (uiState.activeSheet == ActiveSheet.ABOUT) {
            AboutCreditsBottomSheet(
                partCount = uiState.catalog?.parts?.size ?: 2234,
                conceptCount = uiState.catalog?.concepts?.size ?: 3432,
                triangleCount = uiState.catalog?.totalTriangles ?: 2288268,
                onDismiss = viewModel::closeSheet,
            )
        }
    }
}

@Composable
private fun CompactAtlasLayout(
    uiState: AtlasUiState,
    viewModel: AtlasViewModel,
) {
    val showInspector = uiState.activeSheet == ActiveSheet.INSPECTOR && uiState.selectedConcept != null

    Box(modifier = Modifier.fillMaxSize()) {
        TopHudBar(
            uiState = uiState,
            onSearchClick = { viewModel.setActiveSheet(ActiveSheet.SEARCH) },
            onAboutClick = { viewModel.setActiveSheet(ActiveSheet.ABOUT) },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 10.dp),
        )

        AnimatedVisibility(
            visible = !showInspector,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp, bottom = 72.dp),
        ) {
            CameraQuickControls(
                currentView = uiState.cameraView,
                explode = uiState.explode,
                autoRotate = uiState.autoRotate,
                isIsolated = uiState.isIsolated,
                onSelectView = viewModel::setCameraView,
                onToggleAutoRotate = viewModel::toggleAutoRotate,
                onReset = viewModel::resetScene,
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            AnimatedVisibility(
                visible = showInspector,
                enter = slideInVertically { it / 2 } + fadeIn(),
                exit = slideOutVertically { it / 2 } + fadeOut(),
            ) {
                val concept = uiState.selectedConcept
                if (concept != null) {
                    StructureInspectorCard(
                        concept = concept,
                        selectedParts = uiState.selectedParts,
                        system = uiState.primarySelectedSystem ?: SystemId.CONNECTIVE,
                        isIsolated = uiState.isIsolated,
                        bookmark = uiState.currentBookmark,
                        bookmarkKey = uiState.currentBookmarkKey ?: concept.id,
                        onToggleIsolate = viewModel::toggleIsolate,
                        onClearSelection = viewModel::clearSelection,
                        onSelectSubPart = viewModel::selectPartById,
                        onToggleBookmark = viewModel::toggleBookmarkForSelection,
                        onSaveStudyNote = viewModel::saveStudyNote,
                    )
                }
            }

            AnimatedVisibility(
                visible = !showInspector,
                enter = slideInVertically { it / 2 } + fadeIn(),
                exit = slideOutVertically { it / 2 } + fadeOut(),
            ) {
                BottomControlDock(
                    uiState = uiState,
                    onExplodeChange = viewModel::setExplode,
                    onToggleExplodeShortcut = viewModel::toggleExplodeShortcut,
                    onOpenSystems = { viewModel.setActiveSheet(ActiveSheet.SYSTEMS) },
                    onOpenBookmarks = { viewModel.setActiveSheet(ActiveSheet.BOOKMARKS) },
                    onReset = viewModel::resetScene,
                )
            }
        }
    }

    when (uiState.activeSheet) {
        ActiveSheet.SYSTEMS -> SystemsBottomSheet(
            systemModes = uiState.systemModes,
            partCounts = uiState.catalog?.partCountsBySystem ?: emptyMap(),
            activePreset = uiState.activePreset,
            onApplyPreset = viewModel::applyPreset,
            onSetSystemMode = viewModel::setSystemMode,
            onDismiss = viewModel::closeSheet,
        )

        ActiveSheet.SEARCH -> SearchBottomSheet(
            query = uiState.searchQuery,
            results = uiState.searchResults,
            onQueryChange = viewModel::updateSearchQuery,
            onSelectConcept = viewModel::selectConcept,
            onDismiss = viewModel::closeSheet,
        )

        ActiveSheet.BOOKMARKS -> BookmarksBottomSheet(
            bookmarks = uiState.bookmarks,
            onOpenBookmark = viewModel::openBookmark,
            onUpdateNote = viewModel::saveStudyNote,
            onDeleteBookmark = viewModel::deleteBookmark,
            onDismiss = viewModel::closeSheet,
        )

        else -> Unit
    }
}

@Composable
private fun WideAtlasLayout(
    uiState: AtlasUiState,
    sidePanelVisible: Boolean,
    viewModel: AtlasViewModel,
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        NavigationRail(
            containerColor = SlateSurface.copy(alpha = 0.94f),
            contentColor = ClinicalIce,
            modifier = Modifier.fillMaxHeight(),
        ) {
            Spacer(Modifier.height(8.dp))
            NavigationRailItem(
                selected = uiState.activeSheet == ActiveSheet.SYSTEMS,
                onClick = { viewModel.setActiveSheet(ActiveSheet.SYSTEMS) },
                icon = { Icon(Icons.Filled.Layers, contentDescription = "Systems") },
                label = { Text("Systems") },
                colors = railItemColors(),
                modifier = Modifier.testTag("rail_systems_button"),
            )
            NavigationRailItem(
                selected = uiState.activeSheet == ActiveSheet.SEARCH,
                onClick = { viewModel.setActiveSheet(ActiveSheet.SEARCH) },
                icon = { Icon(Icons.Filled.Search, contentDescription = "Find structure") },
                label = { Text("Find") },
                colors = railItemColors(),
                modifier = Modifier.testTag("rail_search_button"),
            )
            NavigationRailItem(
                selected = uiState.activeSheet == ActiveSheet.BOOKMARKS,
                onClick = { viewModel.setActiveSheet(ActiveSheet.BOOKMARKS) },
                icon = {
                    BadgedBox(
                        badge = {
                            if (uiState.bookmarks.isNotEmpty()) {
                                Badge(containerColor = VitalAmber, contentColor = SlateSurface) {
                                    Text("${uiState.bookmarks.size}")
                                }
                            }
                        },
                    ) {
                        Icon(
                            if (uiState.bookmarks.isNotEmpty()) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Bookmarks",
                        )
                    }
                },
                label = { Text("Saved") },
                colors = railItemColors(),
                modifier = Modifier.testTag("rail_bookmarks_button"),
            )
            NavigationRailItem(
                selected = uiState.explode > 0.1f,
                onClick = viewModel::toggleExplodeShortcut,
                icon = { Icon(Icons.Filled.AutoAwesomeMotion, contentDescription = "Explode view") },
                label = { Text("Explode") },
                colors = railItemColors(),
                modifier = Modifier.testTag("rail_explode_button"),
            )
            Spacer(Modifier.weight(1f))
            NavigationRailItem(
                selected = false,
                onClick = viewModel::resetScene,
                icon = { Icon(Icons.Filled.Refresh, contentDescription = "Reset scene") },
                label = { Text("Reset") },
                colors = railItemColors(),
            )
            NavigationRailItem(
                selected = uiState.activeSheet == ActiveSheet.ABOUT,
                onClick = { viewModel.setActiveSheet(ActiveSheet.ABOUT) },
                icon = { Icon(Icons.Outlined.Info, contentDescription = "About and credits") },
                label = { Text("Credits") },
                colors = railItemColors(),
            )
            Spacer(Modifier.height(8.dp))
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(16.dp),
        ) {
            // Subtle telemetry header on wide screens
            Surface(
                color = SlateSurface.copy(alpha = 0.88f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, SlateHairline),
                modifier = Modifier.align(Alignment.TopStart),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "Human Atlas 3D",
                        style = MaterialTheme.typography.titleMedium,
                        color = ClinicalIce,
                    )
                    Text(
                        text = "${uiState.visiblePartCount} / ${uiState.catalog?.parts?.size ?: 2234} meshes",
                        style = MaterialTheme.typography.labelSmall,
                        color = SurgicalCyan,
                    )
                }
            }

            CameraQuickControls(
                currentView = uiState.cameraView,
                explode = uiState.explode,
                autoRotate = uiState.autoRotate,
                isIsolated = uiState.isIsolated,
                onSelectView = viewModel::setCameraView,
                onToggleAutoRotate = viewModel::toggleAutoRotate,
                onReset = viewModel::resetScene,
                modifier = Modifier.align(Alignment.CenterEnd),
            )

            // Explode slider pill at bottom center of wide viewport
            Surface(
                color = SlateSurface.copy(alpha = 0.90f),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, SlateHairline),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .width(360.dp),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "EXPLODE",
                        style = MaterialTheme.typography.labelSmall,
                        color = ClinicalMuted,
                    )
                    Slider(
                        value = uiState.explode,
                        onValueChange = viewModel::setExplode,
                        colors = SliderDefaults.colors(
                            thumbColor = SurgicalCyan,
                            activeTrackColor = SurgicalCyan,
                            inactiveTrackColor = SlateElevated,
                        ),
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "${(uiState.explode * 100).roundToInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = SurgicalCyan,
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = sidePanelVisible,
            enter = slideInHorizontally { it } + fadeIn(),
            exit = slideOutHorizontally { it } + fadeOut(),
        ) {
            Surface(
                color = SlateSurface.copy(alpha = 0.96f),
                border = BorderStroke(1.dp, SlateHairline),
                shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp),
                modifier = Modifier
                    .width(360.dp)
                    .fillMaxHeight()
                    .padding(vertical = 8.dp),
            ) {
                Box(modifier = Modifier.padding(top = 16.dp)) {
                    when (uiState.activeSheet) {
                        ActiveSheet.SYSTEMS -> SystemsPanelContent(
                            systemModes = uiState.systemModes,
                            partCounts = uiState.catalog?.partCountsBySystem ?: emptyMap(),
                            activePreset = uiState.activePreset,
                            onApplyPreset = viewModel::applyPreset,
                            onSetSystemMode = viewModel::setSystemMode,
                            onClose = viewModel::closeSheet,
                        )

                        ActiveSheet.SEARCH -> SearchPanelContent(
                            query = uiState.searchQuery,
                            results = uiState.searchResults,
                            onQueryChange = viewModel::updateSearchQuery,
                            onSelectConcept = viewModel::selectConcept,
                            onClose = viewModel::closeSheet,
                        )

                        ActiveSheet.BOOKMARKS -> BookmarksPanelContent(
                            bookmarks = uiState.bookmarks,
                            onOpenBookmark = viewModel::openBookmark,
                            onUpdateNote = viewModel::saveStudyNote,
                            onDeleteBookmark = viewModel::deleteBookmark,
                            onClose = viewModel::closeSheet,
                        )

                        ActiveSheet.INSPECTOR -> {
                            val concept = uiState.selectedConcept
                            if (concept != null) {
                                StructureInspectorCard(
                                    concept = concept,
                                    selectedParts = uiState.selectedParts,
                                    system = uiState.primarySelectedSystem ?: SystemId.CONNECTIVE,
                                    isIsolated = uiState.isIsolated,
                                    bookmark = uiState.currentBookmark,
                                    bookmarkKey = uiState.currentBookmarkKey ?: concept.id,
                                    onToggleIsolate = viewModel::toggleIsolate,
                                    onClearSelection = viewModel::clearSelection,
                                    onSelectSubPart = viewModel::selectPartById,
                                    onToggleBookmark = viewModel::toggleBookmarkForSelection,
                                    onSaveStudyNote = viewModel::saveStudyNote,
                                    modifier = Modifier.padding(12.dp),
                                )
                            }
                        }

                        else -> Unit
                    }
                }
            }
        }
    }
}

@Composable
private fun railItemColors() = NavigationRailItemDefaults.colors(
    selectedIconColor = SurgicalCyanDeep,
    selectedTextColor = SurgicalCyan,
    indicatorColor = SurgicalCyan,
    unselectedIconColor = ClinicalMuted,
    unselectedTextColor = ClinicalMuted,
)
