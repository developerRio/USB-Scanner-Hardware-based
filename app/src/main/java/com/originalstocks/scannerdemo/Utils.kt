package com.originalstocks.scannerdemo

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import java.io.File
import java.io.IOException


fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
}

fun isRootedDevice(context: Context): Boolean {
    var rootedDevice = false
    val buildTags = Build.TAGS
    if (buildTags != null && buildTags.contains("test-keys")) {
        Log.e("Root Detected", "1")
        rootedDevice = true
    }

    // check if /system/app/Superuser.apk is present
    try {
        val file = File("/system/app/Superuser.apk")
        if (file.exists()) {
            Log.e("Root Detected", "2")
            rootedDevice = true
        }
    } catch (e1: Throwable) {
        //Ignore
    }

    //check if SU command is executable or not
    try {
        Runtime.getRuntime().exec("su")
        Log.e("Root Detected", "3")
        rootedDevice = true
    } catch (localIOException: IOException) {
        //Ignore
    }

    //check weather busy box application is installed
    val packageName = "stericson.busybox" //Package for busy box app
    val pm = context.packageManager
    try {
        Log.e("Root Detected", "4")
        pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
        rootedDevice = true
    } catch (e: PackageManager.NameNotFoundException) {
        //App not installed
    }
    return rootedDevice
}