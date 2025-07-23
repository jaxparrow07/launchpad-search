package com.devrinth.launchpad.search

import android.content.Context
import com.devrinth.launchpad.adapters.ResultAdapter

open class SearchPlugin(
    var mContext: Context
) {

    open var ACTIVATION_SHORTCUT = ""
    open var INIT = false
    open var PRIORITY = 0

    private var updateUI: ((List<ResultAdapter>, String) -> Unit)? = null

    // Function to be called on input change, callback function `PluginResult` to return the results ( Array<ResultAdapter> )
    open fun pluginProcess(query: String) {
        if (!INIT || query.isEmpty())
            pluginResult(emptyList(),"")
    }
    // Initialize required classes, files etc
    open fun pluginInit() {
        INIT = true
    }
    open fun pluginUnInit() {
        INIT = false
    }

    fun pluginResult(list : List<ResultAdapter>, query : String) {
        updateUI?.invoke(list, query)
    }
    fun onPluginResult(updateFunction: (List<ResultAdapter>, String) -> Unit) {
        this.updateUI = updateFunction
    }

}