package com.matvienko.taigergpstracker.Fragment

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.matvienko.taigergpstracker.R
import com.matvienko.taigergpstracker.databinding.FragmentMainBinding
import com.matvienko.taigergpstracker.location.LocationService
import com.matvienko.taigergpstracker.utils.DialogManager
import com.matvienko.taigergpstracker.utils.TimeUtils
import com.matvienko.taigergpstracker.utils.checkPermission
import com.matvienko.taigergpstracker.utils.showToast
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.Timer
import java.util.TimerTask


class FragmentBlank : Fragment() {
    private var isServiceRunning = false
    private var timer: Timer? = null
    private var startTime = 0L
    private val timeData = MutableLiveData <String>()
    private lateinit var pLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var binding: FragmentMainBinding

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

    private fun updateTime(){
        timeData.observe(viewLifecycleOwner){
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
                  timeData.value = getCurrentTime()
              }
            }

        }, 1000,1000)
    }

private fun getCurrentTime():String {
    return "Time: ${TimeUtils.getTime(System.currentTimeMillis()-startTime)}"
}

    private fun startStopService() {
        if (!isServiceRunning) {
            startLocService()
        } else {
            activity?.stopService(Intent(activity, LocationService::class.java))
            binding.fPlay.setImageResource(R.drawable.ic_play)
            timer?.cancel()
        }
        isServiceRunning = !isServiceRunning
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
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
    }

    private fun initOsm() = with(binding) {
        map.controller.setZoom(20.0)

        val mLocProvider = GpsMyLocationProvider(activity)
        val mLocOverlay = MyLocationNewOverlay(mLocProvider, map)
        mLocOverlay.enableMyLocation()
        mLocOverlay.enableFollowLocation()
        mLocOverlay.runOnFirstFix {
            map.overlays.clear()
            map.overlays.add(mLocOverlay)
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

    companion object {

        @JvmStatic
        fun newInstance() = FragmentBlank()

    }
}
