package com.cheat.outcat

import android.app.DatePickerDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.cheat.outcat.base.Global
import com.cheat.outcat.base.OutCatContextBase
import com.cheat.outcat.service.OutCatService
import com.cheat.outcat.util.isAccessibilitySettingsOn
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import java.lang.StringBuilder


class MainActivity : AppCompatActivity() {


    // 申请权限
    private var mBtnReqPermissions: Button? = null

    // 打开大麦首页
    private var mBtnOpenDaMai: Button? = null

    // 点击添加日期
    private var mBtnAddDate: Button? = null

    // 日期选择的chipGroup
    private var mDateChipGroup: ChipGroup? = null

    // 价格的chipGroup
    private var mPriceChipGroup: ChipGroup? = null

    // 输入价格的layout
    private var mInputLayout: TextInputLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView()

        setupClickListener()

        setupListener()

//        showWindow()
        dealHistoryData()
    }

    private fun initView() {
        mBtnReqPermissions = findViewById(R.id.main_req_permission)

        mDateChipGroup = findViewById(R.id.main_chip_date)

        mPriceChipGroup = findViewById(R.id.main_chip_price)

        mInputLayout = findViewById(R.id.main_text_field)

        mBtnOpenDaMai = findViewById(R.id.main_btn_open_damai)

        mBtnAddDate = findViewById(R.id.main_btn_add_date)

    }

    private fun setupClickListener() {

        mBtnOpenDaMai?.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                component = ComponentName("cn.damai", "cn.damai.homepage.MainActivity")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        }

        mBtnAddDate?.setOnClickListener {
            val datePickerDialog = DatePickerDialog(this)
            datePickerDialog.setOnDateSetListener { view, year, month, dayOfMonth ->
                val chipView = generateChip("$year-$month-$dayOfMonth")
                OutCatDataCenter.mAllDateList.add(chipView.text.toString())
                OutCatDataCenter.mSelectedDateList.add(chipView.text.toString())
                mDateChipGroup?.addView(chipView)

                val size = mDateChipGroup?.checkedChipIds?.size ?: -1
                Toast.makeText(this, "${size}", Toast.LENGTH_SHORT).show()

                // 记录在sp中
                recordDateDataToSp()
            }
            datePickerDialog.show()
        }

        mBtnReqPermissions?.setOnClickListener {

            // 判断权限，如果已经申请了，toast提示
            if (hadFloatingWindowPermission() && hadAccessibilitySettingsOn()) {
                Toast.makeText(Global.getContext(), "权限都有了", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 弹窗提醒，危险权限
            MaterialAlertDialogBuilder(this)
                .setTitle("权限申请")
                .setMessage("程序需要获取无障碍服务以及悬浮窗权限\n##拒绝后无法使用该辅助")
                .setNeutralButton("取消") { dialog, which ->
                    // Respond to neutral button press
                }
                .setNegativeButton("拒绝") { dialog, which ->
                    // Respond to negative button press
                }
                .setPositiveButton("同意") { dialog, which ->
                    // Respond to positive button press
                    if (!hadAccessibilitySettingsOn()) {
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        startActivity(intent)
                    }
                    if (!hadFloatingWindowPermission()) {
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
                }
                .show()

        }

        mInputLayout?.setEndIconOnClickListener {
            try {
                val price = mInputLayout?.editText?.text?.toString()?.toInt()
                    ?: return@setEndIconOnClickListener
                mInputLayout?.editText?.text?.clear()

                val chipView = generateChip("${price}元")

                OutCatDataCenter.mAllPriceList.add("${price}元")
                OutCatDataCenter.mSelectedPriceList.add("${price}元")
                mPriceChipGroup?.addView(chipView)

                recordPriceDataToSp()

            } catch (e: Exception) {
                Toast.makeText(this, "${e}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun generateChip(
        text: String
    ): Chip {
        val chipView = Chip(this)
        chipView.isCheckedIconVisible = true
        chipView.checkedIcon =
            resources.getDrawable(R.drawable.baseline_check_24)
        chipView.isCheckable = true
        chipView.isChecked = true
        chipView.text = text
        chipView.setOnClickListener {
            if (!chipView.isChecked) {
                OutCatDataCenter.mSelectedDateList.remove(chipView.text)
                OutCatDataCenter.mSelectedPriceList.remove(chipView.text)

                deleteHistoryFromSp(text)
            }
        }
        chipView.setOnLongClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("删除")
                .setMessage("确定要删除该条目吗")
                .setNeutralButton("取消") { dialog, which ->
                }
                .setPositiveButton("删除") { dialog, which ->
                    mDateChipGroup?.removeView(chipView)

                    OutCatDataCenter.mSelectedDateList.remove(chipView.text)
                    OutCatDataCenter.mSelectedPriceList.remove(chipView.text)


                    deleteHistoryFromSp(text)
                }
                .show()

            true
        }
        return chipView
    }

    private fun deleteHistoryFromSp(text: String) {
        val sp = OutCatContextBase.getDefaultSharePreferences()
        val historyList = sp.getString("dateList", "")
        if (historyList?.contains("$text,") == true) {
            val newList = historyList.removeSuffix("$text,")
            sp.edit().putString("dateList", newList).apply()
        }

        val historyPriceList = sp.getString("priceList", "")
        if (historyPriceList?.contains("$text,") == true) {
            val newList = historyPriceList.removeSuffix("$text,")
            sp.edit().putString("priceList", newList).apply()
        }
    }

    private fun recordPriceDataToSp() {
        val sp = OutCatContextBase.getDefaultSharePreferences()
        val listStr = StringBuilder()
        OutCatDataCenter.mSelectedPriceList.forEach {
            listStr.append("$it,")
        }
        sp.edit().putString("priceList", listStr.toString()).apply()
    }

    private fun recordDateDataToSp() {
        val sp = OutCatContextBase.getDefaultSharePreferences()
        val listStr = StringBuilder()
        OutCatDataCenter.mSelectedDateList.forEach {
            listStr.append("$it,")
        }
        sp.edit().putString("dateList", listStr.toString()).apply()
    }

    private fun setupListener() {
        mDateChipGroup?.setOnCheckedStateChangeListener { group, checkedIds ->
//            Toast.makeText(this, "${checkedIds}", Toast.LENGTH_SHORT).show()
        }
        mPriceChipGroup?.setOnCheckedStateChangeListener { group, checkedIds ->
//            Toast.makeText(this, "${checkedIds}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 是否获取了悬浮窗权限
     */
    private fun hadFloatingWindowPermission(): Boolean {
        return Settings.canDrawOverlays(Global.getContext())
    }

    private fun hadAccessibilitySettingsOn(): Boolean {
        return isAccessibilitySettingsOn(OutCatService::class.java)
    }

    /**
     * 处理历史数据
     */
    private fun dealHistoryData() {
        val sp = OutCatContextBase.getDefaultSharePreferences()
        val dateListStr = sp.getString("dateList", "")
        val dateList = dateListStr?.split(",") ?: emptyList()
        if (dateList.isNotEmpty()) {
            dateList.forEach {
                if (it != "") {
                    mDateChipGroup?.addView(
                        generateChip(it)
                    )
                    OutCatDataCenter.mAllDateList.add(it)
                    OutCatDataCenter.mSelectedDateList.add(it)
                }
            }
        }

        val priceListStr = sp.getString("priceList", "")
        val priceList = priceListStr?.split(",") ?: emptyList()

        if (priceList.isNotEmpty()) {
            priceList.forEach {
                if (it != "") {
                    mPriceChipGroup?.addView(
                        generateChip(it)
                    )
                    OutCatDataCenter.mAllPriceList.add(it)
                    OutCatDataCenter.mSelectedPriceList.add(it)
                }
            }
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

