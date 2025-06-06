package com.devrinth.launchpad.search.plugins

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.content.res.AppCompatResources
import androidx.preference.PreferenceManager
import com.devrinth.launchpad.R
import com.devrinth.launchpad.adapters.ResultAdapter
import com.devrinth.launchpad.search.SearchPlugin
import com.devrinth.launchpad.utils.IntentUtils
import java.net.URLEncoder
import java.nio.charset.StandardCharsets


class UrlHandlerPlugin(mContext: Context) : SearchPlugin(mContext) {

    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext)

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
                    IntentUtils.getLinkIntent( searchEngineQ.format( URLEncoder.encode(query, StandardCharsets.UTF_8.toString()) ) ),
                    null
                )
            ),
            query
        )
    }

    override fun pluginInit() {
        super.pluginInit()

        val search = sharedPreferences.getString("setting_search_plugin_engine", mContext.resources.getString(R.string.search_google_query) ).toString().split("|")[0]

        searchEngine = if (search.equals("custom")) { mContext.resources.getString(R.string.search_engine_custom) } else {search}

        if (!search.equals("custom")) {
            searchEngineQ =
                sharedPreferences.getString("setting_search_plugin_engine", mContext.resources.getString(R.string.search_google_query) ).toString().split("|")[1]
        } else {
            searchEngineQ = sharedPreferences.getString("setting_search_plugin_custom_engine", mContext.resources.getString(R.string.search_google_query).split("|")[1] ).toString()
        }

    }


}