package com.devrinth.launchpad.utils

import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.net.toUri

class IntentUtils {

    companion object {

        fun getExternalIntent(action: String) : Intent {
            return Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        fun getLinkIntent(link: String) : Intent {
            return Intent(Intent.ACTION_VIEW, link.toUri()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        fun getAppIntent(packageManager: PackageManager, packageName: String) : Intent? {
            return packageManager.getLaunchIntentForPackage(packageName)
        }

        fun getShortcutIntent(packageName: String, activityName: String) : Intent? {
            return Intent().setClassName(packageName, activityName).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        fun getCallIntent(number: String) : Intent {
            return Intent(Intent.ACTION_DIAL, "tel:$number".toUri()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        fun getEmailIntent(email: String) : Intent {
            return Intent(Intent.ACTION_SENDTO, "mailto:$email".toUri()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        fun getIntentFromString(action : String, uri : String) : Intent {
            return Intent( action, uri.toUri() ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

    }

}