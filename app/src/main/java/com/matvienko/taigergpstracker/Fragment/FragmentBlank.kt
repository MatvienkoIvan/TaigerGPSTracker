package com.matvienko.taigergpstracker.Fragment

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.matvienko.taigergpstracker.DB.TrackItem
import com.matvienko.taigergpstracker.MainViewModel
import com.matvienko.taigergpstracker.R
import com.matvienko.taigergpstracker.databinding.FragmentMainBinding
import com.matvienko.taigergpstracker.location.LocationModel
import com.matvienko.taigergpstracker.location.LocationService
import com.matvienko.taigergpstracker.utils.DialogManager
import com.matvienko.taigergpstracker.utils.TimeUtils
import com.matvienko.taigergpstracker.utils.checkPermission
import com.matvienko.taigergpstracker.utils.showToast
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.Timer
import java.util.TimerTask


class FragmentBlank : Fragment() {
    private var locationModel: LocationModel? = null
    private var firstStart = true
    private var pl: Polyline? = null
    private var isServiceRunning = false
    private var timer: Timer? = null
    private var startTime = 0L
    private lateinit var pLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var binding: FragmentMainBinding
    private val model: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        settingsOsm()
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerPermission()
        setOnClicks()
        checkServiceState()
        updateTime()
        registerLocReceiver()
        locationUpdates()

    }
    private fun setOnClicks () = with (binding){
        val listener = onClicks()
        fPlay.setOnClickListener(listener)
    }

    private fun onClicks(): View.OnClickListener {
        return View.OnClickListener {
            when(it.id){
                R.id.fPlay -> startStopService()

            }
        }
    }
    private fun locationUpdates() = with(binding){
        model.locationUpdates.observe(viewLifecycleOwner){
            val distance = "Distance: ${String.format("%.1f", it.distance)} m"
            val velocity = "Velocity: ${String.format("%.1f", 3.6f* it.velocity)} km/h"
            val aVelocity = " Average Velocity: ${getAverageSpeed(it.distance)} km/h"
            tvDistance.text = distance
            tvVelocity.text = velocity
            tvAverageSpeed.text = aVelocity
            locationModel = it
//            trackItem = TrackItem(

//            )
            updatePolyLine(it.geoPointsList)
        }
    }

    private fun updateTime(){
        model.timeData.observe(viewLifecycleOwner){
            binding.tvTime.text = it //tvTime - это textView который мы добавили на слое.
        }
    }

    private fun startTimer(){
        timer?.cancel()
        timer = Timer()
        startTime = LocationService.startTime
        timer?.schedule(object :  TimerTask() {
            override fun run() {
              activity?.runOnUiThread {
                  model.timeData.value = getCurrentTime()
              }
            }

        }, 1,1)
    }
private fun getAverageSpeed (distance: Float) :String {
    return String.format("%.1f", 3.6f * ((distance / (System.currentTimeMillis() - startTime) / 1000.0f)))
}

