package com.marwari.keyboard.voice

import org.junit.Assert.assertEquals
import org.junit.Test

class DevanagariToRomanTest {
    @Test fun convertsCommonMarwariWordsToRoman() {
        assertEquals("mhaaro", DevanagariToRoman.transliterate("म्हारो"))
        assertEquals("raam", DevanagariToRoman.transliterate("राम"))
        assertEquals("paani", DevanagariToRoman.transliterate("पानी"))
    }

    @Test fun preservesSpacesAndPunctuation() {
        assertEquals("mhaaro naam", DevanagariToRoman.transliterate("म्हारो नाम"))
    }
}
