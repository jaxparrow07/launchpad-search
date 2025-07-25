package com.devrinth.launchpad.fragments

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.devrinth.launchpad.R
import com.devrinth.launchpad.plugins.PluginManager
import com.devrinth.launchpad.preferences.PluginSwitchPreference

class PluginManagerPreferences : PreferenceFragmentCompat() {

    private lateinit var pluginManager: PluginManager
    private var isInitialized = false

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        if (isInitialized) return

        val context = preferenceManager.context
        val screen = preferenceManager.createPreferenceScreen(context)

        pluginManager = PluginManager(requireContext())

        val pluginsCategory = androidx.preference.PreferenceCategory(context).apply {
            key = "plugins_category"
            title = "Inbuilt"
        }
        screen.addPreference(pluginsCategory)

        val plugins = pluginManager.getPlugins()
        plugins.forEach { plugin ->
            val pluginPreference = PluginSwitchPreference(context).apply {
                setPlugin(plugin, pluginManager)
                setOnSettingsClickListener { pluginId ->
                    navigateToPluginSettings(pluginId)
                }
                setOnPreferenceChangeListener { _, _ ->
                    // TODO: PLUGIN STATE CHANGE HANDLING
                    true
                }
            }
            pluginsCategory.addPreference(pluginPreference)
        }

        preferenceScreen = screen
        isInitialized = true
    }

    private fun navigateToPluginSettings(pluginId: String) {
        val resourceId = when (pluginId) {
            "websearch" -> R.xml.plugin_websearch_preferences
            "calculator" -> R.xml.plugin_calculator_preferences
            "units" -> R.xml.plugin_units_preferences
            "apps" -> R.xml.plugin_apps_preferences
            "contacts" -> R.xml.plugin_contacts_preferences
            "settings" -> R.xml.plugin_settings_preferences
            "search_suggestions" -> R.xml.plugin_search_suggestions_preferences
            "shortcuts" -> R.xml.plugin_shortcuts_preferences
            else -> null
        }

        resourceId?.let { resId ->
            val fragment = PluginSettingsFragment().apply {
                arguments = Bundle().apply {
                    putInt("preferences_resource", resId)
                    putString("plugin_id", pluginId)
                }
            }
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    android.R.anim.slide_in_left,
                    android.R.anim.slide_out_right,
                    android.R.anim.slide_in_left,
                    android.R.anim.slide_out_right
                )
                .replace(R.id.plugin_preferences_container, fragment)
                .addToBackStack("plugin_settings_$pluginId")
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isInitialized = false
    }
}
