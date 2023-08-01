package com.cheat.outcat

import android.app.Application
import android.content.Context
import com.cheat.outcat.base.Global

class OutCatApplication : Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)

        Global.init(base)
    }

    override fun onCreate() {
        super.onCreate()

        OutCatApplicationWrapper.setApplication(this)
    }


}