package site.jarviscopilot.jarvis.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

data class ChecklistInfo(val name: String, val description: String, val filename: String)

class ChecklistRepository(private val context: Context) {

    private val gson = Gson()

    suspend fun loadChecklistInfo(): List<ChecklistInfo> = withContext(Dispatchers.IO) {
        try {
            context.assets.list("")
                ?.filter { it.startsWith("cl_") && it.endsWith(".json") }
                ?.mapNotNull { filename ->
                    try {
                        val jsonString = context.assets.open(filename).bufferedReader().use { it.readText() }
                        val jsonObject = gson.fromJson(jsonString, JsonObject::class.java)
                        val title = jsonObject.get("title")?.asString ?: ""
                        val description = jsonObject.get("description")?.asString ?: ""
                        val name = title.ifEmpty {
                            filename.substringAfter("cl_").substringBefore(".json").replace("_", " ").capitalize()
                        }
                        ChecklistInfo(name, description, filename)
                    } catch (_: Exception) {
                        null
                    }
                } ?: emptyList()
        } catch (_: IOException) {
            emptyList()
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
