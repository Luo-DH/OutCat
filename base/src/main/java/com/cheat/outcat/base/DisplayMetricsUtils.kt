package com.cheat.outcat.base

import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager
import androidx.annotation.RequiresApi

object DisplayMetricsUtils {
    private var sDensity = 0f // 根据GOOGLE IO文档中提到的，对density作缓存能起来省电的作用
    private var sScreenWidth = 320 // 屏幕宽px
    private var sScreenHeight = 480 // 屏幕高px

    init {
        val dm = Global.getResources().displayMetrics
        sScreenWidth = dm.widthPixels
        sScreenHeight = dm.heightPixels
    }

    @JvmOverloads
    fun getDensity(context: Context? = null): Float {
        if (sDensity == 0f && context != null) {
            sDensity = context.resources.displayMetrics.density;
        }
        return sDensity;
    }

    /**
     * 获取屏幕正式宽度
     */
    fun getScreenWidth(context: Context?): Int {
        if (context != null) {
            val dm = DisplayMetrics()
            getDisplay(context).getRealMetrics(dm)
            return dm.widthPixels
        }
        return sScreenWidth
    }

    fun getScreenWidth(): Int {
        val wm = OutCatContextBase.getApplication()
            ?.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        if (wm != null) {
            sScreenWidth = wm.defaultDisplay.width
            sScreenHeight = wm.defaultDisplay.height
        }
        return sScreenWidth
    }


    fun getScreenHeight(context: Context?): Int {
        if (context != null) {
            val dm = DisplayMetrics()
            getDisplay(context).getRealMetrics(dm)
            return dm.heightPixels
        }
        return sScreenHeight
    }

    fun getScreenHeight(): Int {
        val wm = OutCatContextBase.getApplication()
            ?.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        if (wm != null) {
            sScreenHeight = wm.defaultDisplay.height
        }
        return sScreenHeight
    }

    private fun getDisplay(context: Context): Display {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getDisplayR(context)
        } else {
            getDisplayL(context)
        }
    }

    private fun getDisplayL(context: Context): Display {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return wm.defaultDisplay
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private fun getDisplayR(context: Context): Display = context.display as Display

}