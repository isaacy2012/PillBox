package com.innerCat.pillBox.activities

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import com.innerCat.pillBox.R
import com.innerCat.pillBox.databinding.SettingsActivityBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var g: SettingsActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        g = SettingsActivityBinding.inflate(layoutInflater)
        val view: View = g.root
        setContentView(view)
        if (savedInstanceState == null) {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.settings, SettingsFragment())
                    .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            setOnBindEditTextListener(getString(R.string.sp_warning_day_threshold))
            setOnBindEditTextListener(getString(R.string.sp_red_day_threshold))
            setOnBindEditTextListener(getString(R.string.sp_red_stock_threshold))
        }

        private fun setOnBindEditTextListener(preference: String) {
            val preferenceET: EditTextPreference? = findPreference(preference)
            preferenceET?.setOnBindEditTextListener { editText ->
                editText.inputType = InputType.TYPE_CLASS_NUMBER
            }
        }
    }


    override fun finish() {
        val intent = Intent()
        setResult(RESULT_OK, intent)
        super.finish()
    }

    /**
     * When the hardware/software back button is pressed
     */
    override fun onBackPressed() {
        finish()
    }
}