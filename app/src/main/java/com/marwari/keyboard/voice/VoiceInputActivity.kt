package com.marwari.keyboard.voice

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast

/** Opens the device's speech recognizer and returns romanized text to the keyboard. */
class VoiceInputActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this, "इस फोन में speech recognition उपलब्ध नहीं है।", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_MIC)
        } else {
            launchRecognizer()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_MIC && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            launchRecognizer()
        } else {
            Toast.makeText(this, "वॉइस टाइपिंग के लिए माइक्रोफोन की अनुमति दें।", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun launchRecognizer() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "मारवाड़ी में बोलें")
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }
            startActivityForResult(intent, REQUEST_SPEECH)
        } catch (_: Exception) {
            Toast.makeText(this, "वॉइस पहचान शुरू नहीं हो सकी।", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    @Deprecated("Uses activity result API for broad device compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SPEECH && resultCode == RESULT_OK) {
            val heard = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull().orEmpty()
            if (heard.isNotBlank()) {
                val roman = DevanagariToRoman.transliterate(heard)
                sendBroadcast(Intent(ACTION_VOICE_TEXT).setPackage(packageName).putExtra(EXTRA_TEXT, roman))
            }
        }
        finish()
    }

    companion object {
        @Volatile var pendingText: String? = null
        const val ACTION_VOICE_TEXT = "com.marwari.keyboard.VOICE_TEXT"
        const val EXTRA_TEXT = "voice_text"
        private const val REQUEST_MIC = 4101
        private const val REQUEST_SPEECH = 4102
    }
}
