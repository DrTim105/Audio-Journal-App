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
import android.content.Intent
import android.net.Uri
import android.widget.ImageView
import androidx.navigation.NavController
import androidx.navigation.Navigation


class SettingsFragment : PreferenceFragmentCompat() {
    private lateinit var backButtonSettings: ImageView
    private lateinit var navController: NavController


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        val themePreference = findPreference<SwitchPreferenceCompat>("theme")
        themePreference?.onPreferenceChangeListener = modeChangeListener

        val contactPreference = findPreference<Preference>("contact")
        contactPreference?.setOnPreferenceClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            val uriText =
                "mailto:" + Uri.encode("jlmapps.developer@gmail.com").toString() + "?subject=" +
                        Uri.encode("Audio Journal ").toString() + "${BuildConfig.VERSION_NAME}=" + Uri.encode("")
            val uri: Uri = Uri.parse(uriText)
            intent.data = uri
            startActivity(Intent.createChooser(intent, "send email"))
            true
        }

    }

    private val modeChangeListener =
        Preference.OnPreferenceChangeListener { preference, newValue ->
            newValue as? Boolean
            when (newValue){
                true -> updateTheme(AppCompatDelegate.MODE_NIGHT_YES)
                false -> updateTheme(AppCompatDelegate.MODE_NIGHT_NO)
            }

            true
        }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<Toolbar>(R.id.settingsToolbar)
        backButtonSettings = view.findViewById(R.id.back_settings) as ImageView
        navController = Navigation.findNavController(view)


        backButtonSettings.setOnClickListener {
            navController.run {
                popBackStack()
            }
        }
    }

    private fun updateTheme(nightMode: Int): Boolean {
        AppCompatDelegate.setDefaultNightMode(nightMode)
//        requireActivity().recreate()
        return true
    }
}