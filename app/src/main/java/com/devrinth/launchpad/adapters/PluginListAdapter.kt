package com.devrinth.launchpad.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.devrinth.launchpad.R
import com.devrinth.launchpad.plugins.PluginManager
import com.google.android.material.materialswitch.MaterialSwitch

class PluginListAdapter(
    private var plugins: List<PluginAdapter>,
    private val pluginManager: PluginManager
) : RecyclerView.Adapter<PluginListAdapter.PluginViewHolder>() {

    class PluginViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val pluginTitle: TextView = itemView.findViewById(R.id.plugin_title)
        val pluginDescription: TextView = itemView.findViewById(R.id.plugin_description)
        val pluginSwitch: MaterialSwitch = itemView.findViewById(R.id.plugin_switch)
        val pluginIcon: ImageView = itemView.findViewById(R.id.plugin_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PluginViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.plugin_item_view, parent, false)
        return PluginViewHolder(view)
    }

    override fun onBindViewHolder(holder: PluginViewHolder, position: Int) {
        val plugin = plugins[position]

        holder.pluginTitle.text = plugin.title
        holder.pluginDescription.text = plugin.description
        holder.pluginSwitch.isChecked = plugin.isEnabled

        holder.pluginIcon.setImageDrawable(AppCompatResources.getDrawable(holder.itemView.context, plugin.iconResource ?: R.drawable.baseline_extension_24))

        holder.pluginSwitch.setOnCheckedChangeListener { _, isChecked ->
            pluginManager.setPluginEnabled(plugin.id, isChecked)
            plugins[position].isEnabled = isChecked
        }

        holder.itemView.setOnClickListener {
            holder.pluginSwitch.isChecked = !holder.pluginSwitch.isChecked
        }
    }

    override fun getItemCount(): Int = plugins.size

    fun updatePlugins(newPlugins: List<PluginAdapter>) {
        plugins = newPlugins
        notifyDataSetChanged()
    }
}
