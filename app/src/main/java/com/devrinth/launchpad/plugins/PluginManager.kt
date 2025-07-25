package com.devrinth.launchpad.plugins

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.devrinth.launchpad.R
import com.devrinth.launchpad.adapters.PluginAdapter
import androidx.core.content.edit

class PluginManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    // Enhanced plugin data with settings screen mapping
    private val availablePlugins = listOf(
        PluginAdapter(
            id = "search_suggestions",
            title = "Search Suggestions",
            description = "Shows search suggestions and history",
            iconResource = R.drawable.baseline_search_24
        ),
        PluginAdapter(
            id = "calculator",
            title = "Calculator",
            description = "Perform calculations directly in search",
            iconResource = R.drawable.baseline_calculate_24
        ),
        PluginAdapter(
            id = "websearch",
            title = "Web Search",
            description = "Search the web using your preferred search engine",
            iconResource = R.drawable.web_search_24
        ),
        PluginAdapter(
            id = "units",
            title = "Unit Conversion",
            description = "Convert between different units of measurement",
            iconResource = R.drawable.baseline_scale_24
        ),
        PluginAdapter(
            id = "apps",
            title = "Apps",
            description = "Search and launch installed applications",
            iconResource = R.drawable.baseline_launch_24
        ),
        PluginAdapter(
            id = "contacts",
            title = "Contacts",
            description = "Search through your contacts",
            iconResource = R.drawable.baseline_menu_book_24
        ),
        PluginAdapter(
            id = "settings",
            title = "Settings",
            description = "Access device settings quickly",
            iconResource = R.drawable.baseline_settings_24
        ),
        PluginAdapter(
            id = "shortcuts",
            title = "Shortcuts",
            description = "Access app shortcuts and actions",
            iconResource = R.drawable.baseline_settings_24
        )
    )

    fun getPlugins(): List<PluginAdapter> {
        val enabledPluginIds = getEnabledPluginIds()
        return availablePlugins.map { plugin ->
            plugin.copy(isEnabled = enabledPluginIds.contains(plugin.id))
        }
    }

    fun getPlugin(pluginId: String): PluginAdapter? {
        return getPlugins().find { it.id == pluginId }
    }

    fun hasSettings(pluginId: String): Boolean {
        // Check if plugin has dedicated settings screen
        return when (pluginId) {
            "websearch", "calculator", "units", "apps", "contacts", "settings", "search_suggestions" -> true
            else -> false
        }
    }

    // This logic ensures this is backwards compatible with previous versions, and doesn't need much maintenance
    // since new plugins are going to be external anyways.
    private fun getEnabledPluginIds(): Set<String> {
        return sharedPreferences.getStringSet("setting_search_plugins", emptySet()) ?: emptySet()
    }

    fun setPluginEnabled(pluginId: String, enabled: Boolean) {
        val currentEnabled = getEnabledPluginIds().toMutableSet()

        if (enabled) {
            currentEnabled.add(pluginId)
        } else {
            currentEnabled.remove(pluginId)
        }

        sharedPreferences.edit {
            putStringSet("setting_search_plugins", currentEnabled)
        }

        notifyStateChange(pluginId, enabled)
    }

    fun togglePlugin(pluginId: String): Boolean {
        val currentEnabled = getEnabledPluginIds()
        val newState = !currentEnabled.contains(pluginId)
        setPluginEnabled(pluginId, newState)
        return newState
    }

    fun getPluginSetting(pluginId: String, settingKey: String, defaultValue: Any): Any {
        val fullKey = "plugin_${pluginId}_$settingKey"
        return when (defaultValue) {
            is String -> sharedPreferences.getString(fullKey, defaultValue) ?: defaultValue
            is Boolean -> sharedPreferences.getBoolean(fullKey, defaultValue)
            is Int -> sharedPreferences.getInt(fullKey, defaultValue)
            is Float -> sharedPreferences.getFloat(fullKey, defaultValue)
            is Long -> sharedPreferences.getLong(fullKey, defaultValue)
            else -> defaultValue
        }
    }

    fun setPluginSetting(pluginId: String, settingKey: String, value: Any) {
        val fullKey = "plugin_${pluginId}_$settingKey"
        sharedPreferences.edit {
            when (value) {
                is String -> putString(fullKey, value)
                is Boolean -> putBoolean(fullKey, value)
                is Int -> putInt(fullKey, value)
                is Float -> putFloat(fullKey, value)
                is Long -> putLong(fullKey, value)
            }
        }
    }

    interface PluginStateChangeListener {
        fun onPluginStateChanged(pluginId: String, enabled: Boolean)
    }

    private var stateChangeListener: PluginStateChangeListener? = null

    fun setPluginStateChangeListener(listener: PluginStateChangeListener) {
        this.stateChangeListener = listener
    }

    private fun notifyStateChange(pluginId: String, enabled: Boolean) {
        stateChangeListener?.onPluginStateChanged(pluginId, enabled)
    }
}
