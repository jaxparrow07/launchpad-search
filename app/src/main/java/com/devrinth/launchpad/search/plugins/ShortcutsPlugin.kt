package com.devrinth.launchpad.search.plugins

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.os.Build
import com.devrinth.launchpad.BuildConfig
import com.devrinth.launchpad.R
import com.devrinth.launchpad.adapters.ResultAdapter
import com.devrinth.launchpad.search.SearchPlugin
import com.devrinth.launchpad.utils.IntentUtils
import com.devrinth.launchpad.utils.StringUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ShortcutsPlugin(mContext: Context) : SearchPlugin(mContext) {

    override var ID = "shortcuts"

    private lateinit var mPackageManager: PackageManager
    private lateinit var mLauncherApps: LauncherApps

    private var isProcessing = false

    override fun pluginInit() {
        mPackageManager = mContext.packageManager
        mLauncherApps = mContext.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        super.pluginInit()
    }

    override fun pluginProcess(query: String) {
        if (!INIT || query.isEmpty() || query.length < 2 || isProcessing) {
            pluginResult(emptyList(), "")
            return
        }
        isProcessing = true

        CoroutineScope(Dispatchers.Main).launch {
            pluginResult(filterShortcuts(query), query)
            isProcessing = false
        }
    }

    private suspend fun filterShortcuts(query: String): List<ResultAdapter> {
        return withContext(Dispatchers.Default) {
            val results = mutableListOf<ResultAdapter>()

            val legacyIntent = Intent(Intent.ACTION_CREATE_SHORTCUT)
            val legacyShortcuts = mPackageManager.queryIntentActivities(legacyIntent, 0)

            legacyShortcuts.forEach { ri ->
                if (ri.activityInfo.packageName != BuildConfig.APPLICATION_ID) {
                    val label = ri.loadLabel(mPackageManager).toString()
                    val appLabel = mPackageManager.getApplicationLabel(
                        mPackageManager.getApplicationInfo(ri.activityInfo.packageName, 0)
                    ).toString()

                    if ( StringUtils.anyFuzzyContains(query, arrayListOf(label, appLabel))) {
                        results.add(
                            ResultAdapter(
                                label,
                                mContext.getString(R.string.plugin_shortcuts_query).format(appLabel),
                                ri.activityInfo.loadIcon(mPackageManager),
                                IntentUtils.getShortcutIntent(ri.activityInfo.packageName, ri.activityInfo.name),
                                null
                            )
                        )
                    }
                }
            }
            results
        }
    }

}