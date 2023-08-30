package com.cheat.outcat

import java.util.LinkedList

object OutCatDataCenter {

    val mSelectedDateList: LinkedList<String> = LinkedList()

    val mAllDateList: LinkedList<String> = LinkedList()

    val mSelectedPriceList: LinkedList<String> = LinkedList()

    val mAllPriceList: LinkedList<String> = LinkedList()

    var mListener: ((Boolean) -> Unit)? = null
    fun setClickListener(listener: ((Boolean) -> Unit)?) {
        mListener = listener
    }

    var start = true
}