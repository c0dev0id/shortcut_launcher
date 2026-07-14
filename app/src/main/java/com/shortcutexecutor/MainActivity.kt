package com.shortcutexecutor

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ShortcutExecutor"
        private const val PREFS_NAME = "shortcut_prefs"
        private const val KEY_INTENT_URI = "shortcut_intent_uri"
        private const val REQUEST_CREATE_SHORTCUT = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val savedUri = prefs.getString(KEY_INTENT_URI, null)

        if (savedUri != null) {
            // We have a saved shortcut — fire it and exit
            Log.d(TAG, "Firing saved shortcut intent")
            try {
                val shortcutIntent = Intent.parseUri(savedUri, 0).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(shortcutIntent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to launch shortcut", e)
                Toast.makeText(this, "Shortcut failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
            finishAndRemoveTask()
        } else {
            // No shortcut configured yet — open the picker
            Log.d(TAG, "No shortcut configured, opening picker")
            openShortcutPicker()
        }
    }

    private fun openShortcutPicker() {
        val pickerIntent = Intent(Intent.ACTION_CREATE_SHORTCUT)
        try {
            @Suppress("DEPRECATION")
            startActivityForResult(pickerIntent, REQUEST_CREATE_SHORTCUT)
        } catch (e: Exception) {
            Log.e(TAG, "No apps support ACTION_CREATE_SHORTCUT", e)
            Toast.makeText(this, "No shortcut sources found", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CREATE_SHORTCUT) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                // The returned Intent contains an extra "android.intent.extra.shortcut.INTENT"
                // which is the actual Intent the shortcut should fire.
                val shortcutIntent: Intent? =
                    data.getParcelableExtra("android.intent.extra.shortcut.INTENT")

                if (shortcutIntent != null) {
                    val uri = shortcutIntent.toUri(0)
                    getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                        .edit()
                        .putString(KEY_INTENT_URI, uri)
                        .apply()

                    val name = data.getStringExtra("android.intent.extra.shortcut.NAME") ?: "shortcut"
                    Log.d(TAG, "Saved shortcut '$name': $uri")
                    Toast.makeText(this, "Shortcut saved: $name\nTap again to run it.", Toast.LENGTH_LONG).show()
                } else {
                    Log.w(TAG, "Shortcut picker returned OK but no intent extra")
                    Toast.makeText(this, "Could not read shortcut data", Toast.LENGTH_LONG).show()
                }
            } else {
                Log.d(TAG, "Shortcut picker cancelled")
                Toast.makeText(this, "No shortcut selected", Toast.LENGTH_SHORT).show()
            }
            finish()
        }
    }
}
