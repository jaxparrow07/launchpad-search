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

    private var previousQuery: String = ""

    private var pluginList = arrayListOf<SearchPlugin>()
    private var pluginsMap = mapOf(

        "search_suggestions" to SearchSuggestionsPlugin(mContext),
        "apps" to AppsPlugin(mContext),
        "contacts" to ContactsPlugin(mContext),
        "calculator" to CalculatorPlugin(mContext),
        "websearch" to WebSearchPlugin(mContext),
        "units" to UnitConversionPlugin(mContext),
        "settings" to SettingsPlugin(mContext),
        "shortcuts" to ShortcutsPlugin(mContext),

    )

    private var actionSearchOpen : Boolean = true

    private var resultArray = ArrayList<ResultAdapter>()
    private var resultScrollAdapter: ResultScrollAdapter

    private var displayedResults = mutableSetOf<ResultAdapter>()

    private var searchSuggestions = ArrayList<ResultAdapter>()
    private var searchSuggestionListAdapter: SearchSuggestionListAdapter

    private var displayedSuggestions = mutableSetOf<ResultAdapter>()

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
            searchTextBox.setSelection(searchTextBox.text.length)
        }
        searchCardLayout.post {
            processQuery()
        }
        externalSearch.listener = object : ExternalSearch.ExternalSearchListener {
            override fun onExternalSearchResult(result: ResultAdapter, query: String, pluginPackage: String?) {
                addResults(listOf(result), query, pluginPackage)
            }
        }
    }

    fun unloadPlugins() {
        enabledPlugins = null
        externalSearch.unloadPlugins()
    }

    /*
    *  PLUGIN LOADING
    *
    *  -> This may look complicated, but this was built to load the plugins asynchronously, and add listeners for the loaded plugins.
    *  -> Might add a less messy way to load plugins in the future.
    *
    *  */
    private fun loadPlugin(pluginName: String, isInternalPlugin: Boolean = false) {

        val plugin = pluginsMap[pluginName]!!

        try {
            plugin.pluginInit()

        } catch (e : Exception) {
            Log.e(pluginName, e.localizedMessage!!)

        } finally {

            pluginList.add(plugin)
            plugin.onPluginResult { resultArray, query ->
                if (BuildConfig.DEBUG)
                    Log.d(TAG, "${pluginName.uppercase()} returned ${resultArray.size} values")

                if (!isInternalPlugin) {

                    if (pluginName == "search_suggestions") {
                        addSearchSuggestions(resultArray, query)
                    } else {
                        if (plugin.PRIORITY > 0) {
                            CoroutineScope(Dispatchers.Main).launch {
                                kotlinx.coroutines.delay((80 * (plugin.PRIORITY).toLong()))
                                addResults(resultArray, query, pluginName)
                            }
                        } else {
                            addResults(resultArray, query, pluginName)
                        }
                    }
                } else {
                    // TODO: Internal Plugins
                }
            }

        }
    }
    fun reloadPlugins() {
        actionSearchOpen = sharedPreferences.getBoolean("setting_top_result_default", true)
        enabledPlugins = sharedPreferences.getStringSet("setting_search_plugins", pluginsMap.keys)

        pluginList = arrayListOf()
        CoroutineScope(Dispatchers.Main).launch {
            pluginsMap.forEach { plugin ->
                val isInternalPlugin = plugin.key.contains("int-")
                if (enabledPlugins!!.contains(plugin.key) || enabledPlugins!!.isEmpty() || (isInternalPlugin)) {
                    loadPlugin(plugin.key, isInternalPlugin)
                }
            }
        }
    }

    private fun addSearchSuggestions(suggestions: List<ResultAdapter>, query: String) {
        if (!searchQuery.equals(query, ignoreCase = true)) return

        val newSuggestions = suggestions.filter { newSuggestion ->
            !displayedSuggestions.contains(newSuggestion) &&
            !searchSuggestions.any { existingSuggestion ->
                isDuplicateSuggestion(existingSuggestion, newSuggestion)
            }
        }

        if (newSuggestions.isNotEmpty()) {
            val startIndex = searchSuggestions.size
            searchSuggestions.addAll(newSuggestions)
            displayedSuggestions.addAll(newSuggestions)
            searchSuggestionListAdapter.notifyItemRangeInserted(startIndex, newSuggestions.size)
        }
    }

    private fun addResults(results: List<ResultAdapter>, query: String, plugin: String? = "default") {
        if (!searchQuery.equals(query, ignoreCase = true)) return

        val newResults = results.filter { newResult ->
            !displayedResults.contains(newResult) &&
            !resultArray.any { existingResult ->
                isDuplicateResult(existingResult, newResult)
            }
        }

        if (newResults.isNotEmpty()) {
            val startIndex = resultArray.size
            resultArray.addAll(newResults)
            displayedResults.addAll(newResults)
            resultScrollAdapter.notifyItemRangeInserted(startIndex, newResults.size)
        }
    }

    private fun isDuplicateResult(existing: ResultAdapter, new: ResultAdapter): Boolean {
        return existing.value == new.value &&
               existing.extra == new.extra &&
               existing.action1?.toString() == new.action1?.toString()
    }

    private fun isDuplicateSuggestion(existing: ResultAdapter, new: ResultAdapter): Boolean {
        return existing.value == new.value
    }

    private fun processQuery() {
        val isTypingForward = searchQuery.length > previousQuery.length &&
                             searchQuery.startsWith(previousQuery, ignoreCase = true)

        if (searchQuery.isEmpty()) {
            resultRecyclerView.visibility = View.GONE
            clearAllResults()
            clearAllSuggestions()
            previousQuery = searchQuery
            return
        } else {
            resultRecyclerView.visibility = View.VISIBLE
        }

        if (isTypingForward) {
            filterExistingResultsForward()
        } else {
            clearAllResults()
            clearAllSuggestions()
        }

        if (firstQuery && searchQuery.isNotEmpty()) {
            firstQuery = false
        }

        if (firstQuery) {
            searchSuggestionsView.visibility = View.GONE
        } else {
            searchSuggestionsView.visibility = View.VISIBLE
            if (isTypingForward) {
                filterExistingSuggestionsForward()
            }
        }

        externalSearch.sendQuery(searchQuery)

        pluginList.forEach { mPlugin ->
            mPlugin.pluginProcess(searchQuery)
        }

        previousQuery = searchQuery
    }

    private fun clearAllResults() {
        if (resultArray.isNotEmpty()) {
            val count = resultArray.size

            resultArray.clear()
            displayedResults.clear()
            resultScrollAdapter.notifyItemRangeRemoved(0, count)
        }
    }

    private fun clearAllSuggestions() {
        if (searchSuggestions.isNotEmpty()) {
            val count = searchSuggestions.size

            searchSuggestions.clear()
            displayedSuggestions.clear()
            searchSuggestionListAdapter.notifyItemRangeRemoved(0, count)
        }
    }

    private fun filterExistingResultsForward() {
        val iterator = resultArray.iterator()

        var index = 0
        while (iterator.hasNext()) {
            val result = iterator.next()

            if (!resultMatchesQuery(result, searchQuery)) {

                iterator.remove()
                displayedResults.remove(result)
                resultScrollAdapter.notifyItemRemoved(index)
            } else {
                index++
            }
        }
    }

    private fun filterExistingSuggestionsForward() {
        val iterator = searchSuggestions.iterator()
        var index = 0
        while (iterator.hasNext()) {
            val suggestion = iterator.next()

            if (!suggestionMatchesQuery(suggestion, searchQuery)) {

                iterator.remove()
                displayedSuggestions.remove(suggestion)
                searchSuggestionListAdapter.notifyItemRemoved(index)
            } else {
                index++
            }
        }
    }

    private fun resultMatchesQuery(result: ResultAdapter, query: String): Boolean {
        return result.value.contains(query, ignoreCase = true)
    }

    private fun suggestionMatchesQuery(suggestion: ResultAdapter, query: String): Boolean {
        return suggestion.value.contains(query, ignoreCase = true)
    }
}