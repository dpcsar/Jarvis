package site.jarviscopilot.jarvis.data

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import site.jarviscopilot.jarvis.model.Checklist
import java.io.IOException

class ChecklistRepository(private val context: Context) {

    private val gson = Gson()
    
    suspend fun loadChecklistFromAssets(): Result<Checklist> = withContext(Dispatchers.IO) {
        try {
            Log.d("ChecklistRepository", "Loading checklist from assets...")
            val jsonString = context.assets.open("checklist.json").bufferedReader().use { it.readText() }
            Log.d("ChecklistRepository", "JSON read successfully, length: ${jsonString.length}")
            val checklist = gson.fromJson(jsonString, Checklist::class.java)
            Log.d("ChecklistRepository", "Checklist parsed successfully: ${checklist.name}, lists: ${checklist.children.size}")
            Result.success(checklist)
        } catch (e: IOException) {
            Log.e("ChecklistRepository", "IOException while loading checklist", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e("ChecklistRepository", "Exception while loading checklist", e)
            Result.failure(e)
        }
    }
}