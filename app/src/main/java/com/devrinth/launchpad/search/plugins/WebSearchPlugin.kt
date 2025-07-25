package com.devrinth.launchpad.search.plugins

import android.content.Context
import androidx.appcompat.content.res.AppCompatResources
import com.devrinth.launchpad.R
import com.devrinth.launchpad.adapters.ResultAdapter
import com.devrinth.launchpad.search.SearchPlugin
import com.devrinth.launchpad.utils.IntentUtils
import com.devrinth.launchpad.utils.StringUtils

class WebSearchPlugin(mContext: Context) : SearchPlugin(mContext) {

    override var PRIORITY = 1
    override var ID = "websearch"

    private lateinit var searchEngine : String
    private lateinit var searchEngineQ : String

    override fun pluginProcess(query: String) {
        super.pluginProcess(query)
        pluginResult(
            arrayListOf(
                ResultAdapter(
                    mContext.resources.getString(R.string.plugin_search_result).format(searchEngine, query),
                    null,
                    AppCompatResources.getDrawable(mContext, R.drawable.web_search_24),
                    IntentUtils.getLinkIntent( searchEngineQ.format( StringUtils.encodeUrl(query) ) ),
                    null
                )
            ),
            query
        )
    }

    override fun pluginInit() {
        super.pluginInit()
        val search = (getPluginSetting("engine", mContext.resources.getString(R.string.search_google_query) ) as String).toString().split("|")[0]

        searchEngine = if (search == "custom") { mContext.resources.getString(R.string.search_engine_custom) } else { search }

        searchEngineQ = if (search != "custom") {
            ( getPluginSetting("engine", mContext.resources.getString(R.string.search_google_query)) as String ).split("|")[1]
        } else {
            ( getPluginSetting("custom_engine", mContext.resources.getString(R.string.search_google_query)) as String ).split("|")[1]
        }
    }


}