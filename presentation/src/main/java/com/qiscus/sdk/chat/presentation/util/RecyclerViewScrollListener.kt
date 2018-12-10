package com.qiscus.sdk.chat.presentation.util

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView

class RecyclerViewScrollListener(private val linearLayoutManager: LinearLayoutManager,
                                 private val listener: Listener) : RecyclerView.OnScrollListener() {

    private var onTop: Boolean = false
    private var onBottom = true
    private var onMiddle: Boolean = false


    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        if (linearLayoutManager.findFirstVisibleItemPosition() <= 0 && !onTop) {
            listener.onBottomOffList()
            onBottom = true
            onTop = false
            onMiddle = false
        } else if (linearLayoutManager.findLastVisibleItemPosition() >= linearLayoutManager.itemCount - 1 && !onBottom) {
            listener.onTopOffList()
            onTop = true
            onBottom = false
            onMiddle = false
        } else if (!onMiddle) {
            listener.onMiddleOffList()
            onMiddle = true
            onTop = false
            onBottom = false
        }
    }

    interface Listener {
        fun onTopOffList()

        fun onMiddleOffList()

        fun onBottomOffList()
    }
}