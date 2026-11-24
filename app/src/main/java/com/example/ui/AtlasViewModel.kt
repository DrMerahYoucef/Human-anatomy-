package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AtlasCatalogLoader
import com.example.data.local.BookmarkedStructure
import com.example.data.local.BookmarkRepository
import com.example.data.model.AnatomicalConcept
import com.example.data.model.AnatomicalPart
import com.example.data.model.AtlasCatalog
import com.example.data.model.AtlasPreset
import com.example.data.model.CameraView
import com.example.data.model.SceneViewportInsets
import com.example.data.model.SystemId
import com.example.data.model.SystemRenderMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

enum class ActiveSheet {
    NONE,
    SYSTEMS,
    SEARCH,
    INSPECTOR,
    BOOKMARKS,
    ABOUT,
}

sealed interface CatalogState {
    data object Loading : CatalogState
    data class Ready(val catalog: AtlasCatalog) : CatalogState
    data class Error(val message: String) : CatalogState
}

data class AtlasUiState(
    val catalogState: CatalogState = CatalogState.Loading,
    val webLoadProgress: Int = 0,
    val isSceneReady: Boolean = false,
    val sceneError: String? = null,
    val explode: Float = 0f,
    val systemModes: Map<SystemId, SystemRenderMode> = defaultSystemModes(),
    val selectedPartIds: List<String> = emptyList(),
    val selectedConcept: AnatomicalConcept? = null,
    val isIsolated: Boolean = false,
    val cameraView: CameraView = CameraView.THREE_QUARTER,
    val autoRotate: Boolean = false,
    val resetCounter: Int = 0,
    val activeSheet: ActiveSheet = ActiveSheet.NONE,
    val searchQuery: String = "",
    val bookmarks: List<BookmarkedStructure> = emptyList(),
    val viewportInsets: SceneViewportInsets = SceneViewportInsets(),
    val webReloadToken: Int = 0,
) {
    val catalog: AtlasCatalog?
        get() = (catalogState as? CatalogState.Ready)?.catalog

    val selectedParts: List<AnatomicalPart>
        get() {
            val map = catalog?.partsById ?: return emptyList()
            return selectedPartIds.mapNotNull { map[it] }
        }

    val primarySelectedPart: AnatomicalPart?
        get() = selectedParts.firstOrNull()

    val primarySelectedSystem: SystemId?
        get() = primarySelectedPart?.system

    val visiblePartCount: Int
        get() {
            val cat = catalog ?: return 0
            val selectedSet = selectedPartIds.toSet()
            return cat.parts.count { part ->
                if (isIsolated) {
                    part.id in selectedSet
                } else {
                    systemModes[part.system] != SystemRenderMode.HIDDEN || part.id in selectedSet
                }
            }
        }

    val activePreset: AtlasPreset?
        get() {
            val visibleSystems = systemModes.filterValues { it != SystemRenderMode.HIDDEN }.keys
            val hasXray = systemModes.values.any { it == SystemRenderMode.XRAY }
            if (hasXray) return null
            return when (visibleSystems) {
                SystemId.entries.toSet() -> AtlasPreset.ALL
                SystemId.defaultVisible -> AtlasPreset.DEFAULT
                setOf(SystemId.SKELETAL) -> AtlasPreset.SKELETON
                ORGANS_SYSTEMS -> AtlasPreset.ORGANS
                NEUROVASCULAR_SYSTEMS -> AtlasPreset.NEUROVASCULAR
                else -> null
            }
        }

    val searchResults: List<AnatomicalConcept>
        get() {
            val cat = catalog ?: return emptyList()
            val term = searchQuery.trim().lowercase()
            if (term.isEmpty()) {
                return FEATURED_NAMES.mapNotNull { name ->
                    cat.concepts.firstOrNull { it.name.equals(name, ignoreCase = true) }
                }
            }
            return cat.concepts
                .asSequence()
                .filter {
                    it.name.lowercase().contains(term) || it.id.lowercase().contains(term)
                }
                .sortedBy { it.name.length }
                .take(80)
                .toList()
        }

    val currentBookmarkKey: String?
        get() = selectedConcept?.id?.takeIf { it.isNotBlank() } ?: primarySelectedPart?.id

    val currentBookmark: BookmarkedStructure?
        get() {
            val key = currentBookmarkKey ?: return null
            return bookmarks.firstOrNull { it.structureId == key }
        }

    fun toSceneStateJson(): String {
        val visibleArr = JSONArray()
        val xrayArr = JSONArray()
        SystemId.entries.forEach { sys ->
            when (systemModes[sys] ?: SystemRenderMode.HIDDEN) {
                SystemRenderMode.SOLID -> visibleArr.put(sys.id)
                SystemRenderMode.XRAY -> {
                    visibleArr.put(sys.id)
                    xrayArr.put(sys.id)
                }
                SystemRenderMode.HIDDEN -> Unit
            }
        }
        val selectedArr = JSONArray().apply {
            selectedPartIds.forEach { put(it) }
        }
        val insetsObj = JSONObject().apply {
            put("left", viewportInsets.left)
            put("top", viewportInsets.top)
            put("right", viewportInsets.right)
            put("bottom", viewportInsets.bottom)
        }
        return JSONObject().apply {
            put("explode", explode.toDouble())
            put("visible", visibleArr)
            put("xray", xrayArr)
            put("selected", selectedArr)
            put("isolate", isIsolated)
            put("view", cameraView.jsValue)
            put("rotate", autoRotate)
            put("reset", resetCounter)
            put("inspectorOpen", activeSheet == ActiveSheet.INSPECTOR && selectedPartIds.isNotEmpty())
            put("insets", insetsObj)
        }.toString()
    }

    companion object {
        val ORGANS_SYSTEMS = setOf(
            SystemId.CARDIAC,
            SystemId.RESPIRATORY,
            SystemId.DIGESTIVE,
            SystemId.URINARY,
            SystemId.ENDOCRINE,
            SystemId.REPRODUCTIVE,
        )

        val NEUROVASCULAR_SYSTEMS = setOf(
            SystemId.CARDIAC,
            SystemId.ARTERIAL,
            SystemId.VENOUS,
            SystemId.NERVOUS,
            SystemId.LYMPHATIC,
        )

        private val FEATURED_NAMES = listOf(
            "heart",
            "brain",
            "liver",
            "stomach",
            "spleen",
            "pancreas",
            "urinary bladder",
            "trachea",
            "diaphragm",
        )

        fun defaultSystemModes(): Map<SystemId, SystemRenderMode> =
            SystemId.entries.associateWith { sys ->
                if (sys in SystemId.defaultVisible) SystemRenderMode.SOLID else SystemRenderMode.HIDDEN
            }
    }
}

