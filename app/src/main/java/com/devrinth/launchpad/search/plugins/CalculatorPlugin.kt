package com.devrinth.launchpad.search.plugins

import android.content.Context
import androidx.appcompat.content.res.AppCompatResources
import com.devrinth.launchpad.R
import com.devrinth.launchpad.adapters.ResultAdapter
import com.devrinth.launchpad.search.SearchPlugin
import com.notkamui.keval.Keval

class CalculatorPlugin(mContext: Context) : SearchPlugin(mContext) {

    override var ID = "calculator"

    override fun pluginProcess(query: String) {
        super.pluginProcess(query)

        try {
            pluginResult(arrayListOf(ResultAdapter(
                    Keval.eval(query).toString(),
                    query,
                    AppCompatResources.getDrawable(mContext, R.drawable.baseline_calculate_24),
                    null,
                    null
                ))
            , query)
        } catch (e: Exception) {
            pluginResult(emptyList(), "")
        }
    }

}