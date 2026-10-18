package com.marwari.keyboard

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.marwari.keyboard.data.MarwariDictionaryRepository

/** Hindi-first setup screen and offline word-by-word English meaning helper. */
class MainActivity : Activity() {
    private lateinit var statusView: TextView
    private lateinit var translationInput: EditText
    private lateinit var translationOutput: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pad = (20 * resources.displayMetrics.density).toInt()
        val ink = Color.rgb(31, 42, 36)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
            setBackgroundColor(Color.rgb(251, 248, 241))
        }
        root.addView(TextView(this).apply {
            text = "मारवाड़ी कीबोर्ड"
            textSize = 28f
            setTextColor(ink)
        })
        root.addView(TextView(this).apply {
            text = "रोमन अक्षरों में मारवाड़ी लिखें। शब्दों के सुझाव भी मिलेंगे।"
            textSize = 16f
            setTextColor(ink)
            setPadding(0, pad / 2, 0, pad / 2)
        })
        statusView = TextView(this).apply { textSize = 15f; setTextColor(ink) }
        root.addView(statusView)
        root.addView(Button(this).apply {
            text = "कीबोर्ड चालू करें"
            setOnClickListener { startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) }
        })
        root.addView(Button(this).apply {
            text = "कीबोर्ड चुनें"
            setOnClickListener {
                (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).showInputMethodPicker()
            }
        })
        root.addView(TextView(this).apply {
            text = "मारवाड़ी से English शब्दार्थ"
            textSize = 20f
            setTextColor(ink)
            setPadding(0, pad, 0, pad / 3)
        })
        translationInput = EditText(this).apply {
            hint = "जैसे: mharo naam ram hai"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            minLines = 2
        }
        root.addView(translationInput)
        root.addView(Button(this).apply {
            text = "शब्दार्थ देखें"
            setOnClickListener { showWordMeanings() }
        })
        translationOutput = TextView(this).apply {
            text = "यह ऑफलाइन शब्द-दर-शब्द मदद है, पूरा वाक्य-अनुवाद नहीं।"
            textSize = 16f
            setTextColor(ink)
            setPadding(0, pad / 2, 0, 0)
        }
        root.addView(translationOutput)
        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        refreshStatus()
    }

    private fun refreshStatus() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val enabled = imm.enabledInputMethodList.any { it.packageName == packageName }
        val currentIme = Settings.Secure.getString(
            contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD
        ).orEmpty()
        val selected = currentIme.startsWith(packageName)
        statusView.text = when {
            !enabled -> "स्थिति: कीबोर्ड अभी चालू नहीं है।"
            !selected -> "स्थिति: कीबोर्ड चालू है, लेकिन चुना नहीं गया।"
            else -> "स्थिति: मारवाड़ी कीबोर्ड चालू और चुना हुआ है।"
        }
    }

    private fun showWordMeanings() {
        val dictionary = MarwariDictionaryRepository(this).load()
            .associate { it.word.lowercase() to it.meaning }
        val words = translationInput.text.toString().trim().split(Regex("\\s+"))
            .filter { it.isNotBlank() }
        if (words.isEmpty()) {
            translationOutput.text = "पहले मारवाड़ी शब्द लिखें।"
            return
        }
        translationOutput.text = words.joinToString("  ") { raw ->
            val clean = raw.trim { !it.isLetter() }
            val meaning = dictionary[clean.lowercase()].orEmpty()
            if (meaning.isBlank()) "$raw → (अर्थ शब्दकोश में नहीं है)" else "$raw → $meaning"
        } + "\\n\\nनोट: यह शब्द-दर-शब्द मदद है; प्राकृतिक वाक्य-अनुवाद के लिए ऑनलाइन अनुवाद सेवा जोड़नी होगी।"
    }
}
