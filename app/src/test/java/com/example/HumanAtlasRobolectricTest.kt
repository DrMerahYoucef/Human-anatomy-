package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AtlasCatalogLoader
import com.example.data.local.AtlasDatabase
import com.example.data.local.BookmarkedStructure
import com.example.data.local.BookmarkRepository
import com.example.data.model.SystemId
import com.example.data.model.SystemRenderMode
import com.example.ui.AtlasUiState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HumanAtlasRobolectricTest {

    private lateinit var context: Context
    private lateinit var db: AtlasDatabase
    private lateinit var repository: BookmarkRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AtlasDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = BookmarkRepository(db.bookmarkDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `catalog loader parses bundled BodyParts3D manifest`() = runTest {
        val catalog = AtlasCatalogLoader(context).load()
        assertEquals(2234, catalog.parts.size)
        assertEquals(3432, catalog.concepts.size)
        assertEquals(2288268, catalog.totalTriangles)
        assertTrue(catalog.concepts.any { it.name.equals("heart", ignoreCase = true) })
    }

    @Test
    fun `bookmark repository persists and updates study notes`() = runTest {
        val item = BookmarkedStructure(
            structureId = "FMA7088",
            conceptId = "FMA7088",
            name = "heart",
            systemId = "cardiac",
            systemName = "Heart",
            elementIdsCsv = "FJ2925,FJ2933",
            studyNote = "Four-chambered muscular pump",
        )
        repository.upsert(item)
        val firstList = repository.bookmarks.first()
        assertEquals(1, firstList.size)
        assertEquals("Four-chambered muscular pump", firstList.first().studyNote)

        repository.updateNote("FMA7088", "Updated ventricular valve note")
        val updatedList = repository.bookmarks.first()
        assertEquals("Updated ventricular valve note", updatedList.first().studyNote)

        repository.deleteById("FMA7088")
        assertTrue(repository.bookmarks.first().isEmpty())
    }

    @Test
    fun `scene state serializes solid and xray systems cleanly`() {
        val modes = AtlasUiState.defaultSystemModes().toMutableMap().apply {
            this[SystemId.MUSCULAR] = SystemRenderMode.XRAY
            this[SystemId.SKELETAL] = SystemRenderMode.SOLID
        }
        val state = AtlasUiState(
            explode = 0.5f,
            systemModes = modes,
            selectedPartIds = listOf("FJ1252"),
            isIsolated = true,
        )
        val json = JSONObject(state.toSceneStateJson())
        assertEquals(0.5, json.getDouble("explode"), 0.001)
        assertTrue(json.getBoolean("isolate"))
        val xray = json.getJSONArray("xray")
        assertEquals(1, xray.length())
        assertEquals("muscular", xray.getString(0))
    }

    @Test
    fun `bundled atlas asset loader resolves both gz and bin chunk paths`() {
        val bytesFromGzPath = com.example.ui.components
            .openBundledAtlasAsset(context, "models/body-0.bin.gz")
            .use { it.readBytes() }
        val bytesFromBinPath = com.example.ui.components
            .openBundledAtlasAsset(context, "models/body-0.bin")
            .use { it.readBytes() }
        assertTrue(bytesFromGzPath.isNotEmpty())
        assertEquals(bytesFromGzPath.size, bytesFromBinPath.size)
    }
}
