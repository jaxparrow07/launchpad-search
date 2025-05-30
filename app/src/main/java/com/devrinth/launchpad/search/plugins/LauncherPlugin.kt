package com.devrinth.launchpad.search.plugins

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import com.devrinth.launchpad.BuildConfig
import com.devrinth.launchpad.adapters.ResultAdapter
import com.devrinth.launchpad.db.AppDatabase
import com.devrinth.launchpad.db.CachedApp
import com.devrinth.launchpad.search.SearchPlugin
import com.devrinth.launchpad.utils.IconUtils
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
            var currentList = appList
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

//class LauncherPlugin(mContext: Context) : SearchPlugin(mContext) {
//
//    private lateinit var appList: List<ResolveInfo>
//    private val mPackageManager: PackageManager = mContext.packageManager
//    private val db = AppDatabase.getInstance(mContext)
//    private val dao = db.cachedAppDao()
//
//    private var isProcessing = false
//
//    override fun pluginInit() {
//        CoroutineScope(Dispatchers.IO).launch {
//            val cached = dao.getAllApps()
//            val currentPackages = mPackageManager.getInstalledPackages(0).map { it.packageName }
//
//            if (cached.isEmpty() || cached.map { it.packageName }.toSet() != currentPackages.toSet()) {
//                val resolvedApps = mPackageManager.queryIntentActivities(Intent(Intent.ACTION_MAIN, null)
//                    .addCategory(Intent.CATEGORY_LAUNCHER), 0)
//
//                val apps = resolvedApps.map { ri ->
//                    CachedApp(
//                        ri.activityInfo.packageName,
//                        ri.loadLabel(mPackageManager).toString(),
//                        IconUtils.drawableToByteArray(ri.loadIcon(mPackageManager))
//                    )
//                }
//
//                dao.clearAll()
//                dao.insertApps(apps)
//            }
//
//            appList = mPackageManager.queryIntentActivities(Intent(Intent.ACTION_MAIN, null)
//                .addCategory(Intent.CATEGORY_LAUNCHER), 0)
//
//            withContext(Dispatchers.Main) {
//                super.pluginInit()
//            }
//        }
//    }
//
//    override fun pluginProcess(query: String) {
//        if (!INIT || query.isEmpty() || isProcessing) {
//            pluginResult(emptyList(), "")
//            return
//        }
//        CoroutineScope(Dispatchers.Main).launch {
//            isProcessing = true
//            pluginResult(filterApps(query), query)
//            isProcessing = false
//        }
//    }
//
//    private suspend fun filterApps(query: String): List<ResultAdapter> {
//        return withContext(Dispatchers.IO) {
//            val result = mutableListOf<ResultAdapter>()
//            val dbQuery = "%${query}%"
//            val filtered = dao.searchApps(dbQuery)
//
//            for (app in filtered) {
//                result.add(
//                    ResultAdapter(
//                        app.appName,
//                        app.packageName,
//                        null,
//                        IntentUtils.getAppIntent(mPackageManager, app.packageName),
//                        null
//                    )
//                )
//            }
//            result
//        }
//    }
//}
