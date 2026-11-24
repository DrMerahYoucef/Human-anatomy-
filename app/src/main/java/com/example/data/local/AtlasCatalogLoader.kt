package com.example.data.local

import android.content.Context
import android.util.JsonReader
import com.example.data.model.AnatomicalConcept
import com.example.data.model.AnatomicalPart
import com.example.data.model.AtlasCatalog
import com.example.data.model.SystemId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

class AtlasCatalogLoader(private val context: Context) {
    suspend fun load(): AtlasCatalog = withContext(Dispatchers.IO) {
        val parts = ArrayList<AnatomicalPart>(2300)
        val concepts = ArrayList<AnatomicalConcept>(3500)
        var triangles = 0

        context.assets.open("atlas/models/atlas.json").use { input ->
            JsonReader(InputStreamReader(input, Charsets.UTF_8)).use { reader ->
                reader.beginObject()
                while (reader.hasNext()) {
                    when (reader.nextName()) {
                        "triangles" -> triangles = reader.nextInt()
                        "parts" -> readParts(reader, parts)
                        "concepts" -> readConcepts(reader, concepts)
                        else -> reader.skipValue()
                    }
                }
                reader.endObject()
            }
        }

        val partsById = parts.associateBy { it.id }
        val conceptsById = concepts.associateBy { it.id }
        val counts = parts.groupingBy { it.system }.eachCount()
        AtlasCatalog(
            parts = parts,
            partsById = partsById,
            concepts = concepts,
            conceptsById = conceptsById,
            partCountsBySystem = counts,
            totalTriangles = triangles,
        )
    }

    private fun readParts(reader: JsonReader, out: MutableList<AnatomicalPart>) {
        reader.beginArray()
        while (reader.hasNext()) {
            var id = ""
            var name = ""
            var conceptId = ""
            var system = SystemId.CONNECTIVE
            reader.beginObject()
            while (reader.hasNext()) {
                when (reader.nextName()) {
                    "id" -> id = reader.nextString()
                    "name" -> name = reader.nextString()
                    "conceptId" -> conceptId = reader.nextString()
                    "system" -> system = SystemId.fromId(reader.nextString()) ?: SystemId.CONNECTIVE
                    else -> reader.skipValue()
                }
            }
            reader.endObject()
            if (id.isNotEmpty()) {
                out.add(AnatomicalPart(id = id, name = name, conceptId = conceptId, system = system))
            }
        }
        reader.endArray()
    }

    private fun readConcepts(reader: JsonReader, out: MutableList<AnatomicalConcept>) {
        reader.beginArray()
        while (reader.hasNext()) {
            var id = ""
            var name = ""
            val elements = ArrayList<String>(4)
            reader.beginObject()
            while (reader.hasNext()) {
                when (reader.nextName()) {
                    "id" -> id = reader.nextString()
                    "name" -> name = reader.nextString()
                    "elements" -> {
                        reader.beginArray()
                        while (reader.hasNext()) {
                            elements.add(reader.nextString())
                        }
                        reader.endArray()
                    }
                    else -> reader.skipValue()
                }
            }
            reader.endObject()
            if (id.isNotEmpty()) {
                out.add(AnatomicalConcept(id = id, name = name, elements = elements))
            }
        }
        reader.endArray()
    }
}