private fun getCurrentTime():String {
    return "Time: ${TimeUtils.getTime(System.currentTimeMillis()-startTime)}"
}
    private fun geoPointsToString (list: List <GeoPoint>) :String {

        val sb = StringBuilder()
        list.forEach {
            sb.append(it.latitude).append(",").append(it.longitude).append("/") // sb.append ("${it.latitude},${it.longitude}/"))
        }
            Log.d("MyLog", "points: $sb")
        return sb.toString()
    }

    private fun startStopService() {
        if (!isServiceRunning) {
            startLocService()
        } else {
            activity?.stopService(Intent(activity, LocationService::class.java))
            binding.fPlay.setImageResource(R.drawable.ic_play)
            timer?.cancel()
            DialogManager.showSaveDialog(requireContext(),
                getTrackItem(),
                object : DialogManager.Listener {
                override fun onClick() {
                    showToast("Track saved!")
                }
            })
        }
        isServiceRunning = !isServiceRunning
    }
    private fun getTrackItem(): TrackItem {
        return TrackItem(
            null,
            getCurrentTime(),
            TimeUtils.getDate(),
            String.format("%.1f", locationModel?.distance?.div(1000) ?: 0),
            getAverageSpeed(locationModel?.distance ?: 0.0f),
            geoPointsToString(locationModel?.geoPointsList ?: listOf())
        )
    }

    private fun startLocService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity?.startForegroundService(Intent(activity, LocationService::class.java))

        } else {
            activity?.startService(Intent(activity, LocationService::class.java))
        }
        binding.fPlay.setImageResource(R.drawable.ic_stop)
        LocationService.startTime = System.currentTimeMillis()
        startTimer()
    }
    private fun checkServiceState (){
        isServiceRunning = LocationService.isRunning
        if (isServiceRunning){
            binding.fPlay.setImageResource(R.drawable.ic_stop)
            startTimer()
        }
    }



    override fun onResume() {
        super.onResume()
        checkLocPermission()
    }

    private fun settingsOsm() {
        Configuration.getInstance().load(
            activity as AppCompatActivity,
            activity?.getSharedPreferences("osm_pref", Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = BuildConfig.BUILD_TYPE // исходное APPLICATION_ID выяснить причину ошибки!!!
    }

    private fun initOsm() = with(binding) {
        pl = Polyline()
        pl?.outlinePaint?.color = Color.BLUE
        map.controller.setZoom(20.0)
        val mLocProvider = GpsMyLocationProvider(activity)
        val mLocOverlay = MyLocationNewOverlay(mLocProvider, map)
        mLocOverlay.enableMyLocation()
        mLocOverlay.enableFollowLocation()
        mLocOverlay.runOnFirstFix {
            map.overlays.clear()
            map.overlays.add(mLocOverlay)
            map.overlays.add(pl)
        }
    }
    private fun registerPermission() {
        pLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            if (it[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
                initOsm()
                checkLocationEnabled()
            } else {
                showToast("Необходимо разрешение.")
            }
        }
    }

    private fun checkLocPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            checkPermissionAfter10()
        } else {
            checkPermissionBefore10()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun checkPermissionAfter10() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            && checkPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        ) {
            initOsm()
            checkLocationEnabled()
        } else {
            pLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            )
        }
    }
    private fun checkPermissionBefore10() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            && checkPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        ) {
            initOsm()
            checkLocationEnabled()
        } else {
            pLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }
private fun checkLocationEnabled(){
    val lManager = activity?.getSystemService(Context.LOCATION_SERVICE)
            as LocationManager
    val isEnabled = lManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    if (!isEnabled) {
        DialogManager.showLocEnableDialog(
            activity as AppCompatActivity,
            object : DialogManager.Listener {
                override fun onClick() {
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
            }
        )
    }else{
        showToast("Location enabled")
    }

}
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, i: Intent?) {
            if (i?.action == LocationService.LOC_MODEL_INTENT) {
                val locModel =
                    i.getSerializableExtra(LocationService.LOC_MODEL_INTENT) as LocationModel
//                Log.d("MyLog","MF distance: ${locModel.distance}")
                model.locationUpdates.value = locModel
            }
        }
    }
    private fun registerLocReceiver (){
        val locFilter = IntentFilter (LocationService.LOC_MODEL_INTENT)
        LocalBroadcastManager.getInstance(activity as AppCompatActivity)
            .registerReceiver(receiver, locFilter)
    }

    private fun addPoint (list: List <GeoPoint>){
        pl?.addPoint(list[list.size - 1])
    }

    private fun fillPolyLine (list: List<GeoPoint>){
        list.forEach {
            pl?.addPoint(it)
        }
    }

private fun updatePolyLine (list: List<GeoPoint>) {
    if (list.size > 1 && firstStart){
        fillPolyLine(list)
        firstStart = false
    } else {
        addPoint(list)
    }
}

    override fun onDetach() {
        super.onDetach()
        LocalBroadcastManager.getInstance(activity as AppCompatActivity)
            .unregisterReceiver(receiver)
    }

    companion object {

        @JvmStatic
        fun newInstance() = FragmentBlank()

    }
}
