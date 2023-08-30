package com.cheat.outcat.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityEvent.TYPE_VIEW_CLICKED
import android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.NotificationCompat
import com.cheat.outcat.ID_BACK
import com.cheat.outcat.ID_BUY
import com.cheat.outcat.ID_YUYUE
import com.cheat.outcat.MainActivity
import com.cheat.outcat.OutCatDataCenter
import com.cheat.outcat.PAGE_YUYUE
import com.cheat.outcat.PAGE_MAIN_FEATURED
import com.cheat.outcat.PAGE_ORDER
import com.cheat.outcat.PAGE_ORDER_LOADING_DIALOG
import com.cheat.outcat.PAGE_SEARCH
import com.cheat.outcat.PAGE_SEARCH_LOADING_DIALOG
import com.cheat.outcat.PAGE_SELECT
import com.cheat.outcat.PAGE_SELECT_LOADING_DIALOG
import com.cheat.outcat.R
import com.cheat.outcat.base.DisplayMetricsUtils
import com.cheat.outcat.base.OutCatContextBase
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis

class OutCatService : AccessibilityService() {
    companion object {
        const val TAG = "OutCatService"
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "onCreate: ")

        createForegroundNotification()?.let {
            startForeground(1, it)
        }

        OutCatDataCenter.setClickListener(mStateChangeListener)
    }

    override fun onDestroy() {
        super.onDestroy()

        OutCatDataCenter.setClickListener(null)
    }

    private val mStateChangeListener: (start: Boolean) -> Unit= {
        if (it) {
            handleEntry(event = mLastEvent, currentPage)
        }
    }


    @Volatile
    private var currentPage = ""

    private var mPage = ""

    // 上次点击的文案
    private var mLastClickText = ""

    private var mLastEvent: AccessibilityEvent? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        Log.i(TAG, "onAccessibilityEvent: ${event}")
        event ?: return

        mLastEvent = event

        if (event.eventType == TYPE_VIEW_CLICKED) {
            mLastClickText = event.text.toString()
        }

        if (event.eventType == TYPE_WINDOW_STATE_CHANGED) {
            // loading弹窗不记录
            if (event.className != PAGE_SEARCH_LOADING_DIALOG) {
                currentPage = event.className.toString()

                if (currentPage != mPage) {
                    if (event.className == PAGE_YUYUE) {
                        Log.i(TAG, "vansing mSelectYuYue重制")
                        mSelectYuYue = false
                    } else if (event.className == PAGE_SELECT) {
                        Log.i(TAG, "vansing mSelectPrice重制")
                        mSelectPrice = false
                    }
                }
            }
        }

        if (!OutCatDataCenter.start) {
            return
        }

        handleEntry(event)

        if (event.eventType == TYPE_WINDOW_STATE_CHANGED && event.className != PAGE_SEARCH_LOADING_DIALOG) {
            mPage = event.className.toString()
        }

    }

    // 是否点击了预约
    private var mSelectYuYue = false
    // 是否选择了价格
    private var mSelectPrice = false


    private fun handleEntry(event: AccessibilityEvent?, lastPage: String? = null) {
        event ?: return
        Log.i(TAG, "handleEntry: ${currentPage} ${event.contentChangeTypes} ${event}")
        when (lastPage ?: event.className) {
            "android.widget.FrameLayout" -> {
                if (currentPage == PAGE_YUYUE) {
                    if (mSelectYuYue) {
                        Log.i(TAG, "vansing 已经点击过了预约")
                        return
                    }
                    val cast = measureTimeMillis {
                        mSelectYuYue = handleYuYue(event)
                    }
                    Log.i(TAG, "vansing onAccessibilityEvent: handleSelect:${cast}")
                } else if (currentPage == PAGE_SELECT) {
                    if (mSelectPrice) {
                        Log.i(TAG, "vansing 已经选择了价格了")
                        OutCatContextBase.getDefaultMainHandler().removeCallbacks(mRunnableClickBack)
                        handleClickBuy(event)
                        return
                    } else {
                        OutCatContextBase.getDefaultMainHandler().removeCallbacks(mRunnableClickBack)
                        OutCatContextBase.getDefaultMainHandler().postDelayed(mRunnableClickBack, 200)
                    }
                    val cast = measureTimeMillis {
                        mSelectPrice = handlePrice(event)
                        if (mSelectPrice) {
                            // 点击购买
                            OutCatContextBase.getDefaultMainHandler().postDelayed({
                                handleClickBuy(event)
                            }, 100)
                        }
                    }
                    Log.i(TAG, "vansing onAccessibilityEvent: handlePrice:${cast} ${mSelectPrice}")
                } else if (currentPage == PAGE_ORDER) {
                    handleOrder(event)
                }
            }
            PAGE_YUYUE -> {
//                val cast = measureTimeMillis {
//                    handleYuYue(event)
//                }
//                Log.i(TAG, "vansing onAccessibilityEvent: handleYuYue:${cast}")
            }



            PAGE_ORDER -> {
//                val cast = measureTimeMillis {
//                    handleOrder(event)
//                }
//                Log.i(TAG, "vansing onAccessibilityEvent: handleOrder:${cast}")
            }
        }
    }

    private val mRunnableClickBack :()->Unit = {
        var clickBackRes = false
        val cast = measureTimeMillis {
            clickBackRes = handleClickBack(mLastEvent!!)
        }
        Log.i(TAG, "vansing: cast:${cast} clickBack ${clickBackRes}")
    }


    // 预约那个页面
    private fun handleYuYue(event: AccessibilityEvent): Boolean {
        Log.i(TAG, "vansing handleYuYue: ${Thread.currentThread().name}")

        // 不在当前页面直接return
        if (currentPage != PAGE_YUYUE) {
            return false
        }
        // 预约抢票
        return getNodeById(event, ID_YUYUE).click() ||
                getNodeByName(event, "预约抢票").click() ||
                getNodeByName(event, "立即购买").click() ||
                getNodeByName(event, "立即预定").click()

    }

    // 选票的页面
    private fun handleSelect(event: AccessibilityEvent) {
        // 不在当前页面直接return
        if (currentPage != PAGE_SELECT) {
            return
        }

        // 上一个页面不是加载页面，直接return
//        if (mPage != PAGE_SELECT_LOADING_DIALOG &&
//            mPage != PAGE_SEARCH_LOADING_DIALOG && mPage != PAGE_YUYUE
//        ) {
//            return
//        }


        // 1. 选择票价
        val selectedPrice = handlePrice(event)
        if (selectedPrice) {
            // 点击购买
            handleClickBuy(event)
            return
        } else {
            OutCatDataCenter.mSelectedDateList.forEach {
                if (mLastClickText.contains(it)) {
                    handleClickBack(event)
                    return
                }
            }
        }

        // 2. 选择日期
        val selectedDate = handleDate(event)
        Log.i(TAG, "handleSelect: selectedDate:${selectedDate} ${event}")
        if (selectedDate) {
            // 如果选上了日期，那么可以结束这次事件
            return
        } else {
            // 如果没有选上日期，考虑点击返回按钮，退出当前页面
//            handleClickBack(event)
        }

    }

    // 提交订单
    private fun handleOrder(event: AccessibilityEvent) {
        // 不在当前页面直接return
        if (currentPage != PAGE_ORDER) {
            return
        }
        if (mPage != PAGE_ORDER_LOADING_DIALOG) {
            return
        }
        var res = false
        res = (getNodeById(event, ID_BUY)).run { this.click() } ||
                (getNodeByName(event, "提交订单")).run { this.click() } ||
                (getNodeByName(event, "确认")).run { this.click() } ||
                (getNodeByName(event, "确定")).run { this.click() }
        if (!res) {
            getNodeById(event, ID_BACK).click()
        }
        return
    }

    // 处理无票的情况，抢余票
    private fun handleEmptyTick(event: AccessibilityEvent): Boolean {
        // 不在当前页面直接return
        if (currentPage != PAGE_SELECT) {
            return false
        }

        // 存在“可预约”这三个字，代表还没有开抢，直接退出当前页面
        val reservationList = getNodeByName(event, "可预约")
        if (reservationList.isNotEmpty()) {
            return getNodeById(event, ID_BACK).click()
        }

        // 如果是提交缺货登记，不点击，直接返回上一个页面
        val emptyTickList = getNodeByName(event, "提交缺货登记")
        if (emptyTickList.isNotEmpty()) {
            return getNodeById(event, ID_BACK).click()
        }

        // 提交开售登记
        val readyList = getNodeByName(event, "提交开售提醒")
        if (readyList.isNotEmpty()) {
            return getNodeById(event, ID_BACK).click()
        }

        // 如果有预售，直接点击预售
        if (getNodeByName(event, "本商品为预售").isEmpty()) {
            getNodeByName(event, "预售").click()
        }

        // 无票的数量，要和场次绑定
        val noTickList = getNodeByName(event, "无票")

        val dateList = ArrayList<List<AccessibilityNodeInfo>>()

        OutCatDataCenter.mSelectedDateList.forEach {
            getNodeByName(event, it).also {
                if (it.click()) {
                    return@forEach
                }
                if (it.isNotEmpty()) {
                    dateList.add(it)
                }
            }
        }

        if (noTickList.size >= dateList.size) {
            return getNodeById(event, ID_BACK).click()
        }

        // 拿到所有缺货登记的坐标
        val queHuoList = getNodeByName(event, "缺货登记")
        val queHuoRectList = ArrayList<Rect>()
        queHuoList.forEach {
            val rect = Rect()
            it.parent.getBoundsInParent(rect)
            queHuoRectList.add(rect)
        }

        OutCatDataCenter.mSelectedPriceList.forEach {
            val que = checkQueHuo(getNodeByName(event, it), queHuoRectList)
            if (que) {
                return@forEach
            }
        }

        // 点击完预售，需要点击票价，可以拿郁可唯的验证
        // 票价来搜，380/480/680/880/1080
//        val price355 = getNodeByName(event, "355元")
//        val price380 = getNodeByName(event, "380元")
//        val price480 = getNodeByName(event, "480元")
//        val price580 = getNodeByName(event, "580元")
//        val price655 = getNodeByName(event, "655元")
//        val price680 = getNodeByName(event, "680元")
//        val price780 = getNodeByName(event, "780元")
//        val price855 = getNodeByName(event, "855元")
//        val price880 = getNodeByName(event, "880元")
//        val price980 = getNodeByName(event, "980元")
//        val price988 = getNodeByName(event, "988元")
//        val price1080 = getNodeByName(event, "1080元")
//        val price1155 = getNodeByName(event, "1155元")
//        val price1280 = getNodeByName(event, "1280元")
//        val price1355 = getNodeByName(event, "1355元")
//        val price1380 = getNodeByName(event, "1380元")
//        val price1555 = getNodeByName(event, "1555元")

//        checkQueHuo(price1555, queHuoRectList) ||
//                checkQueHuo(price1380, queHuoRectList) ||
//                checkQueHuo(price1355, queHuoRectList) ||
//        checkQueHuo(price1280, queHuoRectList) ||
//                checkQueHuo(price1155, queHuoRectList) ||
//                checkQueHuo(price1080, queHuoRectList) ||
//                checkQueHuo(price988, queHuoRectList) ||
//        checkQueHuo(price980, queHuoRectList) ||
//                checkQueHuo(price855, queHuoRectList) ||
//                checkQueHuo(price880, queHuoRectList) ||
//                checkQueHuo(price780, queHuoRectList) ||
//                checkQueHuo(price680, queHuoRectList) ||
//                checkQueHuo(price655, queHuoRectList) ||
//                checkQueHuo(price580, queHuoRectList) ||
//                checkQueHuo(price480, queHuoRectList) ||
//                checkQueHuo(price380, queHuoRectList)
//                checkQueHuo(price355, queHuoRectList)
        return false
    }

    private fun handlePrice(event: AccessibilityEvent): Boolean {
        // 拿到所有缺货登记的坐标
        val queHuoList = getNodeByName(event, "缺货登记")
        val queHuoRectList = ArrayList<Rect>()
        queHuoList.forEach {
            val rect = Rect()
            it.parent.getBoundsInParent(rect)
            queHuoRectList.add(rect)
        }
        OutCatDataCenter.mSelectedPriceList.forEach {
            if (checkQueHuo(getNodeByName(event, it), queHuoRectList))
                return true
        }
        return false
    }

    private fun handleDate(event: AccessibilityEvent): Boolean {

        // 拿到所有缺货登记的坐标
        val noTicketList = getNodeByName(event, "无票")
        val noTicketRectList = ArrayList<Rect>()
        noTicketList.forEach {
            val rect = Rect()
            it.parent.getBoundsInScreen(rect)
            noTicketRectList.add(rect)
        }

        OutCatDataCenter.mSelectedDateList.forEach {
            if (checkQueHuo2(getNodeByName(event, it), noTicketRectList)) {
                return true
            }
        }

        return false
    }

    private fun handleClickBuy(event: AccessibilityEvent): Boolean {
        return (getNodeById(event, ID_BUY)).run { this.click() } ||
                (getNodeByName(event, "立即购买")).run { this.click() } ||
                (getNodeByName(event, "立即预定")).run { this.click() } ||
                (getNodeByName(event, "确认")).run { this.click() } ||
                (getNodeByName(event, "确定")).run { this.click() }
    }

    private fun handleClickBack(event: AccessibilityEvent): Boolean {
        return getNodeById(event, ID_BACK).click()
    }

    /**
     * 检查有没有不缺货的，直接点击不缺货的
     */
    private fun checkQueHuo(a: List<AccessibilityNodeInfo>?, list: List<Rect>): Boolean {
        if (a?.isNotEmpty() == true) {
            val rect = Rect()
            a.first().parent.getBoundsInParent(rect)
            if (rect !in list) {
                return a.first().click()
            }
        }
        return false
    }

    private fun checkQueHuo2(a: List<AccessibilityNodeInfo>?, list: List<Rect>): Boolean {
        if (a?.isNotEmpty() == true) {
            val rect = Rect()
            a.first().parent.getBoundsInScreen(rect)
            if (rect !in list) {
                return a.first().click()
            }
        }
        return false
    }

    override fun onInterrupt() {
        Log.i(TAG, "onInterrupt: ")
    }

    /**
     * 开启前台通知
     */
    private fun createForegroundNotification(): Notification? {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        val channelId = "outCat"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(channelId, "出猫辅助", NotificationManager.IMPORTANCE_HIGH)
            notificationManager?.createNotificationChannel(channel)
        }
        return NotificationCompat.Builder(this, channelId)
            // 设置点击notification跳转，比如跳转到设置页
            .setContentIntent(
                PendingIntent.getActivity(
                    this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
                )
            ).setSmallIcon(R.drawable.ic_launcher_foreground) // 设置小图标
            .setContentTitle(getString(R.string.app_name)).setContentText("出猫辅助")
            .setTicker("出猫辅助").build()
    }
}

fun AccessibilityNodeInfo?.click(): Boolean {
    if (this == null) return false
    if (this.isClickable) {
        return this.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    } else {
        return this.parent.click()
    }
}

fun List<AccessibilityNodeInfo>.click(): Boolean {
    if (this.isNotEmpty()) {
        return this.first().click()
    } else {
        return false
    }
}


private fun getNodeByName(
    event: AccessibilityEvent,
    name: String
): List<AccessibilityNodeInfo> {
    return event.source?.findAccessibilityNodeInfosByText(name) ?: emptyList()
}

private fun getNodeById(
    event: AccessibilityEvent,
    id: String
): List<AccessibilityNodeInfo> {
    return event.source?.findAccessibilityNodeInfosByViewId(id) ?: emptyList()
}
