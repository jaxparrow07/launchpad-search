package com.devrinth.launchpad.activities

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import com.devrinth.launchpad.modals.OverlayState
import com.devrinth.launchpad.search.SearchWindow

class LaunchpadOverlayActivity : Activity() {

    private lateinit var mSearchWindow: SearchWindow

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        )
        @Suppress("DEPRECATION")
        window.statusBarColor = Color.TRANSPARENT

        mSearchWindow = SearchWindow(this)
        mSearchWindow.createLaunchpadWindow(window)

        setContentView(mSearchWindow.getLaunchpadWindow())

        mSearchWindow.showWindow()
        mSearchWindow.showKeyboard()

        mSearchWindow.onWindowClose { finish() }
    }

    override fun onStart() {
        super.onStart()
        OverlayState.isOverlayActive = true
    }

    override fun onStop() {
        super.onStop()
        mSearchWindow.unload()
        OverlayState.isOverlayActive = false
    }
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        mSearchWindow.hideWindow()
    }

    override fun onResume() {
        super.onResume()
        mSearchWindow.reload()
    }
    override fun onPause() {
        super.onPause()
        finish()
    }
}
