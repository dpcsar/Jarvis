package site.jarviscopilot.jarvis.data

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

data class ChecklistInfo(
    val id: String,
    val name: String,
    val description: String,
    val filename: String,
    val isExample: Boolean
)

private const val USER_CHECKLISTS_DIR = "user_checklists"

class ChecklistRepository(private val context: Context) {

    private val gson = Gson()
    private val kotlinxJson = Json { ignoreUnknownKeys = true }

    // Get the directory for user-imported checklists
    private fun getUserChecklistsDir(): File {
        val dir = File(context.filesDir, USER_CHECKLISTS_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    // Load all checklists (both from assets and user-imported)
    suspend fun loadAllChecklists(): List<ChecklistInfo> = withContext(Dispatchers.IO) {
        val exampleChecklists = loadExampleChecklistInfo()
        val userChecklists = loadUserChecklistInfo()
        exampleChecklists + userChecklists
    }

    // Load example checklists from assets
    private suspend fun loadExampleChecklistInfo(): List<ChecklistInfo> = withContext(Dispatchers.IO) {
        try {
            context.assets.list("")
                ?.filter { it.endsWith(".json") }
                ?.mapNotNull { filename ->
                    try {
                        if (isExampleChecklistDeleted(filename)) {
                            null
                        } else {
                            val jsonString = context.assets.open(filename).bufferedReader().use { it.readText() }
                            val jsonObject = gson.fromJson(jsonString, JsonObject::class.java)
                            val title = jsonObject.get("title")?.asString ?: ""
                            val description = jsonObject.get("description")?.asString ?: ""
                            val name = title.ifEmpty {
                                filename.substringBefore(".json").replace("_", " ").capitalize()
                            }
                            ChecklistInfo(filename, name, description, filename, true)
                        }
                    } catch (_: Exception) {
                        null
                    }
                } ?: emptyList()
        } catch (_: IOException) {
            emptyList()
        }
    }

    // Load user-imported checklists
    private fun loadUserChecklistInfo(): List<ChecklistInfo> {
        val userDir = getUserChecklistsDir()
        return userDir.listFiles()?.filter { it.isFile && it.name.endsWith(".json") }
            ?.mapNotNull { file ->
                try {
                    val jsonString = file.readText()
                    val jsonObject = gson.fromJson(jsonString, JsonObject::class.java)
                    val title = jsonObject.get("title")?.asString ?: ""
                    val description = jsonObject.get("description")?.asString ?: ""
                    val name = title.ifEmpty {
                        file.nameWithoutExtension.replace("_", " ").capitalize()
                    }
                    ChecklistInfo(file.name, name, description, file.name, false)
                } catch (_: Exception) {
                    null
                }
            } ?: emptyList()
    }

    // Load a specific checklist by filename (from either assets or user directory)
    fun loadChecklist(fileName: String): ChecklistData? {
        return try {
            // First try to load from user directory
            val userFile = File(getUserChecklistsDir(), fileName)
            val jsonString = if (userFile.exists()) {
                userFile.readText()
            } else {
                // If not found in user directory, try loading from assets
                context.assets.open(fileName).bufferedReader().use { it.readText() }
            }
            kotlinxJson.decodeFromString<ChecklistData>(jsonString)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    // Import a checklist from a Uri (file selected by the user)
    // This copies the checklist file into the app's private storage
    suspend fun importChecklist(uri: Uri): Result<ChecklistInfo> = withContext(Dispatchers.IO) {
        try {
            // One-time read of the content from the URI
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext Result.failure(IOException("Could not open input stream"))
            val content = inputStream.bufferedReader().use { it.readText() }
            inputStream.close()

            // Validate JSON format and parse
            try {
                val jsonObject = gson.fromJson(content, JsonObject::class.java)

                // Create a unique filename in app's private storage
                val filename = "cl_user_${UUID.randomUUID().toString().take(8)}.json"
                val file = File(getUserChecklistsDir(), filename)

                // Copy the checklist content to app's private storage
                FileOutputStream(file).use { it.write(content.toByteArray()) }

                // Extract name and description from the imported checklist
                val title = jsonObject.get("title")?.asString ?: ""
                val description = jsonObject.get("description")?.asString ?: ""
                val name = title.ifEmpty {
                    uri.lastPathSegment?.substringAfterLast("/")?.substringBeforeLast(".") ?: filename
                }

                // Return info about the imported checklist that's now stored in app's private directory
                val checklistInfo = ChecklistInfo(file.name, name, description, file.name, false)
                Result.success(checklistInfo)
            } catch (_: JsonSyntaxException) {
                Result.failure(IllegalArgumentException("Invalid checklist format: The file is not a valid JSON file"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Delete a checklist (can delete both example and user checklists)
    suspend fun deleteChecklist(checklistInfo: ChecklistInfo): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Make sure the user checklists directory exists
            val userDir = getUserChecklistsDir()
            if (!userDir.exists()) {
                userDir.mkdirs()
            }

            if (checklistInfo.isExample) {
                // For example checklists, we need to create a "deleted" file to track it's been removed
                val markerFile = File(userDir, "deleted_${checklistInfo.filename}")
                markerFile.createNewFile()
                Result.success(Unit)
            } else {
                // For user checklists, delete the actual file
                val file = File(userDir, checklistInfo.filename)
                if (file.exists() && file.delete()) {
                    Result.success(Unit)
                } else {
                    Result.failure(IOException("Failed to delete checklist"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Check if a checklist from assets is marked as deleted
    private fun isExampleChecklistDeleted(filename: String): Boolean {
        val markerFile = File(getUserChecklistsDir(), "deleted_$filename")
        return markerFile.exists()
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
