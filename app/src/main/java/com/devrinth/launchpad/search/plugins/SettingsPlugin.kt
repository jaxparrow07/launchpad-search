package com.devrinth.launchpad.search.plugins

import android.content.Context
import android.provider.Settings
import androidx.appcompat.content.res.AppCompatResources
import com.devrinth.launchpad.R
import com.devrinth.launchpad.adapters.ResultAdapter
import com.devrinth.launchpad.search.SearchPlugin
import com.devrinth.launchpad.utils.IntentUtils
import com.devrinth.launchpad.utils.StringUtils

class SettingsPlugin(mContext: Context) : SearchPlugin(mContext) {

    override var PRIORITY = 1
    override var ID = "settings"

    private val settingsMap = mapOf(
        "Wi-Fi Settings" to Settings.ACTION_WIFI_SETTINGS,
        "Bluetooth Settings" to Settings.ACTION_BLUETOOTH_SETTINGS,
        "Data Usage Settings" to Settings.ACTION_DATA_USAGE_SETTINGS,
        "Location Settings" to Settings.ACTION_LOCATION_SOURCE_SETTINGS,
        "Airplane Mode Settings" to Settings.ACTION_AIRPLANE_MODE_SETTINGS,
        "Security Settings" to Settings.ACTION_SECURITY_SETTINGS,
        "Sound Settings" to Settings.ACTION_SOUND_SETTINGS,
        "Display Settings" to Settings.ACTION_DISPLAY_SETTINGS,
        "Date and Time Settings" to Settings.ACTION_DATE_SETTINGS,
        "Language and Input Settings" to Settings.ACTION_LOCALE_SETTINGS,
        "Accessibility Settings" to Settings.ACTION_ACCESSIBILITY_SETTINGS,
        "Privacy Settings" to Settings.ACTION_PRIVACY_SETTINGS,
        "Application Settings" to Settings.ACTION_APPLICATION_SETTINGS,
        "Battery Saver Settings" to Settings.ACTION_BATTERY_SAVER_SETTINGS,
        "About Phone Settings" to Settings.ACTION_DEVICE_INFO_SETTINGS
    )

    override fun pluginProcess(query: String) {
        super.pluginProcess(query)
        val filteredSettings = arrayListOf<ResultAdapter>()

        settingsMap.forEach{ (key, value) ->
            if (StringUtils.simpleContains(query, key))
                filteredSettings.add(
                    ResultAdapter(
                        key,
                        null,
                        AppCompatResources.getDrawable(mContext, R.drawable.baseline_settings_24),
                        IntentUtils.getExternalIntent(value),
                        null )
                )
        }
        pluginResult(filteredSettings, query)

    }

}