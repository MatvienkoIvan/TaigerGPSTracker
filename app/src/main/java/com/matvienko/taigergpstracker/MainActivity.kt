package com.matvienko.taigergpstracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.matvienko.taigergpstracker.Fragment.FragmentBlank
import com.matvienko.taigergpstracker.Fragment.SettingsFragment
import com.matvienko.taigergpstracker.Fragment.TrackFragment
import com.matvienko.taigergpstracker.databinding.ActivityMainBinding
import com.matvienko.taigergpstracker.utils.openFragment


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        onButtonsNavClicks()
        openFragment(FragmentBlank.newInstance())

    }

    private fun onButtonsNavClicks() {
        binding.Bnab.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> openFragment(FragmentBlank.newInstance())
                R.id.tracks -> openFragment(TrackFragment.newInstance())
                R.id.settings -> openFragment(SettingsFragment())
            }
            true
        }
    }
}