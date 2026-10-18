package com.marwari.keyboard.ime

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.inputmethodservice.InputMethodService
import android.text.InputType
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.TextView
import com.marwari.keyboard.data.MarwariDictionaryRepository
import com.marwari.keyboard.suggest.WordSuggestionEngine
import com.marwari.keyboard.voice.VoiceInputActivity

/**
 * The Marwari keyboard (system input method).
 *
 * All text goes to the focused app through [currentInputConnection].
 * Suggestions are shown in a strip above the keys and are applied only when tapped.
 * Nothing typed is stored or logged. Password fields get no suggestions.
 */
class MarwariInputMethodService : InputMethodService() {

    private lateinit var engine: WordSuggestionEngine
    private lateinit var suggestionBar: LinearLayout
    private lateinit var keyRows: LinearLayout

    private var shiftOn = false
    private var symbolsMode = false
    private var secureField = false
    private var voiceReceiverRegistered = false
    private val voiceReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val text = intent?.getStringExtra(VoiceInputActivity.EXTRA_TEXT).orEmpty()
            if (text.isNotBlank()) {
                val connection = currentInputConnection
                if (connection != null) {
                    connection.commitText(text, 1)
                    VoiceInputActivity.pendingText = null
                } else {
                    VoiceInputActivity.pendingText = text
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        engine = WordSuggestionEngine(MarwariDictionaryRepository(this).load())
    }

    override fun onCreateInputView(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(COLOR_BG)
            setPadding(dp(4), dp(4), dp(4), dp(6))
        }

        suggestionBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        root.addView(suggestionBar, LinearLayout.LayoutParams(MATCH, dp(40)))

        keyRows = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        root.addView(keyRows, LinearLayout.LayoutParams(MATCH, WRAP))

        renderKeys()
        return root
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        shiftOn = false
        symbolsMode = false
        secureField = isSecureField(info)
        if (::keyRows.isInitialized) renderKeys()
        refreshSuggestions()
    }

    override fun onUpdateSelection(
        oldSelStart: Int,
        oldSelEnd: Int,
        newSelStart: Int,
        newSelEnd: Int,
        candidatesStart: Int,
        candidatesEnd: Int
    ) {
        super.onUpdateSelection(
            oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd
        )
        refreshSuggestions()
    }

    // ---------- Keys ----------

    private fun renderKeys() {
        keyRows.removeAllViews()
        val rows = if (symbolsMode) KeyboardLayout.symbolRows else KeyboardLayout.letterRows
        for (row in rows) {
            val rowView = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            for (key in row) {
                val params = LinearLayout.LayoutParams(0, MATCH, key.weight).apply {
                    setMargins(dp(3), dp(3), dp(3), dp(3))
                }
                rowView.addView(makeKeyView(key), params)
            }
            keyRows.addView(rowView, LinearLayout.LayoutParams(MATCH, dp(54)))
        }
    }

    private fun makeKeyView(key: KeyboardKey): TextView {
        val isShiftActive = key.type == KeyType.SHIFT && shiftOn
        val isSpecial = key.type != KeyType.CHAR
        val fill = when {
            isShiftActive -> COLOR_ACCENT
            isSpecial -> COLOR_KEY_SPECIAL
            else -> COLOR_KEY
        }
        return TextView(this).apply {
            text = if (key.type == KeyType.CHAR && shiftOn) key.label.uppercase() else key.label
            textSize = if (isSpecial) 15f else 20f
            gravity = Gravity.CENTER
            setTextColor(if (isShiftActive) Color.WHITE else COLOR_TEXT)
            background = GradientDrawable().apply {
                cornerRadius = dp(8).toFloat()
                setColor(fill)
            }
            setOnClickListener { onKey(key) }
        }
    }

