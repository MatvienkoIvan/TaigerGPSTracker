package com.matvienko.taigergpstracker.location

import org.osmdroid.util.GeoPoint

data class LocationModel(
    val velocity: Float = 0.0f,
    val distance: Float = 0.0f,
    val geoPointList: ArrayList <GeoPoint>
)
