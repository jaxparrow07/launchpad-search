package com.devrinth.launchpad.search.plugins
import android.content.Context
import android.util.Log
import com.devrinth.launchpad.R
import com.devrinth.launchpad.adapters.ResultAdapter
import com.devrinth.launchpad.search.SearchPlugin
import com.devrinth.launchpad.utils.IntentUtils
import com.devrinth.launchpad.utils.StringUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray

class SearchSuggestionsPlugin(mContext: Context) : SearchPlugin(mContext) {

    override var ID = "search_suggestions"

    private var isProcessing = false

    private val client = OkHttpClient()
    private val GOOGLE_API = "https://suggestqueries.google.com/complete/search?client=firefox&q=%s"

    private lateinit var searchEngineQ : String

    override fun pluginInit() {
        super.pluginInit()

        // This reads the plugin settings from the "websearch" plugin
        val PARENT_PLUGIN_ID = "websearch"
        val search = (mPluginManager.getPluginSetting(PARENT_PLUGIN_ID,"engine", mContext.resources.getString(R.string.search_google_query) ) as String).toString().split("|")[0]
        searchEngineQ = if (search != "custom") {
            ( mPluginManager.getPluginSetting(PARENT_PLUGIN_ID,"engine", mContext.resources.getString(R.string.search_google_query)) as String ).split("|")[1]
        } else {
            ( mPluginManager.getPluginSetting(PARENT_PLUGIN_ID,"custom_engine", mContext.resources.getString(R.string.search_google_query)) as String ).split("|")[1]
        }

    }

    override fun pluginProcess(query: String) {
        super.pluginProcess(query)
        isProcessing = false
        CoroutineScope(Dispatchers.Main).launch {
            pluginResult( processSuggestions(query), query )
            isProcessing = false
        }

    }

    private suspend fun processSuggestions(query: String) : List<ResultAdapter> {
        return withContext(Dispatchers.Default) {
            try {
                val searchSuggestions = ArrayList<ResultAdapter>()

                val request = Request.Builder()
                    .url(GOOGLE_API.format(query.lowercase()))
                    .build()

                val response = client.newCall(request).execute()

                val mainObj = JSONArray( response.body?.string() )
                val suggestionArray = mainObj.getJSONArray(1)

                for (i in 0 until suggestionArray.length() ) {
                    if (i > getPluginSetting("max", 5) as Int - 1)
                        break
                    val suggestion = suggestionArray.getString(i)
                    searchSuggestions.add(
                        ResultAdapter(
                            suggestion,
                            null,
                            null,
                            IntentUtils.getLinkIntent( searchEngineQ.format( StringUtils.encodeUrl(query) ) ),
                            null
                        )
                    )

                }
                searchSuggestions

            } catch (e : Exception) {
                emptyList()
            }
        }
    }

}