package com.devrinth.launchpad.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.StatusBarManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.devrinth.launchpad.BuildConfig
import com.devrinth.launchpad.R
import com.devrinth.launchpad.fragments.LaunchPadPreferences
import com.devrinth.launchpad.receivers.AssistantActionReceiver
import com.devrinth.launchpad.services.LaunchpadTileService
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.elevation.SurfaceColors
import java.util.concurrent.Executors

class SettingsActivity : AppCompatActivity() {

    private lateinit var settingsContainer : View
    private lateinit var homeContainer : View
    private lateinit var navigationBarView: BottomNavigationView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_layout)

        val window = this.window
        val surfaceColor = SurfaceColors.SURFACE_5.getColor(baseContext)

        window.statusBarColor = surfaceColor

        supportFragmentManager.beginTransaction()
            .replace(R.id.settings_container, LaunchPadPreferences())
            .commit()

        initViews()
        checkDefaults()

//        if (intent.getBooleanExtra("from_launchpad", false)) {
//            navigationBarView.selectedItemId = R.id.navigation_settings
//        }
    }

    private val REQUEST_CODE_CONTACTS = 700
    private fun checkAndRequestContactsPermission() {
        if (!checkContactsPermission()) {
            val permissions = arrayOf(Manifest.permission.READ_CONTACTS)
            requestPermissions(permissions, REQUEST_CODE_CONTACTS)
        } else {
            Toast.makeText(baseContext, baseContext.resources.getString(R.string.home_contacts_toast), Toast.LENGTH_SHORT).show()
        }

    }

    private fun checkContactsPermission() : Boolean {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED)
    }

    private fun checkCurrentAssistant(): Int {
        return try {
            Settings.Secure.getString(baseContext.contentResolver, "assistant")
                .let { assistantIdentifier ->
                    if (assistantIdentifier.isNotEmpty() && (ComponentName.unflattenFromString(assistantIdentifier)?.packageName == BuildConfig.APPLICATION_ID)) {
                        0
                    } else {
                        1
                    }
                }
        } catch (_: Exception) {
            2
        }
    }

    override fun onResume() {
        super.onResume()
        checkDefaults()
    }

    private fun checkDefaults() {
        if (checkContactsPermission()) {
            findViewById<View>(R.id.home_allow_contacts).visibility = View.GONE
            findViewById<View>(R.id.home_contacts_summary).visibility = View.GONE
        }

        when(checkCurrentAssistant()) {

            0 -> {
                findViewById<TextView>(R.id.home_change_assist_title).text = baseContext.resources.getString(R.string.home_action_assist_ENABLED)
                findViewById<TextView>(R.id.home_change_assist_summary).text = baseContext.resources.getString(R.string.home_assist_summary_ENABLED)
                findViewById<View>(R.id.home_navigation_instructions).visibility = View.VISIBLE
            }

            1 -> {
                findViewById<TextView>(R.id.home_change_assist_title).text = baseContext.resources.getString(R.string.home_action_assist)
                findViewById<TextView>(R.id.home_change_assist_summary).text = baseContext.resources.getString(R.string.home_assist_summary)
                findViewById<View>(R.id.home_navigation_instructions).visibility = View.GONE
            }

            2 -> {
                findViewById<View>(R.id.home_navigation_instructions).visibility = View.VISIBLE
                // lazy hack but saves time
                val displayText = """
                    |${resources.getString(R.string.home_assist_summary)}
                    |
                    |${resources.getString(R.string.home_assist_summary_ENABLED)}
                """.trimIndent()
                findViewById<TextView>(R.id.home_change_assist_summary).text = displayText


            }

        }
    }

    private fun initViews() {

        settingsContainer = findViewById(R.id.settings_container)
        homeContainer = findViewById(R.id.home_container)
        navigationBarView = findViewById(R.id.bottom_navigation)

        findViewById<View>(R.id.home_allow_contacts).setOnClickListener {
            checkAndRequestContactsPermission()
        }
        findViewById<View>(R.id.home_change_assist).setOnClickListener {
            startActivity( Intent( Settings.ACTION_VOICE_INPUT_SETTINGS ) )
        }

        findViewById<View>(R.id.home_button_qs_tile).setOnClickListener {
            if (Build.VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
                val componentName = ComponentName(this, LaunchpadTileService::class.java)

                val mStatusBarManager : StatusBarManager = getSystemService(StatusBarManager::class.java)

                mStatusBarManager.requestAddTileService(
                    componentName,
                    getString(R.string.shortcut_short_label), // Label
                    Icon.createWithResource(this, R.drawable.shortcut_icon),
                    Executors.newSingleThreadExecutor()
                ) {
                    when (it) {
                        StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ALREADY_ADDED -> {
                            settingsContainer.post {
                                Toast.makeText(this,
                                    getString(R.string.general_warning_qs_tile_exists), Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

            } else {
                Toast.makeText(this, getString(R.string.general_warning_qs_tile), Toast.LENGTH_SHORT).show()
            }
        }

        navigationBarView.setOnItemSelectedListener {item ->
            when(item.itemId) {
                R.id.navigation_settings -> {
                    settingsContainer.visibility = View.VISIBLE
                    homeContainer.visibility = View.GONE
                    true
                }
                R.id.navigation_home -> {
                    settingsContainer.visibility = View.GONE
                    homeContainer.visibility = View.VISIBLE
                    true
                }
                else -> false
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        sendBroadcast(Intent(AssistantActionReceiver.ACTION_OVERLAY_SHOW))
    }

    override fun onPause() {
        super.onPause()
        sendBroadcast(Intent(AssistantActionReceiver.ACTION_OVERLAY_SHOW))
    }

}