package com.salihutimothy.myaudiojournalapp

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreferenceCompat

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        val preference = findPreference<SwitchPreferenceCompat>("theme")
        preference?.onPreferenceChangeListener = modeChangeListener

    }

    private val modeChangeListener =
        Preference.OnPreferenceChangeListener { preference, newValue ->
            newValue as? Boolean

            Log.d("BUG", "value $newValue")

            when (newValue){
                true -> updateTheme(AppCompatDelegate.MODE_NIGHT_YES)
                false -> updateTheme(AppCompatDelegate.MODE_NIGHT_NO)
            }

            true
        }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<Toolbar>(R.id.settingsToolbar)
    }

    private fun updateTheme(nightMode: Int): Boolean {
        AppCompatDelegate.setDefaultNightMode(nightMode)
//        requireActivity().recreate()
        return true
    }
}