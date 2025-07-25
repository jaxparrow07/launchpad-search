package com.devrinth.launchpad.preferences

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.devrinth.launchpad.R
import com.devrinth.launchpad.adapters.PluginAdapter
import com.devrinth.launchpad.plugins.PluginManager
import com.google.android.material.materialswitch.MaterialSwitch

class PluginSwitchPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.preference.R.attr.preferenceStyle
) : Preference(context, attrs, defStyleAttr) {

    private var plugin: PluginAdapter? = null
    private var pluginManager: PluginManager? = null
    private var switch: MaterialSwitch? = null
    private var onSettingsClickListener: ((String) -> Unit)? = null

    init {
        layoutResource = R.layout.preference_plugin_switch
        isSelectable = true
    }

    fun setPlugin(plugin: PluginAdapter, pluginManager: PluginManager) {
        this.plugin = plugin
        this.pluginManager = pluginManager

        title = plugin.title
        summary = plugin.description
        key = "plugin_${plugin.id}"

        notifyChanged()
    }

    fun setOnSettingsClickListener(listener: (String) -> Unit) {
        this.onSettingsClickListener = listener
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        val plugin = this.plugin ?: return

        val iconView = holder.findViewById(R.id.plugin_icon) as ImageView
        val titleView = holder.findViewById(R.id.plugin_title) as TextView
        val descriptionView = holder.findViewById(R.id.plugin_description) as TextView
        val switchView = holder.findViewById(R.id.plugin_switch) as MaterialSwitch
        val settingsButton = holder.findViewById(R.id.plugin_settings_button)

        this.switch = switchView

        iconView.setImageDrawable(
            AppCompatResources.getDrawable(
                context,
                plugin.iconResource ?: R.drawable.baseline_extension_24
            )
        )

        titleView.text = plugin.title
        descriptionView.text = plugin.description
        switchView.isChecked = plugin.isEnabled

        switchView.setOnCheckedChangeListener { _, isChecked ->
            pluginManager?.setPluginEnabled(plugin.id, isChecked)
            this.plugin = plugin.copy(isEnabled = isChecked)
            callChangeListener(isChecked)
        }

        // Handle settings button click
        settingsButton?.setOnClickListener {
            onSettingsClickListener?.invoke(plugin.id)
        }

        // Only make the main area toggle the switch, settings button has separate action
        holder.itemView.setOnClickListener {
            switchView.isChecked = !switchView.isChecked
        }
    }

    fun updatePluginState(isEnabled: Boolean) {
        plugin = plugin?.copy(isEnabled = isEnabled)
        switch?.isChecked = isEnabled
    }
}
