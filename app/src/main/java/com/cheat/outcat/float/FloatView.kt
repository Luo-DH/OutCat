package com.cheat.outcat.float

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.provider.Settings
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.cheat.outcat.OutCatDataCenter
import com.cheat.outcat.R
import com.cheat.outcat.base.Global.startActivity
import com.cheat.outcat.service.OutCatService
import com.cheat.outcat.util.isAccessibilitySettingsOn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

class FloatView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private var mTvSwitch: Button? = null
    private var mBtnOpenApp: Button? = null

    private var mTvTips: TextView? = null
    private var mBtnReqPermissions: Button? = null

    private var mTvShowDate: TextView? = null

    private var mMoveListener: ((moveX: Float, moveY: Float) -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.float_app_view, this)

        initView()

        initClickListener()

        checkPermission()
    }

    private fun initView() {
        mTvSwitch = findViewById(R.id.tv_switch)
        mBtnOpenApp = findViewById(R.id.tv_go_to_app)

        mTvTips = findViewById(R.id.tv_tips)
        mBtnReqPermissions = findViewById(R.id.btn_req_per)

    }

    private fun initClickListener() {
        mTvSwitch?.setOnClickListener {
            OutCatDataCenter.start = !OutCatDataCenter.start
            OutCatDataCenter.mListener?.invoke(OutCatDataCenter.start)
            if (OutCatDataCenter.start) {
                mTvSwitch?.text = "点击停止"
            } else {
                mTvSwitch?.text = "点击开始"
            }
        }
        mBtnOpenApp?.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                addFlags(FLAG_ACTIVITY_NEW_TASK)
                component = ComponentName("com.cheat.outcat", "com.cheat.outcat.MainActivity")
            })
        }
        mBtnReqPermissions?.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                addFlags(FLAG_ACTIVITY_NEW_TASK)
                component = ComponentName("com.cheat.outcat", "com.cheat.outcat.MainActivity")
            })
            if (!this.context.isAccessibilitySettingsOn(OutCatService::class.java)) {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).also {
                    it.addFlags(FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(intent)
            }
        }
    }

    fun checkPermission() {
        if (!this.context.isAccessibilitySettingsOn(OutCatService::class.java)) {
            mTvTips?.visibility = View.VISIBLE
            mBtnReqPermissions?.visibility = View.VISIBLE
            mTvSwitch?.visibility = View.GONE
        } else {
            mTvTips?.visibility = View.GONE
            mBtnReqPermissions?.visibility = View.GONE
            mTvSwitch?.visibility = View.VISIBLE
        }
    }

    fun showCurrTime() {
        val formatter = SimpleDateFormat("YYYY-MM-dd HH:mm:ss") //设置时间格式


        formatter.setTimeZone(TimeZone.getTimeZone("GMT+08")) //设置时区


        val curDate = Date(System.currentTimeMillis()) //获取当前时间


        val createDate: String = formatter.format(curDate) //格式转换

        mTvTips?.text = createDate
    }


    fun setOnMoveListener(listener: (moveX: Float, moveY: Float) -> Unit) {
        mMoveListener = listener
    }

    private var mDownX: Float = 0f
    private var mDownY: Float = 0f

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mDownX = event.rawX
                mDownY = event.rawY
            }

            MotionEvent.ACTION_MOVE -> {
                val moveX = event.rawX - mDownX
                val moveY = event.rawY - mDownY

                mMoveListener?.invoke(moveX, moveY)

                mDownX = event.rawX
                mDownY = event.rawY
            }
        }
        return true
    }


}