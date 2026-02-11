package com.aliothmoon.maameow.remote.internal

import android.app.ActivityOptions
import android.content.Intent
import android.content.pm.PackageManager
import com.aliothmoon.maameow.third.FakeContext
import com.aliothmoon.maameow.third.Ln
import com.aliothmoon.maameow.third.wrappers.ServiceManager

object ActivityUtils {
    @JvmStatic
    fun startApp(packageName: String, displayId: Int, forceStop: Boolean = true) {
        val pm: PackageManager = FakeContext.get().packageManager

        val intent = pm.getLaunchIntentForPackage(packageName) ?: run {
            pm.getLeanbackLaunchIntentForPackage(packageName)
        }

        if (intent == null) {
            Ln.w("Cannot create launch intent for app $packageName")
            return
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)

        val launchOptions = ActivityOptions.makeBasic()
        launchOptions.setLaunchDisplayId(displayId)
        val options = launchOptions.toBundle()

        val am = ServiceManager.getActivityManager()
        if (forceStop) {
            am.forceStopPackage(packageName)
        }
        am.startActivity(intent, options)
    }
}