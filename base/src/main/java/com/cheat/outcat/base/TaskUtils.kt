package com.cheat.outcat.base

import android.os.Handler
import android.os.Looper

private val sUiThreadHandler = Handler(Looper.getMainLooper())

/**
 * 在主线程中运行代码
 */
fun runOnUiThread(runnable: Runnable) {
    runOnUiThread { runnable.run() }
}

fun runOnUiThread(action: () -> Unit) {
    if (Looper.myLooper() == Looper.getMainLooper()) {
        action.invoke()
    } else {
        sUiThreadHandler.post(action)
    }
}