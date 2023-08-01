package com.cheat.outcat

import android.app.Application
import com.cheat.outcat.base.OutCatContextBase

object OutCatApplicationWrapper {
    /**
     * 隔离给KaraokeContextBase设置Application
     */
    fun setApplication(application: Application?) {
        OutCatContextBase.setApplication(application)
    }
}