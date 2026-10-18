package com.marwari.keyboard.voice

/** Lightweight offline transliteration; this is not a translation engine. */
object DevanagariToRoman {
    private val consonants = mapOf(
        'क' to "k", 'ख' to "kh", 'ग' to "g", 'घ' to "gh", 'ङ' to "ng",
        'च' to "ch", 'छ' to "chh", 'ज' to "j", 'झ' to "jh", 'ञ' to "ny",
        'ट' to "t", 'ठ' to "th", 'ड' to "d", 'ढ' to "dh", 'ण' to "n",
        'त' to "t", 'थ' to "th", 'द' to "d", 'ध' to "dh", 'न' to "n",
        'प' to "p", 'फ' to "ph", 'ब' to "b", 'भ' to "bh", 'म' to "m",
        'य' to "y", 'र' to "r", 'ल' to "l", 'व' to "v", 'श' to "sh",
        'ष' to "sh", 'स' to "s", 'ह' to "h", 'ळ' to "l", 'क़' to "q",
        'ख़' to "kh", 'ग़' to "gh", 'ज़' to "z", 'ड़' to "r", 'ढ़' to "rh",
        'फ़' to "f"
    )
    private val vowels = mapOf(
        'अ' to "a", 'आ' to "aa", 'इ' to "i", 'ई' to "ee", 'उ' to "u",
        'ऊ' to "oo", 'ऋ' to "ri", 'ए' to "e", 'ऐ' to "ai", 'ओ' to "o",
        'औ' to "au", 'ऑ' to "o"
    )
    private val matras = mapOf(
        'ा' to "aa", 'ि' to "i", 'ी' to "ee", 'ु' to "u", 'ू' to "oo",
        'ृ' to "ri", 'े' to "e", 'ै' to "ai", 'ो' to "o", 'ौ' to "au",
        'ॉ' to "o", 'ॅ' to "e"
    )

    fun transliterate(input: String): String {
        val out = StringBuilder()
        var pendingA = false
        fun removePendingA() {
            if (pendingA && out.isNotEmpty() && out.last() == 'a') out.deleteCharAt(out.lastIndex)
            pendingA = false
        }
        for (ch in input) {
            when {
                consonants.containsKey(ch) -> {
                    out.append(consonants.getValue(ch)).append('a')
                    pendingA = true
                }
                matras.containsKey(ch) -> {
                    removePendingA()
                    out.append(matras.getValue(ch))
                }
                ch == '्' -> removePendingA()
                vowels.containsKey(ch) -> {
                    removePendingA()
                    out.append(vowels.getValue(ch))
                }
                ch == 'ं' || ch == 'ँ' -> { out.append('n'); pendingA = false }
                ch == 'ः' -> { out.append('h'); pendingA = false }
                ch == '़' -> Unit
                ch == '़' -> Unit
                ch.isWhitespace() || ch in ".,!?;:।॥-()[]" -> {
                    removePendingA()
                    out.append(ch)
                }
                ch in '०'..'९' -> { out.append(('0'.code + (ch.code - '०'.code)).toChar()); pendingA = false }
                ch.code < 128 -> { out.append(ch); pendingA = false }
                else -> { out.append(ch); pendingA = false }
            }
        }
        removePendingA()
        return out.toString()
    }
}
