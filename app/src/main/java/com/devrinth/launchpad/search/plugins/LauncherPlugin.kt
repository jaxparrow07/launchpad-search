package com.devrinth.launchpad.search.plugins

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import com.devrinth.launchpad.BuildConfig
import com.devrinth.launchpad.adapters.ResultAdapter
import com.devrinth.launchpad.search.SearchPlugin
import com.devrinth.launchpad.utils.IntentUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.text.contains

class LauncherPlugin(mContext: Context) : SearchPlugin(mContext) {

    private lateinit var appList : List<ResolveInfo>
    private lateinit var mPackageManager : PackageManager
    private var lastFilteredList: List<ResultAdapter> = emptyList()
    private var lastQuery = ""

    private var isProcessing = false
    override fun pluginInit() {
        mPackageManager = mContext.packageManager
        appList = mPackageManager.queryIntentActivities(Intent(Intent.ACTION_MAIN,null)
            .addCategory(Intent.CATEGORY_LAUNCHER),0)
        super.pluginInit()
    }

    override fun pluginProcess(query: String) {
        if (!INIT || query.isEmpty() || query.length < 2 || isProcessing) {
            pluginResult(emptyList(), "")
            return
        }
        isProcessing = false

        CoroutineScope(Dispatchers.Main).launch {
            pluginResult(filterApps(query), query)
            isProcessing = false
        }
    }

    private suspend fun filterApps(query: String): List<ResultAdapter> {
        return withContext(Dispatchers.Default) {
            val currentList = appList
            val filteredApps = arrayListOf<ResultAdapter>()

            if(query.startsWith(lastQuery) && lastFilteredList.isNotEmpty()) {
                lastFilteredList.forEach {
                    if(it.value.contains(query, ignoreCase = true) || it.extra?.contains(query, ignoreCase = true) == true) {
                        filteredApps.add(it)
                    }
                }
            } else {
                for (ri in currentList) {
                    if (ri.activityInfo.packageName != BuildConfig.APPLICATION_ID) {
                        val label = ri.loadLabel(mPackageManager).toString()
                        if (label.contains(query, ignoreCase = true) || ri.activityInfo.packageName.contains(query, ignoreCase = true)) {
                            filteredApps.add(
                                ResultAdapter(
                                    label,
                                    ri.activityInfo.packageName,
                                    ri.activityInfo.loadIcon(mPackageManager),
                                    IntentUtils.getAppIntent(mPackageManager, ri.activityInfo.packageName),
                                    null
                                )
                            )
                        }
                    }
                }
            }
            lastFilteredList = filteredApps
            lastQuery = query

            filteredApps
        }
    }
}
