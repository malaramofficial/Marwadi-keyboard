package com.marwari.keyboard.suggest

data class DictionaryEntry(
    val word: String,
    val meaning: String = "",
    val variants: List<String> = emptyList()
)

/** Roman Marwari suggestions with regional spellings and conservative typo tolerance. */
class WordSuggestionEngine(entries: List<DictionaryEntry>) {
    private val entries = entries.toList()

    fun suggest(typed: String, limit: Int = MAX_SUGGESTIONS): List<String> {
        val prefix = typed.trim().lowercase()
        if (prefix.isEmpty() || limit <= 0) return emptyList()
        return entries.asSequence()
            .filter { !it.word.contains(' ') }
            .mapNotNull { entry ->
                val canonical = entry.word.lowercase()
                val variants = entry.variants.map { it.lowercase() }
                val score = when {
                    canonical.startsWith(prefix) -> 0
                    variants.any { it.startsWith(prefix) } -> 1
                    prefix.length >= 3 && (
                        oneEditAway(prefix, canonical.take(prefix.length)) ||
                        variants.any { oneEditAway(prefix, it.take(prefix.length)) }
                    ) -> 2
                    else -> return@mapNotNull null
                }
                if (canonical == prefix) null else Triple(score, entries.indexOf(entry), entry.word)
            }
            .sortedWith(compareBy<Triple<Int, Int, String>> { it.first }.thenBy { it.second })
            .map { it.third }.distinct().take(limit).toList()
    }

    private fun oneEditAway(a: String, b: String): Boolean {
        if (kotlin.math.abs(a.length - b.length) > 1) return false
        var i = 0
        var j = 0
        var edits = 0
        while (i < a.length && j < b.length) {
            if (a[i] == b[j]) { i++; j++ }
            else {
                edits++
                if (edits > 1) return false
                when {
                    a.length > b.length -> i++
                    b.length > a.length -> j++
                    else -> { i++; j++ }
                }
            }
        }
        if (i < a.length || j < b.length) edits++
        return edits <= 1
    }

    companion object {
        const val MAX_SUGGESTIONS = 3
        fun currentWord(textBeforeCursor: CharSequence): String =
            textBeforeCursor.takeLastWhile { it.isLetter() }.toString()
        fun matchCase(suggestion: String, typed: String): String = when {
            typed.isEmpty() -> suggestion
            typed.length > 1 && typed.all { it.isUpperCase() } -> suggestion.uppercase()
            typed.first().isUpperCase() -> suggestion.replaceFirstChar { it.uppercaseChar() }
            else -> suggestion
        }
    }
}
