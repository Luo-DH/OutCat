package com.cheat.outcat.float

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.Button
import android.widget.LinearLayout
import com.cheat.outcat.OutCatDataCenter
import com.cheat.outcat.R
import com.cheat.outcat.base.Global.startActivity

class FloatView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private var mTvSwitch: Button? = null
    private var mBtnOpenApp: Button? = null

    private var mMoveListener: ((moveX: Float, moveY: Float) -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.float_app_view, this)

        initView()

        initClickListener()
    }

    private fun initView() {
        mTvSwitch = findViewById(R.id.tv_switch)
        mBtnOpenApp = findViewById(R.id.tv_go_to_app)
    }

    private fun initClickListener() {
        mTvSwitch?.setOnClickListener {
            OutCatDataCenter.start = !OutCatDataCenter.start
            OutCatDataCenter.mListener?.invoke()
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