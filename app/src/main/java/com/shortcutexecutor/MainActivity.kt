package com.shortcutexecutor

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ShortcutExecutor"
        private const val PREFS_NAME = "shortcut_prefs"
        private const val KEY_INTENT_URI = "shortcut_intent_uri"
        private const val REQUEST_CREATE_SHORTCUT = 1
    }

    // First-launch menu. Add a row (e.g. "REST call") by adding a pair here.
    private val actionTypes: List<Pair<String, () -> Unit>> by lazy {
        listOf(
            "System shortcut" to ::openShortcutPicker,
            "App Activity" to ::showAppPicker,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val savedUri = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .getString(KEY_INTENT_URI, null)

        if (savedUri != null) {
            fireSavedIntent(savedUri)
            finishAndRemoveTask()
        } else {
            showTypeChooser()
        }
    }

    private fun fireSavedIntent(uri: String) {
        Log.d(TAG, "Firing saved intent")
        try {
            val intent = Intent.parseUri(uri, 0).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch saved intent", e)
            Toast.makeText(this, "Launch failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // --- First launch: pick what kind of action to configure ---

    private fun showTypeChooser() {
        val labels = actionTypes.map { it.first }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Configure action")
            .setItems(labels) { _, which -> actionTypes[which].second() }
            .setOnCancelListener { finish() }
            .show()
    }

    // --- Type: System shortcut (legacy ACTION_CREATE_SHORTCUT picker) ---

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

    // --- Type: App Activity (pick an app, then name an activity class) ---

    @Suppress("DEPRECATION")
    private fun showAppPicker() {
        val pm = packageManager
        val apps = pm.getInstalledApplications(0)
            .filter { it.packageName != packageName }
            .sortedBy { pm.getApplicationLabel(it).toString().lowercase() }
        val labels = apps.map { pm.getApplicationLabel(it).toString() }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Pick app")
            .setItems(labels) { _, which -> showActivityInput(apps[which].packageName) }
            .setOnCancelListener { finish() }
            .show()
    }

    private fun showActivityInput(pkg: String) {
        val input = EditText(this).apply { hint = "Activity class, e.g. .SettingsActivity" }
        AlertDialog.Builder(this)
            .setTitle(pkg)
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val entered = input.text.toString().trim()
                if (entered.isEmpty()) finish() else saveAppActivity(pkg, entered)
            }
            .setNegativeButton("Cancel") { _, _ -> finish() }
            .setOnCancelListener { finish() }
            .show()
    }

    private fun saveAppActivity(pkg: String, className: String) {
        // The app is already chosen, so resolve the class against its package:
        // ".Foo" -> pkg.Foo, "Foo" -> pkg.Foo, "a.b.Foo" -> used as-is.
        val fqcn = when {
            className.startsWith(".") -> pkg + className
            !className.contains(".") -> "$pkg.$className"
            else -> className
        }
        saveIntent(Intent().setClassName(pkg, fqcn), "$pkg/$fqcn")
    }

    // --- Shared persistence (all action types serialize to one intent URI) ---

    private fun saveIntent(intent: Intent, displayName: String) {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .edit()
            .putString(KEY_INTENT_URI, intent.toUri(0))
            .apply()
        Log.d(TAG, "Saved action '$displayName'")
        Toast.makeText(this, "Saved: $displayName\nTap again to run it.", Toast.LENGTH_LONG).show()
        finish()
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CREATE_SHORTCUT) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                val shortcutIntent: Intent? =
                    data.getParcelableExtra("android.intent.extra.shortcut.INTENT")
                if (shortcutIntent != null) {
                    val name = data.getStringExtra("android.intent.extra.shortcut.NAME") ?: "shortcut"
                    saveIntent(shortcutIntent, name)
                } else {
                    Log.w(TAG, "Shortcut picker returned OK but no intent extra")
                    Toast.makeText(this, "Could not read shortcut data", Toast.LENGTH_LONG).show()
                    finish()
                }
            } else {
                Log.d(TAG, "Shortcut picker cancelled")
                Toast.makeText(this, "No shortcut selected", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
