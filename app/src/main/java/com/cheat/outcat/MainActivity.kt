package com.cheat.outcat

import android.app.DatePickerDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ServiceCompat.stopForeground
import com.cheat.outcat.base.DisplayMetricsUtils
import com.cheat.outcat.service.OutCatService
import com.cheat.outcat.util.isAccessibilitySettingsOn
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions


class MainActivity : AppCompatActivity() {

    // 是否开启无障碍模式
    private var mTvIsAccessibilitySettingsOn: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView()

//        showWindow()
    }

    private fun initView() {
        mTvIsAccessibilitySettingsOn = findViewById(R.id.tv)

        findViewById<Button>(R.id.btn)?.setOnClickListener {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btn2)?.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                component = ComponentName("cn.damai", "cn.damai.homepage.MainActivity")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        }

        findViewById<Button>(R.id.btn3)?.setOnClickListener {
            XXPermissions.with(this)
                .permission(Permission.SYSTEM_ALERT_WINDOW)
                .request { permissions, allGranted ->
                    Toast.makeText(
                        baseContext,
                        "${allGranted}",
                        Toast.LENGTH_SHORT
                    ).show()
                    if (allGranted) {
                    }
                }
        }


        findViewById<Button>(R.id.btn4)?.setOnClickListener {
            DatePickerDialog(this).show()
        }
    }

    override fun onResume() {
        super.onResume()
        checkPerm()
    }

    private fun checkPerm() {
        if (isAccessibilitySettingsOn(OutCatService::class.java)) {
            mTvIsAccessibilitySettingsOn?.text = "已开启无障碍模式"
        } else {
            mTvIsAccessibilitySettingsOn?.text = "无障碍模式未开启"
        }

    }

    private var overlayView: View? = null
    private var mWindowManager: WindowManager? = null

    private fun showWindow() {
        if (mWindowManager == null) {
            // 获取 WindowManager
            mWindowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            // 创建一个悬浮窗口 View
            overlayView =
                (getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
                    R.layout.float_app_view, null
                ) as ConstraintLayout
            // 设置悬浮窗口参数
            val flag =
                (WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                flag,
                PixelFormat.TRANSLUCENT
            )
            val tvSwitch = overlayView?.findViewById<TextView>(R.id.tv_switch)
            tvSwitch?.setOnClickListener {
//                stopForeground(true)
//                mWindowManager?.removeView(overlayView)
//                stopSelf()
//                isStop = true
            }
            // 设置窗口布局的位置和大小
            params.gravity = Gravity.END or Gravity.TOP
            // 将悬浮窗口 View 添加到 WindowManager 中
            mWindowManager?.addView(overlayView, params)
        }
    }

}

