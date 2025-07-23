package com.devrinth.launchpad.search


import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout

import android.widget.TextView.OnEditorActionListener
import androidx.core.view.get
import androidx.core.widget.doOnTextChanged
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devrinth.launchpad.BuildConfig
import com.devrinth.launchpad.adapters.ResultAdapter
import com.devrinth.launchpad.adapters.ResultScrollAdapter
import com.devrinth.launchpad.adapters.SearchSuggestionListAdapter
import com.devrinth.launchpad.search.external.ExternalSearch

import com.devrinth.launchpad.search.plugins.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.core.content.edit
import androidx.core.view.isNotEmpty

class SearchManager(
    mContext: Context,
    searchTextBox: EditText,
    private var resultRecyclerView: RecyclerView,
    private var searchSuggestionsView: RecyclerView,
    searchCardLayout: LinearLayout
) {

    private var searchQuery: String = ""
    private var pluginList = arrayListOf<SearchPlugin>()
    private var pluginsMap = mapOf(

        "search-suggestions" to SearchSuggestionsPlugin(mContext),

        "apps" to LauncherPlugin(mContext),
        "contacts" to ContactsPlugin(mContext),
        "calc" to CalculatorPlugin(mContext),
        "websearch" to WebSearchPlugin(mContext),
        "units" to UnitConversionPlugin(mContext),
        "settings" to SettingsPlugin(mContext),
        "shortcuts" to ShortcutsPlugin(mContext),

    )

    private var actionSearchOpen : Boolean = true

    private var resultArray = ArrayList<ResultAdapter>()
    private var resultScrollAdapter: ResultScrollAdapter

    private var searchSuggestions = ArrayList<ResultAdapter>()
    private var searchSuggestionListAdapter: SearchSuggestionListAdapter

    private var externalSearch : ExternalSearch = ExternalSearch(mContext)

    private val sharedPreferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(mContext)

    private var enabledPlugins: MutableSet<String>? = null

    private val TAG : String = "PLUGIN MANAGER"

    private var firstQuery: Boolean = true


    init {
        searchSuggestionsView.layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false)
        resultRecyclerView.layoutManager = LinearLayoutManager(mContext)

        reloadPlugins()

        searchTextBox.doOnTextChanged { text, _, _, _ ->
            searchQuery = text.toString().trim()
            sharedPreferences.edit { putString("LAST_SEARCH_QUERY", searchQuery) }
            processQuery()
        }
        searchTextBox.setOnEditorActionListener(OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
               if ((resultRecyclerView.isNotEmpty()) && actionSearchOpen)
                   resultRecyclerView[0].performClick()
                return@OnEditorActionListener true
            }
            false
        })

        if (!enabledPlugins!!.contains("int-search")) {
            searchSuggestionsView.visibility = View.GONE
        }

        searchSuggestionListAdapter = SearchSuggestionListAdapter(searchSuggestions, mContext)
        searchSuggestionsView.adapter = searchSuggestionListAdapter

        resultScrollAdapter = ResultScrollAdapter(resultArray, mContext)
        resultRecyclerView.adapter = resultScrollAdapter

        if (!sharedPreferences.getBoolean("setting_clear_search", true)) {
            searchTextBox.setText(sharedPreferences.getString("LAST_SEARCH_QUERY", ""))
        }
        searchCardLayout.post {
            processQuery()
        }

        externalSearch.listener = object : ExternalSearch.ExternalSearchListener {
            override fun onExternalSearchResult(result: ResultAdapter, query: String) {
                appendResult(result, query)
            }
        }
    }

    fun unloadPlugins() {
        enabledPlugins = null
        externalSearch.unloadPlugins()
    }

    // Initializes all the plugin classes and loads them into memory.
    fun reloadPlugins() {

        actionSearchOpen = sharedPreferences.getBoolean("setting_top_result_default", true)

        enabledPlugins = sharedPreferences.getStringSet("setting_search_plugins", pluginsMap.keys)

        pluginList = arrayListOf()
        CoroutineScope(Dispatchers.Main).launch {
            pluginsMap.forEach { plugin ->
                val isInternalPlugin = plugin.key.contains("int-")

                if (enabledPlugins!!.contains(plugin.key) || enabledPlugins!!.isEmpty() || (isInternalPlugin)){
                    try {
                        plugin.value.pluginInit()
                    } catch (e : Exception) {
                        Log.e(plugin.key, e.localizedMessage!!)
                    } finally {
                        pluginList.add(plugin.value)
                        plugin.value.onPluginResult { resultArray, query ->
                            if (BuildConfig.DEBUG)
                                Log.d(TAG, "${plugin.key.uppercase()} returned ${resultArray.size} values")

                            if (!isInternalPlugin) {

                                if (plugin.key.contains("search-suggestions")) {
                                    resultArray.forEach { res ->
                                        searchSuggestions.add( res )
                                        searchSuggestionListAdapter.notifyItemChanged(searchSuggestions.size - 1)
                                    }
                                } else {
                                    resultArray.forEach { res ->
                                        appendResult(res, query, plugin.key)
                                    }
                                }
                            } else {
                                // TODO: Internal Plugins
                            }
                        }
                    }
                }
            }
        }
    }

    private fun appendResult(result: ResultAdapter, query : String, plugin: String? = "default") {
        if (searchQuery.equals(query, ignoreCase = true)) {
            resultArray.add(result)
            resultScrollAdapter.notifyItemChanged(resultArray.size - 1)
        }
    }

    private fun processQuery() {

        if (searchQuery.isEmpty()) {
            resultRecyclerView.visibility = View.GONE

        } else {
            resultRecyclerView.visibility = View.VISIBLE
        }
        resultArray.removeAll(resultArray.toSet())
        resultScrollAdapter.notifyDataSetChanged()

        if (firstQuery && searchQuery.isNotEmpty()) {
            firstQuery = false
        }

        if (firstQuery) {
            searchSuggestionsView.visibility = View.GONE
        } else {
            searchSuggestionsView.visibility = View.VISIBLE
        }

        searchSuggestions.removeAll(searchSuggestions.toSet())
        searchSuggestionListAdapter.notifyDataSetChanged()

        externalSearch.sendQuery(searchQuery)

        pluginList.forEach { mPlugin ->
            mPlugin.pluginProcess(searchQuery)
        }

    }
}