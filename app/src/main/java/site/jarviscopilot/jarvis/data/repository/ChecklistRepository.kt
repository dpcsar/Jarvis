package site.jarviscopilot.jarvis.data.repository

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import site.jarviscopilot.jarvis.data.model.ChecklistData
import site.jarviscopilot.jarvis.data.model.ChecklistInfoData
import site.jarviscopilot.jarvis.data.model.ChecklistStateData
import site.jarviscopilot.jarvis.data.source.ChecklistStateManager
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

private const val USER_CHECKLISTS_DIR = "user_checklists"

/**
 * Repository implementation for checklist operations
 */
class ChecklistRepository(private val context: Context) : IChecklistRepository {

    private val gson = Gson()
    private val kotlinxJson = Json { ignoreUnknownKeys = true }
    private val stateManager = ChecklistStateManager(context)

    // Get the directory for user-imported checklists
    private fun getUserChecklistsDir(): File {
        val dir = File(context.filesDir, USER_CHECKLISTS_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Gets all available checklists
     */
    override fun getAvailableChecklists(): List<ChecklistInfoData> {
        val exampleChecklists = loadExampleChecklistInfo()
        val userChecklists = loadUserChecklistInfo()
        return exampleChecklists + userChecklists
    }

    // Load example checklists from assets
    private fun loadExampleChecklistInfo(): List<ChecklistInfoData> {
        try {
            return context.assets.list("")
                ?.filter { it.endsWith(".json") }
                ?.mapNotNull { filename ->
                    try {
                        if (isExampleChecklistDeleted(filename)) {
                            null
                        } else {
                            val jsonString =
                                context.assets.open(filename).bufferedReader().use { it.readText() }
                            val jsonObject = gson.fromJson(jsonString, JsonObject::class.java)
                            val title = jsonObject.get("title")?.asString ?: ""
                            val description = jsonObject.get("description")?.asString ?: ""
                            val name = title.ifEmpty {
                                filename.substringBefore(".json").replace("_", " ").capitalize()
                            }
                            ChecklistInfoData(filename, name, description, filename, true)
                        }
                    } catch (_: Exception) {
                        null
                    }
                } ?: emptyList()
        } catch (_: IOException) {
            return emptyList()
        }
    }

    // Load user-imported checklists
    private fun loadUserChecklistInfo(): List<ChecklistInfoData> {
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
                    ChecklistInfoData(file.name, name, description, file.name, false)
                } catch (_: Exception) {
                    null
                }
            } ?: emptyList()
    }

    /**
     * Load a specific checklist by filename (from either assets or user directory)
     */
    override fun loadChecklist(checklistName: String): ChecklistData? {
        return try {
            // First try to load from user directory
            val userFile = File(getUserChecklistsDir(), checklistName)
            val jsonString = if (userFile.exists()) {
                userFile.readText()
            } else {
                // If not found in user directory, try loading from assets
                context.assets.open(checklistName).bufferedReader().use { it.readText() }
            }
            kotlinxJson.decodeFromString<ChecklistData>(jsonString)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Import a checklist from a Uri (file selected by the user)
     */
    override suspend fun importChecklist(uri: Uri): Result<ChecklistInfoData> =
        withContext(Dispatchers.IO) {
            try {
                // One-time read of the content from the URI
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: return@withContext Result.failure(IOException("Could not open input stream"))
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
                        uri.lastPathSegment?.substringAfterLast("/")?.substringBeforeLast(".")
                            ?: filename
                    }

                    // Return info about the imported checklist that's now stored in app's private directory
                    val checklistInfoData =
                        ChecklistInfoData(file.name, name, description, file.name, false)
                    Result.success(checklistInfoData)
                } catch (_: JsonSyntaxException) {
                    Result.failure(IllegalArgumentException("Invalid checklist format: The file is not a valid JSON file"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Delete a checklist
     */
    override fun deleteChecklist(checklistId: String): Boolean {
        try {
            // Determine if this is an example checklist or user checklist
            val isExample = !File(getUserChecklistsDir(), checklistId).exists()

            // Make sure the user checklists directory exists
            val userDir = getUserChecklistsDir()
            if (!userDir.exists()) {
                userDir.mkdirs()
            }

            if (isExample) {
                // For example checklists, we need to create a "deleted" file to track it's been removed
                val markerFile = File(userDir, "deleted_${checklistId}")
                return markerFile.createNewFile()
            } else {
                // For user checklists, delete the actual file
                val file = File(userDir, checklistId)
                return file.exists() && file.delete()
            }
        } catch (_: Exception) {
            return false
        }
    }

    /**
     * Save the state of a checklist
     */
    override fun saveChecklistState(checklistStateData: ChecklistStateData): Boolean {
        return stateManager.saveChecklistState(checklistStateData)
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
