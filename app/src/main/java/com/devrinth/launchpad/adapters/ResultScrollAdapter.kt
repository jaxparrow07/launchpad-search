package com.devrinth.launchpad.adapters

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Build
import android.service.voice.VoiceInteractionSessionService
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.devrinth.launchpad.R
import com.devrinth.launchpad.receivers.AssistantActionReceiver

class ResultScrollAdapter(private val mResults: List<ResultAdapter>, private var mContext: Context) : RecyclerView.Adapter<ResultScrollAdapter.ViewHolder>() {

    private val sharedPreferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(mContext)

    private var closeOnClick =
        sharedPreferences.getBoolean("setting_close_on_action", true)

    private var reloadReceiver : AssistantActionReceiver = AssistantActionReceiver {
        closeOnClick =
            sharedPreferences.getBoolean("setting_close_on_action", true)
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mContext.registerReceiver(reloadReceiver, IntentFilter(AssistantActionReceiver.ACTION_OVERLAY_SHOW),
                VoiceInteractionSessionService.RECEIVER_EXPORTED
            )
        } else {
            ContextCompat.registerReceiver(
                mContext,
                reloadReceiver,
                IntentFilter(AssistantActionReceiver.ACTION_OVERLAY_SHOW),
                ContextCompat.RECEIVER_EXPORTED
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext)
            .inflate(R.layout.result_view, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mResults.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mResultAdapter : ResultAdapter = mResults[position]

        holder.resultValue.text = mResultAdapter.value
        holder.resultExtra.text = mResultAdapter.extra

        if (mResultAdapter.extra != null) {
            holder.resultExtra.text = mResultAdapter.extra
            holder.resultExtra.visibility = View.VISIBLE
        } else {
            holder.resultExtra.visibility = View.GONE
        }

        holder.resultIcon.setImageDrawable(mResultAdapter.image)

        if (mResultAdapter.action1 != null) {
            holder.parentView.setOnClickListener {
                if (closeOnClick) {
                    mContext.sendBroadcast(Intent(AssistantActionReceiver.ACTION_OVERLAY_HIDE))
                }
                mContext.startActivity( mResultAdapter.action1 )
            }

        } else {
            holder.parentView.setOnClickListener {  }
        }

        if (mResultAdapter.action2 != null) {
            holder.parentView.setOnLongClickListener {
                if (closeOnClick) {
                    mContext.sendBroadcast(Intent(AssistantActionReceiver.ACTION_OVERLAY_HIDE))
                }
                mContext.startActivity(mResultAdapter.action2)
                true
            }
        } else {
            holder.parentView.setOnLongClickListener { false  }
        }

    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val resultValue: TextView = itemView.findViewById(R.id.result_text)
        val resultExtra: TextView = itemView.findViewById(R.id.result_extra)
        val resultIcon: ImageView = itemView.findViewById(R.id.result_icon)

        val parentView: View = itemView
    }
}