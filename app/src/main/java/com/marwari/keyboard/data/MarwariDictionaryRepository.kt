package com.marwari.keyboard.data

import android.content.Context
import android.util.Log
import com.marwari.keyboard.suggest.DictionaryEntry
import org.json.JSONArray
import org.json.JSONException

/**
 * Loads the Marwari word list from assets/marwari_dictionary.json.
 *
 * To add words, edit the JSON file. No Kotlin changes are needed.
 * If the file is missing or broken, this returns an empty list, and the
 * keyboard still works as a normal keyboard.
 */
class MarwariDictionaryRepository(
    private val context: Context,
    private val assetName: String = "marwari_dictionary.json"
) {

    fun load(): List<DictionaryEntry> = try {
        val json = context.assets.open(assetName).bufferedReader().use { it.readText() }
        parse(json)
    } catch (e: Exception) {
        Log.w(TAG, "Could not load dictionary; suggestions disabled", e)
        emptyList()
    }

    /** Parses the JSON array. Bad entries are skipped, and the rest are kept. */
    fun parse(json: String): List<DictionaryEntry> {
        val array = JSONArray(json)
        return (0 until array.length()).mapNotNull { index ->
            try {
                val obj = array.getJSONObject(index)
                val word = obj.getString("word").trim().lowercase()
                if (word.isEmpty()) return@mapNotNull null
                val variants = obj.optJSONArray("variants")?.let { arr ->
                    (0 until arr.length()).map { arr.getString(it).trim().lowercase() }
                }.orEmpty()
                DictionaryEntry(
                    word = word,
                    meaning = obj.optString("meaning"),
                    variants = variants
                )
            } catch (e: JSONException) {
                Log.w(TAG, "Skipping dictionary entry $index")
                null
            }
        }
    }

    companion object {
        private const val TAG = "MarwariDictionary"
    }
}
