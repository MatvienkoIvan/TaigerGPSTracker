package com.matvienko.taigergpstracker.Fragment

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.fragment.app.activityViewModels
import androidx.preference.PreferenceManager
import com.matvienko.taigergpstracker.MainApp
import com.matvienko.taigergpstracker.MainViewModel
import com.matvienko.taigergpstracker.R
import com.matvienko.taigergpstracker.databinding.ViewTrackBinding
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline


class ViewTrackFragment : Fragment() {
    private var startPoint: GeoPoint? = null
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
        binding.fCentr.setOnClickListener {
           if(startPoint != null) binding.map.controller.animateTo(startPoint)
        }
    }

    private fun getTrack() = with(binding) {
        model.currentTrack.observe(viewLifecycleOwner) {

            val date ="Date: ${it.date}"
            val distance = "Distance: ${ it.distance} km"
            val velocity = "Speed: ${it.velocity} km/h"

            tvData.text = date
            tvTimeSave.text = it.time
            tvAverageSpeed.text = velocity
            tvDistance.text = distance
            val polyline = getPolyline (it.geoPoints)
            map.overlays.add(polyline)
            setMarkers(polyline.actualPoints)
            goToStartPosition(polyline.actualPoints[0])
            startPoint= polyline.actualPoints[0]
        }
    }

    private fun goToStartPosition(startPosition : GeoPoint) {
        binding.map.controller.zoomTo(15.0)
        binding.map.controller.animateTo(startPosition )

    }

    private fun setMarkers (list: List<GeoPoint>) = with (binding) {
        val startMarker =  Marker(map)
        val finishMarker =  Marker(map)
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        finishMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        startMarker.icon = getDrawable(requireContext(), R.drawable.start_pos)
        finishMarker.icon = getDrawable(requireContext(), R.drawable.end_pos)
        startMarker.position = list[0]
        finishMarker.position = list[list.size - 1]
        map.overlays.add(startMarker)
        map.overlays.add(finishMarker)
    }

    private fun getPolyline(geoPoints: String): Polyline {
        val polyline = Polyline()
        polyline.outlinePaint.color = Color.parseColor(
            PreferenceManager.getDefaultSharedPreferences(requireContext())
                .getString("update_color_key", "FF6600FF")
        )
        val list = geoPoints.split("/")
        list.forEach {
            if (it.isEmpty()) return@forEach
            val points = it.split(",")
            polyline.addPoint(GeoPoint(points[0].toDouble(), points[1].toDouble()))
        }
        return polyline
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
