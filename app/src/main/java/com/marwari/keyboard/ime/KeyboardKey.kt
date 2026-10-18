package com.marwari.keyboard.ime

/** What a key does when tapped. */
enum class KeyType { CHAR, SHIFT, BACKSPACE, SPACE, ENTER, SYMBOLS }

/**
 * One key on the keyboard.
 * @param label text shown on the key, and the text inserted for CHAR keys (lowercase for letters).
 * @param weight relative width inside its row.
 */
data class KeyboardKey(
    val label: String,
    val type: KeyType = KeyType.CHAR,
    val weight: Float = 1f
)

/** The key rows for the letter and symbol keyboards. Change layouts here, not in the service. */
object KeyboardLayout {

    private fun chars(vararg labels: String): List<KeyboardKey> = labels.map { KeyboardKey(it) }

    val letterRows: List<List<KeyboardKey>> = listOf(
        chars("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
        chars("a", "s", "d", "f", "g", "h", "j", "k", "l"),
        listOf(KeyboardKey("⇧", KeyType.SHIFT, 1.5f)) +
            chars("z", "x", "c", "v", "b", "n", "m") +
            listOf(KeyboardKey("⌫", KeyType.BACKSPACE, 1.5f)),
        listOf(
            KeyboardKey("?123", KeyType.SYMBOLS, 1.5f),
            KeyboardKey(","),
            KeyboardKey("space", KeyType.SPACE, 4f),
            KeyboardKey("."),
            KeyboardKey("↵", KeyType.ENTER, 1.5f)
        )
    )

    val symbolRows: List<List<KeyboardKey>> = listOf(
        chars("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
        chars("@", "#", "₹", "&", "-", "+", "(", ")", "/"),
        chars("!", "?", "'", "\"", ":", ";") +
            listOf(KeyboardKey("⌫", KeyType.BACKSPACE, 1.5f)),
        listOf(
            KeyboardKey("ABC", KeyType.SYMBOLS, 1.5f),
            KeyboardKey(","),
            KeyboardKey("space", KeyType.SPACE, 4f),
            KeyboardKey("."),
            KeyboardKey("↵", KeyType.ENTER, 1.5f)
        )
    )
}
