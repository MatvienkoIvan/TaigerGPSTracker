package com.matvienko.taigergpstracker

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.matvienko.taigergpstracker.location.LocationModel

class MainViewModel : ViewModel() {
    val locationUpdates = MutableLiveData <LocationModel>()
    val timeData = MutableLiveData <String>()
}