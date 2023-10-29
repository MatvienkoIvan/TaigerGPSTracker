package com.matvienko.taigergpstracker.Fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import com.matvienko.taigergpstracker.MainApp
import com.matvienko.taigergpstracker.MainViewModel
import com.matvienko.taigergpstracker.databinding.ViewTrackBinding
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig


class ViewTrackFragment : Fragment() {
    private lateinit var binding: ViewTrackBinding
    private val model: MainViewModel by activityViewModels {
        MainViewModel.ViewModelFactory(
            (requireContext().applicationContext as MainApp).database)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        settingsOsm()
        binding = ViewTrackBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getTrack()
    }

    private fun getTrack() = with(binding) {
        model.currentTrack.observe(viewLifecycleOwner) {
//            "Distance: ${String.format("%.1f", it.distance)} m"


            val date ="Date: ${it.date}"
//            val time = "Time: ${ minutes"
            val distance = "Distance: ${ it.distance} km"
            val velocity = "Speed: ${it.velocity} km/h"

            tvData.text = date
            tvTimeSave.text = it.time
            tvAverageSpeed.text = velocity
            tvDistance.text = distance
        }
    }

    private fun settingsOsm() {
        Configuration.getInstance().load(
            activity as AppCompatActivity,
            activity?.getSharedPreferences("osm_pref", Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = BuildConfig.BUILD_TYPE // исходное APPLICATION_ID выяснить причину ошибки!!!
    }

    companion object {

        @JvmStatic
        fun newInstance() = ViewTrackFragment ()

    }
}
