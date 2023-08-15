package com.cheat.outcat

import java.util.LinkedList

object OutCatDataCenter {

    val mSelectedDateList: LinkedList<String> = LinkedList()

    val mAllDateList: LinkedList<String> = LinkedList()

    val mSelectedPriceList: LinkedList<String> = LinkedList()

    val mAllPriceList: LinkedList<String> = LinkedList()

    var mListener: (() -> Unit)? = null
    fun setClickListener(listener: (() -> Unit)?) {
        mListener = listener
    }

    var start = true
}