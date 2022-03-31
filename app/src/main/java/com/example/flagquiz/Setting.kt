package com.example.flagquiz

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat

class Setting : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        //TODO("Not yet implemented")
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }
}