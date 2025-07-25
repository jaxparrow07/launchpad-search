package com.devrinth.launchpad.fragments

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.devrinth.launchpad.R

class LaunchPadPreferences : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.main_preferences, rootKey)

        setupAboutPreferences()
    }

    private fun setupAboutPreferences() {
        findPreference<androidx.preference.Preference>("setting_about_app_version")?.apply {
            summary = try {
                requireContext().packageManager.getPackageInfo(requireContext().packageName, 0).versionName
            } catch (e: Exception) {
                "Unknown"
            }
        }

        findPreference<androidx.preference.Preference>("setting_about_mail")?.apply {
            summary = "support@devrinth.com"
            setOnPreferenceClickListener {
                // Handle email click
                true
            }
        }

        findPreference<androidx.preference.Preference>("setting_about_website")?.apply {
            summary = "https://devrinth.com"
            setOnPreferenceClickListener {
                // Handle website click
                true
            }
        }

        findPreference<androidx.preference.Preference>("setting_about_privacy")?.apply {
            setOnPreferenceClickListener {
                // Handle privacy policy click
                true
            }
        }

        findPreference<androidx.preference.Preference>("setting_about_bmc")?.apply {
            setOnPreferenceClickListener {
                // Handle buy me a coffee click
                true
            }
        }
    }
}
