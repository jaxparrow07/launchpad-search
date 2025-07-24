package com.devrinth.launchpad.adapters

data class PluginAdapter(
    val id: String,
    val title: String,
    val description: String,
    var isEnabled: Boolean = false,
    val iconResource: Int? = null,
)