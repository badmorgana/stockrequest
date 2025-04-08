package com.example.stockrequest

import android.app.Application
import com.google.firebase.FirebaseApp

class StockRequestApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
    }
}