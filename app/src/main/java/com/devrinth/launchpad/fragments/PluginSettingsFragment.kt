package com.devrinth.launchpad.fragments

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.devrinth.launchpad.R
import androidx.core.content.edit

class PluginSettingsFragment : PreferenceFragmentCompat() {

    private var isInitialized = false

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        if (isInitialized) return

        val preferencesResource = arguments?.getInt("preferences_resource") ?: R.xml.main_preferences
        val pluginId = arguments?.getString("plugin_id") ?: ""

        setPreferencesFromResource(preferencesResource, rootKey)

        setupPluginSpecificSettings(pluginId)
        isInitialized = true
    }

    private fun setupPluginSpecificSettings(pluginId: String) {
        when (pluginId) {
            "search_suggestions" -> setupSearchSuggestionsSettings()
            "websearch" -> setupWebSearchSettings()
            "calculator" -> setupCalculatorSettings()
            "units" -> setupUnitsSettings()
            "apps" -> setupAppsSettings()
            "contacts" -> setupContactsSettings()
            "settings" -> setupSettingsPluginSettings()
            "shortcuts" -> setupShortcutsSettings()

        }
    }

    private fun setupSearchSuggestionsSettings() {
        findPreference<androidx.preference.Preference>("setting_suggestions_clear_history")?.apply {
            setOnPreferenceClickListener {
                try {
                    val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(requireContext())
                    prefs.edit { remove("search_history") }
                    android.widget.Toast.makeText(
                        requireContext(),
                        "Search history cleared",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                } catch (e: Exception) {
                    android.widget.Toast.makeText(
                        requireContext(),
                        "Error clearing history: ${e.message}",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
                true
            }
        }
    }

    private fun setupWebSearchSettings() {
        findPreference<androidx.preference.EditTextPreference>("setting_search_plugin_custom_engine")?.apply {
            setOnPreferenceChangeListener { _, newValue ->
                try {
                    val url = newValue.toString()
                    // Basic URL validation
                    if (url.contains("%s") && (url.startsWith("http://") || url.startsWith("https://"))) {
                        true
                    } else {
                        android.widget.Toast.makeText(
                            requireContext(),
                            "Invalid URL format. Must contain %s placeholder and start with http:// or https://",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                        false
                    }
                } catch (e: Exception) {
                    android.widget.Toast.makeText(
                        requireContext(),
                        "Error validating URL: ${e.message}",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                    false
                }
            }
        }
    }

    private fun setupCalculatorSettings() {
        findPreference<androidx.preference.ListPreference>("setting_calc_precision")?.apply {
            setOnPreferenceChangeListener { _, newValue ->
                try {
                    val precision = newValue.toString().toInt()
                    precision in 0..10
                } catch (e: NumberFormatException) {
                    false
                }
            }
        }
    }

    private fun setupUnitsSettings() {
        // Setup unit conversion preferences - no special handling needed
    }

    private fun setupAppsSettings() {
        // Setup apps plugin preferences - no special handling needed
    }

    private fun setupContactsSettings() {
        // Setup contacts plugin preferences - no special handling needed
    }

    private fun setupSettingsPluginSettings() {
        // Setup settings plugin preferences - no special handling needed
    }

    private fun setupShortcutsSettings() {
        // Setup shortcuts plugin preferences - no special handling needed
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isInitialized = false
    }
}
