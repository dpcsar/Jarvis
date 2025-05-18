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
            val jsonString = context.assets.open("checklist.json").bufferedReader().use { it.readText() }
            val checklist = gson.fromJson(jsonString, Checklist::class.java)
            Result.success(checklist)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}