package site.jarviscopilot.jarvis.data.repository

import android.content.Context
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import site.jarviscopilot.jarvis.data.model.Checklist
import java.io.IOException

class ChecklistRepository(private val context: Context) {

    private val gson = Gson()

    suspend fun loadChecklistNames(): List<String> = withContext(Dispatchers.IO) {
        try {
            context.assets.list("")
                ?.filter { it.startsWith("cl_") && it.endsWith(".json") }
                ?.map { it.substringAfter("cl_").substringBefore(".json").replace("_", " ").capitalize() }
                ?: emptyList()
        } catch (e: IOException) {
            emptyList()
        }
    }

    suspend fun loadChecklist(filename: String): Checklist? = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.assets.open(filename).bufferedReader().use { it.readText() }
            gson.fromJson(jsonString, Checklist::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // Extension function to capitalize first letter of each word
    private fun String.capitalize(): String {
        return split(" ").joinToString(" ") { word ->
            word.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase() else it.toString()
            }
        }
    }
}
