package com.aliothmoon.maameow.utils.log

import android.util.Log
import com.aliothmoon.maameow.data.log.ApplicationLogWriter
import timber.log.Timber


class FileLogTree(private val writer: ApplicationLogWriter) : Timber.DebugTree() {


    override fun isLoggable(tag: String?, priority: Int): Boolean {
        return priority >= Log.WARN
    }


    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        writer.submit(priority, tag, message, t)
    }
}
