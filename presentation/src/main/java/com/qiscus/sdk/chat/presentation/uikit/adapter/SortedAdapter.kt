package com.qiscus.sdk.chat.presentation.uikit.adapter

import android.support.v7.util.SortedList
import android.support.v7.widget.RecyclerView

/**
 * Created on : October 10, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
abstract class SortedAdapter<E, H : RecyclerView.ViewHolder> : RecyclerView.Adapter<H>() {

    val data: SortedList<E> by lazy {
        SortedList(getItemClass(), object : SortedList.Callback<E>() {
            override fun compare(lhs: E, rhs: E): Int {
                return this@SortedAdapter.compare(lhs, rhs)
            }

            override fun onInserted(position: Int, count: Int) {
                this@SortedAdapter.onInserted(position, count)
            }

            override fun onRemoved(position: Int, count: Int) {
                this@SortedAdapter.onRemoved(position, count)
            }

            override fun onMoved(fromPosition: Int, toPosition: Int) {
                this@SortedAdapter.onMoved(fromPosition, toPosition)
            }

            override fun onChanged(position: Int, count: Int) {
                this@SortedAdapter.onChanged(position, count)
            }

            override fun areContentsTheSame(oldE: E, newE: E): Boolean {
                return this@SortedAdapter.areContentsTheSame(oldE, newE)
            }

            override fun areItemsTheSame(oldE: E, newE: E): Boolean {
                return this@SortedAdapter.areItemsTheSame(oldE, newE)
            }
        })
    }

    abstract fun getItemClass(): Class<E>

    abstract fun compare(lhs: E, rhs: E): Int

    open protected fun onInserted(position: Int, count: Int) {

    }

    open protected fun onRemoved(position: Int, count: Int) {

    }

    open protected fun onMoved(fromPosition: Int, toPosition: Int) {

    }

    open protected fun onChanged(position: Int, count: Int) {

    }

    open protected fun areContentsTheSame(oldE: E, newE: E): Boolean {
        return oldE == newE
    }

    open protected fun areItemsTheSame(oldE: E, newE: E): Boolean {
        return oldE == newE
    }
}