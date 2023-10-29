package com.matvienko.taigergpstracker

import com.matvienko.taigergpstracker.DB.MainDb
import android.app.Application

class MainApp : Application() {
val database by lazy{
    MainDb.getDatabase(this) }
}