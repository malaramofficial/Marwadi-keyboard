package com.marwari.keyboard.suggest

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WordSuggestionEngineTest {
    private val engine = WordSuggestionEngine(listOf(
        DictionaryEntry("mharo", "my / our", listOf("mhaaro")),
        DictionaryEntry("mhane", "to me"),
        DictionaryEntry("ram ram", "greeting"),
        DictionaryEntry("ghano", "very")
    ))
    @Test fun prefixReturnsWordsInDictionaryOrder() {
        assertEquals(listOf("mharo", "mhane"), engine.suggest("mh"))
    }
    @Test fun matchingIgnoresCase() {
        assertEquals(listOf("mharo", "mhane"), engine.suggest("MH"))
    }
    @Test fun variantReturnsCanonicalWord() {
        assertEquals(listOf("mharo"), engine.suggest("mhaa"))
    }
    @Test fun oneCharacterTypoCanStillSuggestWord() {
        assertEquals(listOf("mharo"), engine.suggest("mharx"))
    }
    @Test fun exactWordIsNotSuggestedAgain() {
        assertTrue(engine.suggest("ghano").isEmpty())
    }
    @Test fun emptyAndUnknownPrefixesReturnNothing() {
        assertTrue(engine.suggest("").isEmpty())
        assertTrue(engine.suggest("xyz").isEmpty())
    }
    @Test fun limitIsRespected() {
        assertEquals(listOf("mharo"), engine.suggest("mh", limit = 1))
    }
    @Test fun phrasesAreNotSuggestedAsSingleWordReplacement() {
        assertTrue(engine.suggest("ram").isEmpty())
    }
    @Test fun currentWordIsTrailingLettersOnly() {
        assertEquals("mh", WordSuggestionEngine.currentWord("kal mh"))
        assertEquals("", WordSuggestionEngine.currentWord("kal "))
    }
    @Test fun matchCaseFollowsTypedText() {
        assertEquals("mharo", WordSuggestionEngine.matchCase("mharo", "mh"))
        assertEquals("Mharo", WordSuggestionEngine.matchCase("mharo", "M"))
        assertEquals("MHARO", WordSuggestionEngine.matchCase("mharo", "MH"))
    }
}
