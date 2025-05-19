package com.example.guitarapp.utils

import android.view.View

class DoubleClickListener(
    private val onDoubleClick: () -> Unit,
    private val onSingleClick: () -> Unit
) : View.OnClickListener {

    private var lastClickTime: Long = 0
    private val DOUBLE_CLICK_TIME_DELTA: Long = 300

    override fun onClick(v: View?) {
        val clickTime = System.currentTimeMillis()
        if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
            onDoubleClick()
        } else {
            onSingleClick()
        }
        lastClickTime = clickTime
    }
}

