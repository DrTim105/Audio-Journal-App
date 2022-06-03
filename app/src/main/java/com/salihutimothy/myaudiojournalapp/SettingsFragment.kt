package com.salihutimothy.myaudiojournalapp

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.preference.PreferenceFragmentCompat

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        view.findViewById<Toolbar>(R.id.toolbar)?.let {
//            // Customize toolbar
//        }

        val toolbar = view.findViewById<Toolbar>(R.id.settingsToolbar)

    }
}