    private fun onKey(key: KeyboardKey) {
        when (key.type) {
            KeyType.CHAR -> {
                val text = if (shiftOn) key.label.uppercase() else key.label
                currentInputConnection?.commitText(text, 1)
                if (shiftOn) {
                    // Shift applies to one letter, then turns off.
                    shiftOn = false
                    renderKeys()
                }
            }
            KeyType.SHIFT -> {
                shiftOn = !shiftOn
                renderKeys()
            }
            KeyType.BACKSPACE -> handleBackspace()
            KeyType.SPACE -> currentInputConnection?.commitText(" ", 1)
            KeyType.ENTER -> handleEnter()
            KeyType.SYMBOLS -> {
                symbolsMode = !symbolsMode
                shiftOn = false
                renderKeys()
            }
        }
        refreshSuggestions()
    }

    private fun handleBackspace() {
        val ic = currentInputConnection ?: return
        val selected = ic.getSelectedText(0)
        if (!selected.isNullOrEmpty()) {
            // Replace the selection with nothing.
            ic.commitText("", 1)
        } else {
            ic.deleteSurroundingText(1, 0)
        }
    }

    private fun handleEnter() {
        val ic = currentInputConnection ?: return
        val info = currentInputEditorInfo
        val imeOptions = info?.imeOptions ?: 0
        val action = imeOptions and EditorInfo.IME_MASK_ACTION
        val noEnterAction = (imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0
        val usesEditorAction = !noEnterAction &&
            action != EditorInfo.IME_ACTION_NONE &&
            action != EditorInfo.IME_ACTION_UNSPECIFIED

        if (usesEditorAction) {
            // Search, Send, Go, Done, etc.
            ic.performEditorAction(action)
        } else {
            // Multi-line fields get a real newline.
            sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)
        }
    }

    // ---------- Suggestions ----------

    private fun refreshSuggestions() {
        if (!::suggestionBar.isInitialized) return
        if (secureField) {
            showSuggestions(emptyList())
            return
        }
        val before = currentInputConnection
            ?.getTextBeforeCursor(MAX_WORD_LOOKBACK, 0)
            ?.toString()
            .orEmpty()
        showSuggestions(engine.suggest(WordSuggestionEngine.currentWord(before)))
    }

    private fun showSuggestions(words: List<String>) {
        suggestionBar.removeAllViews()
        for (word in words) {
            val view = TextView(this).apply {
                text = word
                textSize = 16f
                gravity = Gravity.CENTER
                setTextColor(COLOR_ACCENT)
                setOnClickListener { acceptSuggestion(word) }
            }
            suggestionBar.addView(view, LinearLayout.LayoutParams(0, MATCH, 1f))
        }
    }

    /** Replaces the word being typed with the chosen suggestion, then adds a space. */
    private fun acceptSuggestion(suggestion: String) {
        val ic = currentInputConnection ?: return
        val before = ic.getTextBeforeCursor(MAX_WORD_LOOKBACK, 0)?.toString().orEmpty()
        val typed = WordSuggestionEngine.currentWord(before)
        if (typed.isEmpty()) return

        ic.deleteSurroundingText(typed.length, 0)
        ic.commitText(WordSuggestionEngine.matchCase(suggestion, typed) + " ", 1)
        refreshSuggestions()
    }

    // ---------- Helpers ----------

    /** True for password fields. The keyboard then shows no suggestions and stores nothing. */
    private fun isSecureField(info: EditorInfo?): Boolean {
        if (info == null) return false
        val variation = info.inputType and InputType.TYPE_MASK_VARIATION
        return when (info.inputType and InputType.TYPE_MASK_CLASS) {
            InputType.TYPE_CLASS_TEXT ->
                variation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                    variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ||
                    variation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD
            InputType.TYPE_CLASS_NUMBER ->
                variation == InputType.TYPE_NUMBER_VARIATION_PASSWORD
            else -> false
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    companion object {
        private const val MATCH = -1 // same value as ViewGroup.LayoutParams.MATCH_PARENT
        private const val WRAP = -2  // same value as ViewGroup.LayoutParams.WRAP_CONTENT
        private const val MAX_WORD_LOOKBACK = 48

        private val COLOR_BG = Color.parseColor("#FBF8F1")
        private val COLOR_KEY = Color.WHITE
        private val COLOR_KEY_SPECIAL = Color.parseColor("#E6EFE6")
        private val COLOR_TEXT = Color.parseColor("#1F2A24")
        private val COLOR_ACCENT = Color.parseColor("#2E7D4F")
    }
}
