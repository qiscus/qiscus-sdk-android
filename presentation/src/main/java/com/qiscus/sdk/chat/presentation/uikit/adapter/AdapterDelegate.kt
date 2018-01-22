package com.qiscus.sdk.chat.presentation.uikit.adapter

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup

/**
 * Created on : December 20, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
abstract class AdapterDelegate<in T> {

    abstract fun isForViewType(data: T, position: Int): Boolean

    abstract fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder

    abstract fun onBindViewHolder(data: T, position: Int, holder: RecyclerView.ViewHolder)

    open fun onViewRecycled(viewHolder: RecyclerView.ViewHolder) {}

    open fun onFailedToRecycleView(holder: RecyclerView.ViewHolder): Boolean {
        return false
    }

    open fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {}

    open fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {}
}