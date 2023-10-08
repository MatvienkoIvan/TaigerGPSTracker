package com.matvienko.taigergpstracker.Fragment

import android.graphics.Color
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceChangeListener
import androidx.preference.PreferenceFragmentCompat
import com.matvienko.taigergpstracker.R
import com.matvienko.taigergpstracker.utils.showToast

class SettingsFragment : PreferenceFragmentCompat() {
    private lateinit var timePref: Preference
    private lateinit var colorPref: Preference
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.main_pref, rootKey)
        init()
    }
    private fun init () {
        timePref= findPreference("update_time_key")!!
        colorPref= findPreference("update_color_key")!!
        val changeListener = onChangeListener()
        timePref.onPreferenceChangeListener = changeListener
        colorPref.onPreferenceChangeListener = changeListener
        initPrefs()
    }
    private fun onChangeListener (): OnPreferenceChangeListener{
        return Preference.OnPreferenceChangeListener {
            pref, value ->
                when(pref.key){
                    "update_time_key" -> onTimeChange(value.toString())
                    "update_color_key" -> pref.icon?.setTint(Color.parseColor(value.toString()))
                }
            true
        }
    }
    private fun onTimeChange (value: String) {
        val nameArray = resources.getStringArray(R.array.loc_time_update_name)
        val valueArray = resources.getStringArray(R.array.loc_time_update_value)
        val title =  timePref.title.toString().substringBefore(":")
//            val pos = valueArray.indexOf(value)
        timePref.title = "$title: ${nameArray[valueArray.indexOf(value)]}"
    }


    private fun initPrefs(){
        val pref = timePref.preferenceManager.sharedPreferences
        val nameArray = resources.getStringArray(R.array.loc_time_update_name)
        val valueArray = resources.getStringArray(R.array.loc_time_update_value)
        val title =  timePref.title
//            val pos = valueArray.indexOf(value)
       timePref.title = "$title: ${nameArray[valueArray.indexOf(pref?.getString("update_time_key" , "3000"))]}"

        val trackColor = pref?.getString("update_color_key", "FF6600FF")
        colorPref.icon?.setTint(Color.parseColor(trackColor))
    }
}