class AtlasViewModel(
    private val catalogLoader: AtlasCatalogLoader,
    private val bookmarkRepository: BookmarkRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AtlasUiState())

    val uiState: StateFlow<AtlasUiState> = combine(
        _state,
        bookmarkRepository.bookmarks,
    ) { state, bookmarks ->
        state.copy(bookmarks = bookmarks)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AtlasUiState(),
    )

    init {
        loadCatalog()
    }

    fun loadCatalog() {
        viewModelScope.launch {
            _state.update { it.copy(catalogState = CatalogState.Loading) }
            runCatching { catalogLoader.load() }
                .onSuccess { catalog ->
                    _state.update { it.copy(catalogState = CatalogState.Ready(catalog)) }
                }
                .onFailure { err ->
                    _state.update {
                        it.copy(
                            catalogState = CatalogState.Error(
                                err.message ?: "Failed to load anatomical catalog.",
                            ),
                        )
                    }
                }
        }
    }

    fun onWebProgress(percent: Int) {
        _state.update { state ->
            state.copy(
                webLoadProgress = percent.coerceIn(0, 100),
                sceneError = if (percent >= 100) null else state.sceneError,
            )
        }
    }

    fun onSceneReady() {
        _state.update { it.copy(isSceneReady = true, webLoadProgress = 100, sceneError = null) }
    }

    fun onSceneError(message: String) {
        _state.update { it.copy(sceneError = message) }
    }

    fun retryWebScene() {
        _state.update {
            it.copy(
                webLoadProgress = 0,
                isSceneReady = false,
                sceneError = null,
                webReloadToken = it.webReloadToken + 1,
            )
        }
    }

    fun onPartTappedInScene(partId: String) {
        val cat = _state.value.catalog ?: return
        val part = cat.partsById[partId] ?: return
        val concept = AnatomicalConcept(
            id = part.conceptId,
            name = part.name,
            elements = listOf(part.id),
        )
        _state.update {
            it.copy(
                selectedPartIds = listOf(part.id),
                selectedConcept = concept,
                isIsolated = false,
                autoRotate = false,
                activeSheet = ActiveSheet.INSPECTOR,
            )
        }
    }

    fun selectPartById(partId: String) {
        onPartTappedInScene(partId)
    }

    fun selectConcept(concept: AnatomicalConcept) {
        _state.update {
            it.copy(
                selectedPartIds = concept.elements,
                selectedConcept = concept,
                isIsolated = false,
                autoRotate = false,
                activeSheet = ActiveSheet.INSPECTOR,
            )
        }
    }

    fun openBookmark(bookmark: BookmarkedStructure) {
        val cat = _state.value.catalog
        val elements = bookmark.elementIds.ifEmpty {
            cat?.conceptsById?.get(bookmark.conceptId)?.elements ?: listOf(bookmark.structureId)
        }
        val concept = AnatomicalConcept(
            id = bookmark.conceptId,
            name = bookmark.name,
            elements = elements,
        )
        _state.update {
            it.copy(
                selectedPartIds = elements,
                selectedConcept = concept,
                isIsolated = true,
                explode = 0f,
                autoRotate = false,
                activeSheet = ActiveSheet.INSPECTOR,
            )
        }
    }

    fun toggleIsolate() {
        _state.update { state ->
            if (state.selectedPartIds.isEmpty()) return@update state
            val nextIsolate = !state.isIsolated
            state.copy(
                isIsolated = nextIsolate,
                explode = if (nextIsolate) 0f else state.explode,
                autoRotate = false,
            )
        }
    }

    fun clearSelection() {
        _state.update {
            it.copy(
                selectedPartIds = emptyList(),
                selectedConcept = null,
                isIsolated = false,
                activeSheet = if (it.activeSheet == ActiveSheet.INSPECTOR) ActiveSheet.NONE else it.activeSheet,
            )
        }
    }

    fun setExplode(value: Float) {
        val clamped = value.coerceIn(0f, 1f)
        _state.update { state ->
            state.copy(
                explode = clamped,
                cameraView = if (clamped > 0.8f) CameraView.FRONT else state.cameraView,
                autoRotate = if (clamped > 0.05f) false else state.autoRotate,
                isIsolated = if (clamped > 0.05f) false else state.isIsolated,
            )
        }
    }

    fun toggleExplodeShortcut() {
        val current = _state.value.explode
        setExplode(if (current > 0.1f) 0f else 1f)
    }

    fun setCameraView(view: CameraView) {
        _state.update {
            it.copy(
                cameraView = view,
                autoRotate = false,
                resetCounter = it.resetCounter + 1,
            )
        }
    }

    fun toggleAutoRotate() {
        _state.update { state ->
            if (state.explode >= 0.4f || state.isIsolated) return@update state
            state.copy(autoRotate = !state.autoRotate)
        }
    }

    fun resetScene() {
        _state.update {
            it.copy(
                explode = 0f,
                systemModes = AtlasUiState.defaultSystemModes(),
                selectedPartIds = emptyList(),
                selectedConcept = null,
                isIsolated = false,
                cameraView = CameraView.THREE_QUARTER,
                autoRotate = false,
                resetCounter = it.resetCounter + 1,
                activeSheet = if (it.activeSheet == ActiveSheet.INSPECTOR) ActiveSheet.NONE else it.activeSheet,
            )
        }
    }

    fun cycleSystemMode(systemId: SystemId) {
        _state.update { state ->
            val current = state.systemModes[systemId] ?: SystemRenderMode.HIDDEN
            val updated = state.systemModes.toMutableMap().apply {
                this[systemId] = current.next()
            }
            state.copy(
                systemModes = updated,
                isIsolated = false,
            )
        }
    }

    fun setSystemMode(systemId: SystemId, mode: SystemRenderMode) {
        _state.update { state ->
            val updated = state.systemModes.toMutableMap().apply {
                this[systemId] = mode
            }
            state.copy(
                systemModes = updated,
                isIsolated = false,
            )
        }
    }

    fun applyPreset(preset: AtlasPreset) {
        val targetSystems = when (preset) {
            AtlasPreset.DEFAULT -> SystemId.defaultVisible
            AtlasPreset.ALL -> SystemId.entries.toSet()
            AtlasPreset.SKELETON -> setOf(SystemId.SKELETAL)
            AtlasPreset.ORGANS -> AtlasUiState.ORGANS_SYSTEMS
            AtlasPreset.NEUROVASCULAR -> AtlasUiState.NEUROVASCULAR_SYSTEMS
        }
        val modes = SystemId.entries.associateWith { sys ->
            if (sys in targetSystems) SystemRenderMode.SOLID else SystemRenderMode.HIDDEN
        }
        _state.update {
            it.copy(
                systemModes = modes,
                selectedPartIds = emptyList(),
                selectedConcept = null,
                isIsolated = false,
            )
        }
    }

    fun setActiveSheet(sheet: ActiveSheet) {
        _state.update { state ->
            val next = if (state.activeSheet == sheet) ActiveSheet.NONE else sheet
            state.copy(activeSheet = next)
        }
    }

    fun closeSheet() {
        _state.update { it.copy(activeSheet = ActiveSheet.NONE) }
    }

    fun updateSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    fun updateViewportInsets(insets: SceneViewportInsets) {
        _state.update { state ->
            if (state.viewportInsets == insets) state else state.copy(viewportInsets = insets)
        }
    }

    fun toggleBookmarkForSelection(initialNote: String = "") {
        val current = uiState.value
        val key = current.currentBookmarkKey ?: return
        val existing = current.bookmarks.firstOrNull { it.structureId == key }
        viewModelScope.launch {
            if (existing != null) {
                bookmarkRepository.deleteById(existing.structureId)
            } else {
                val part = current.primarySelectedPart ?: return@launch
                val concept = current.selectedConcept
                val entity = BookmarkedStructure(
                    structureId = key,
                    conceptId = concept?.id ?: part.conceptId,
                    name = concept?.name ?: part.name,
                    systemId = part.system.id,
                    systemName = part.system.displayName,
                    elementIdsCsv = current.selectedPartIds.joinToString(","),
                    studyNote = initialNote.trim(),
                )
                bookmarkRepository.upsert(entity)
            }
        }
    }

    fun saveStudyNote(structureId: String, note: String) {
        val current = uiState.value
        val existing = current.bookmarks.firstOrNull { it.structureId == structureId }
        viewModelScope.launch {
            if (existing != null) {
                bookmarkRepository.updateNote(structureId, note.trim())
            } else {
                val part = current.primarySelectedPart ?: return@launch
                val concept = current.selectedConcept
                bookmarkRepository.upsert(
                    BookmarkedStructure(
                        structureId = structureId,
                        conceptId = concept?.id ?: part.conceptId,
                        name = concept?.name ?: part.name,
                        systemId = part.system.id,
                        systemName = part.system.displayName,
                        elementIdsCsv = current.selectedPartIds.joinToString(","),
                        studyNote = note.trim(),
                    ),
                )
            }
        }
    }

    fun deleteBookmark(structureId: String) {
        viewModelScope.launch {
            bookmarkRepository.deleteById(structureId)
        }
    }

    class Factory(
        private val catalogLoader: AtlasCatalogLoader,
        private val bookmarkRepository: BookmarkRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AtlasViewModel(catalogLoader, bookmarkRepository) as T
        }
    }
}
