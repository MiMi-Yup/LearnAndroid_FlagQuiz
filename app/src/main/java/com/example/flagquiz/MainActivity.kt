package com.example.flagquiz

import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    companion object {
        public val CHOICES: String = "pref_numberOfChoices"
        public val REGIONS: String = "pref_regionsToInclude"
        public val getData: MainActivity = MainActivity()
    }

    public var phoneDevice: Boolean = true
    public var preferencesChange = true

    private val preferencesChangeListener: SharedPreferences.OnSharedPreferenceChangeListener =
        object : SharedPreferences.OnSharedPreferenceChangeListener {
            override fun onSharedPreferenceChanged(
                sharedPreferences: SharedPreferences,
                key: String
            ) {
                //TODO("Not yet implemented")
                preferencesChange = true

                var mainFragment: FragmentMain =
                    supportFragmentManager.findFragmentById(R.id.FragmentMain) as FragmentMain
                var emptyRegions: Boolean = false

                if (key.equals(MainActivity.CHOICES)) {
                    mainFragment.updateGuessRows(sharedPreferences)
                    mainFragment.resetQuiz()
                } else if (key.equals(MainActivity.REGIONS)) {
                    var regions = sharedPreferences?.getStringSet(MainActivity.REGIONS, null)

                    if (regions != null && regions.size > 0) {
                        mainFragment.updateRegions(sharedPreferences)
                        mainFragment.resetQuiz()
                    } else {
                        emptyRegions = true
                        val editor: SharedPreferences.Editor = sharedPreferences!!.edit()
                        regions?.add(getString(R.string.default_region))
                        editor.putStringSet(MainActivity.REGIONS, regions)
                        editor.apply()

                        Toast.makeText(
                            this@MainActivity,
                            R.string.default_region_message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                if (!emptyRegions) {
                    Toast.makeText(this@MainActivity, R.string.restarting_quiz, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val orientation: Int = resources.configuration.orientation

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            val inflater: MenuInflater = menuInflater
            inflater.inflate(R.menu.menu_main, menu)
            return true
        }

        return false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val preferencesIntent: Intent = Intent(this, SettingsActivity::class.java)
        startActivity(preferencesIntent)
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)
        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(preferencesChangeListener)

        var screenSize: Int =
            getResources().getConfiguration().screenLayout.and(Configuration.SCREENLAYOUT_SIZE_MASK)

        if (screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE || screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE) {
            phoneDevice = false
        }

        if (phoneDevice) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        }
    }

    override fun onStart() {
        super.onStart()

        if (preferencesChange) {
            var mainFragment: FragmentMain =
                supportFragmentManager.findFragmentById(R.id.FragmentMain) as FragmentMain
            mainFragment.updateGuessRows(PreferenceManager.getDefaultSharedPreferences(this))
            mainFragment.updateRegions(PreferenceManager.getDefaultSharedPreferences(this))
            mainFragment.resetQuiz()
            preferencesChange = false
        }
    }
}