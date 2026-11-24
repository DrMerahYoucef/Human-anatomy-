package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "bookmarked_structures")
data class BookmarkedStructure(
    @PrimaryKey val structureId: String,
    val conceptId: String,
    val name: String,
    val systemId: String,
    val systemName: String,
    val elementIdsCsv: String,
    val studyNote: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
) {
    val elementIds: List<String>
        get() = elementIdsCsv.split(',').filter { it.isNotBlank() }
}

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarked_structures ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<BookmarkedStructure>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(bookmark: BookmarkedStructure)

    @Query("DELETE FROM bookmarked_structures WHERE structureId = :structureId")
    suspend fun deleteById(structureId: String)

    @Query("UPDATE bookmarked_structures SET studyNote = :note, updatedAt = :updatedAt WHERE structureId = :structureId")
    suspend fun updateNote(structureId: String, note: String, updatedAt: Long = System.currentTimeMillis())
}

@Database(entities = [BookmarkedStructure::class], version = 1, exportSchema = false)
abstract class AtlasDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao

    companion object {
        @Volatile
        private var instance: AtlasDatabase? = null

        fun getInstance(context: Context): AtlasDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AtlasDatabase::class.java,
                    "human_atlas.db",
                ).build().also { instance = it }
            }
        }
    }
}

class BookmarkRepository(private val dao: BookmarkDao) {
    val bookmarks: Flow<List<BookmarkedStructure>> = dao.observeAll()

    suspend fun upsert(bookmark: BookmarkedStructure) = dao.upsert(bookmark)

    suspend fun deleteById(structureId: String) = dao.deleteById(structureId)

    suspend fun updateNote(structureId: String, note: String) = dao.updateNote(structureId, note)
}
