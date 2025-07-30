package com.devrinth.launchpad.search

import android.content.Context
import com.devrinth.launchpad.adapters.ResultAdapter
import com.devrinth.launchpad.plugins.PluginManager

/**
 * Base class for all search plugins in the Launchpad Search application.
 *
 * This abstract class provides the foundation for creating custom search plugins
 * that can process user queries and return search results. All search plugins
 * should extend this class and implement the required functionality.
 *
 * @param mContext The Android context required for plugin operations
 */
open class SearchPlugin(
    var mContext: Context
) {

    /**
     * The activation shortcut for this plugin.
     * Can be used to trigger the plugin with specific keywords or patterns.
     * Default is empty string (no shortcut).
     */
    open var ACTIVATION_SHORTCUT = ""

    /**
     * Initialization status of the plugin.
     * True when the plugin has been properly initialized, false otherwise.
     */
    open var INIT = false

    /**
     * Priority level of the plugin.
     * Higher values indicate lower priority. Used to determine the order
     * in which plugins are processed or displayed.
     *
     * 0 > 5
     */
    open var PRIORITY = 0

    /**
     * Unique identifier for this plugin.
     * Used for plugin management, settings storage, and identification.
     */
    open var ID = "default"

    /**
     * Callback function to update the UI with search results.
     * This function is called when the plugin has results to display.
     */
    private var updateUI: ((List<ResultAdapter>, String) -> Unit)? = null

    /**
     * Plugin manager instance for handling plugin operations and settings.
     * Initialized during plugin initialization.
     */
    lateinit var mPluginManager: PluginManager

    /**
     * Processes a search query and generates results.
     *
     * This is the main entry point for search functionality. Override this method
     * in derived classes to implement custom search logic.
     *
     * Implement your custom search logic after the super.pluginProcess() call to avoid empty queries being passed.
     * Ignore if you're planning to implement custom logic.
     *
     * @param query The search query string entered by the user
     */
    open fun pluginProcess(query: String) {
        if (!INIT || query.isEmpty())
            pluginResult(emptyList(), "")
    }

    /**
     * Initializes the plugin with necessary components and settings.
     *
     * This method should be called before using the plugin. It sets up the
     * plugin manager, loads settings, and marks the plugin as initialized.
     *
     * Override this method in derived classes to add custom initialization logic.
     * Always add your logic after the super.pluginInit() call to ensure the plugin manager is initialized first.
     * All plugins are instantiated in the PluginManager including disabled plugins.
     * This function is ONLY run when the plugin is enabled by the user, so do all your initialization logic here to save memory.
     *
     */
    open fun pluginInit() {
        mPluginManager = PluginManager(mContext)
        PRIORITY = mPluginManager.getPluginSetting(ID, "priority", PRIORITY) as Int
        INIT = true
    }

    /**
     * Uninitializes the plugin and cleans up resources.
     *
     * This method should be called when the plugin is no longer needed.
     * Override this method in derived classes to add custom cleanup logic.
     */
    open fun pluginUnInit() {
        INIT = false
    }

    /**
     * Retrieves a plugin-specific setting value.
     *
     * This is a convenience method that delegates to the plugin manager
     * to retrieve settings specific to this plugin.
     *
     * @param pluginSetting The name of the setting to retrieve
     * @param defaultValue The default value to return if the setting doesn't exist
     * @return The setting value, or the default value if not found
     */
    fun getPluginSetting(pluginSetting: String, defaultValue: Any): Any {
        return mPluginManager.getPluginSetting(ID, pluginSetting, defaultValue)
    }

    /**
     * Sends search results to the UI for display.
     *
     * This method invokes the registered UI update callback with the provided
     * search results and query. Call this method from pluginProcess() when
     * you have results to display.
     *
     * @param list List of search results as ResultAdapter objects
     * @param query The original search query that generated these results
     */
    fun pluginResult(list: List<ResultAdapter>, query: String) {
        updateUI?.invoke(list, query)
    }

    /**
     * Registers a callback function to handle UI updates.
     *
     * This method allows the host application to provide a callback function
     * that will be called when the plugin has search results to display.
     *
     * @param updateFunction Callback function that takes a list of results and query string
     */
    fun onPluginResult(updateFunction: (List<ResultAdapter>, String) -> Unit) {
        this.updateUI = updateFunction
    }

}