package com.devrinth.launchpad.search.external

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import android.util.Log
import com.devrinth.launchpad.adapters.ResultAdapter
import com.devrinth.launchpad.utils.IconUtils
import com.devrinth.launchpad.utils.IntentUtils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ExternalSearch(var mContext: Context) {

    interface ExternalSearchListener {
        fun onExternalSearchResult(result: ResultAdapter, query: String, pluginPackage: String? = null)
    }

    private val connections = mutableMapOf<String, IPluginService>()
    private val serviceConnections = mutableMapOf<String, ServiceConnection>()

    var listener: ExternalSearchListener? = null

    init {
        bindAvailablePlugins()
    }

    fun unloadPlugins() {
        for ((packageName, connection) in serviceConnections) {
            Log.d("ExternalPlugin", "Unbinding from $packageName")
            try {
                mContext.unbindService(connection)
            } catch (e: IllegalArgumentException) {
                Log.w("ExternalPlugin", "Service not bound: $packageName")
            }
        }
        serviceConnections.clear()
        connections.clear()
    }

    fun sendQuery(query: String) {
        CoroutineScope(Dispatchers.Main).launch {
            queryAllPlugins(query)
        }
    }

    fun bindAvailablePlugins() {
        val intent = Intent("com.devrinth.launchpad.PLUGIN_SERVICE")
        val pm = mContext.packageManager
        val services = pm.queryIntentServices(intent, PackageManager.GET_META_DATA)

        for (service in services) {
            val component = ComponentName(service.serviceInfo.packageName, service.serviceInfo.name)
            val pluginIntent = Intent().setComponent(component)

            val connection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName, service: IBinder) {
                    Log.d("ExternalPlugin", "Connected to ${name.packageName}")
                    val pluginService = IPluginService.Stub.asInterface(service)
                    connections[name.packageName] = pluginService
                }

                override fun onServiceDisconnected(name: ComponentName) {
                    connections.remove(name.packageName)
                }
            }

            serviceConnections[service.serviceInfo.packageName] = connection

            mContext.bindService(
                pluginIntent,
                connection,
                Context.BIND_AUTO_CREATE
            )
        }
    }

    private fun queryAllPlugins(query: String) {
        for ((pkg, plugin) in connections) {
            try {
                plugin.processQuery(query, object : IPluginCallback.Stub() {
                    override fun onPluginResponse(response: PluginResponse) {
                        CoroutineScope(Dispatchers.Main).launch {
                            listener?.onExternalSearchResult(ResultAdapter(
                                response.value,
                                "${response.extra}",
                                if (response.imageUrl == null) { mContext.packageManager.getApplicationIcon(pkg) } else { IconUtils.base64ToDrawable(mContext,
                                    response.imageUrl.toString()
                                ) },
                                if (response.action1 != "")  { IntentUtils.getLinkIntent(response.action1) } else { null },
                                null
                            ), response.query, pkg)
                        }
                    }
                })
            } catch (e: Exception) {
                Log.e("ExternalPlugin", "Error querying $pkg: ${e.localizedMessage}")
            }
        }
    }
}
