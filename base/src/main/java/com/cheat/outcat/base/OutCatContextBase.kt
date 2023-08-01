package com.cheat.outcat.base

import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import java.util.concurrent.atomic.AtomicReference

object OutCatContextBase {

    private val sApplication = AtomicReference<Application?>()

    fun getApplicationContext(): Context? {
        return Global.getApplicationContext()
    }

    fun getApplication(): Application? {
        return sApplication.get()
    }

    fun setApplication(application: Application?) {
        requireNotNull(application) { "Application can not be null" }
        if (sApplication.getAndSet(application) != null) {
            //throw new IllegalStateException("Application can only be set once");
        }
    }


    private val mHandle = Handler(Looper.getMainLooper())

    fun getDefaultMainHandler(): Handler {
        return mHandle
    }

}