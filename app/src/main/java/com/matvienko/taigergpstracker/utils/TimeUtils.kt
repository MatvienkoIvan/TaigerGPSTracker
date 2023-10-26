package com.matvienko.taigergpstracker.utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.TimeZone
@SuppressLint("SimpleDateFormat")
object TimeUtils {

    private val timeFormatter = SimpleDateFormat ("HH:mm:ss") //24 часовой формат
    private val dataFormatter = SimpleDateFormat ("dd/MM/yyyy HH:mm") //24 часовой формат
    fun getTime(timeInMillis: Long): String {
        val cv = Calendar.getInstance()
        timeFormatter.timeZone = TimeZone.getTimeZone("UTC")
        cv.timeInMillis = timeInMillis
        return timeFormatter.format(cv.time)
    }

    fun getDate(): String {
        val cv = Calendar.getInstance()
//        dataFormatter.timeZone = TimeZone.getTimeZone("UTC")
//        cv.timeInMillis = timeInMillis
        return dataFormatter.format(cv.time)

    }

}