package com.qiscus.sdk.chat.presentation.uikit.adapter

import android.support.v4.util.SparseArrayCompat
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup

/**
 * Created on : December 20, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class AdapterDelegatesManager<T> {
    private var delegates: SparseArrayCompat<AdapterDelegate<T>> = SparseArrayCompat()
    private var fallbackDelegate: AdapterDelegate<T>? = null

    fun addDelegate(delegate: AdapterDelegate<T>): AdapterDelegatesManager<T> {
        var viewType = delegates.size()
        while (delegates.get(viewType) != null) {
            viewType++
            if (viewType == FALLBACK_DELEGATE_VIEW_TYPE) {
                throw IllegalArgumentException("Oops, we are very close to Integer.MAX_VALUE. " +
                        "It seems that there are no more free and unused view type integers left " +
                        "to add another AdapterDelegate.")
            }
        }
        return addDelegate(viewType, false, delegate)
    }

    @JvmOverloads
    fun addDelegate(viewType: Int, allowReplacingDelegate: Boolean = false, delegate: AdapterDelegate<T>): AdapterDelegatesManager<T> {
        if (viewType == FALLBACK_DELEGATE_VIEW_TYPE) {
            throw IllegalArgumentException("The view type = "
                    + FALLBACK_DELEGATE_VIEW_TYPE
                    + " is reserved for fallback adapter delegate (see setFallbackDelegate() )."
                    + " Please use another view type.")
        }

        if (!allowReplacingDelegate && delegates.get(viewType) != null) {
            throw IllegalArgumentException(
                    "An AdapterDelegate is already registered for the viewType = "
                            + viewType + ". Already registered AdapterDelegate is " + delegates.get(viewType))
        }

        delegates.put(viewType, delegate)

        return this
    }

    fun removeDelegate(delegate: AdapterDelegate<T>): AdapterDelegatesManager<T> {
        val indexToRemove = delegates.indexOfValue(delegate)
        if (indexToRemove >= 0) {
            delegates.removeAt(indexToRemove)
        }
        return this
    }

    fun removeDelegate(viewType: Int): AdapterDelegatesManager<T> {
        delegates.remove(viewType)
        return this
    }

    internal fun getItemViewType(data: T, position: Int): Int {
        val delegatesCount = delegates.size()
        for (i in 0 until delegatesCount) {
            val delegate = delegates.valueAt(i)
            if (delegate.isForViewType(data, position)) {
                return delegates.keyAt(i)
            }
        }

        if (fallbackDelegate != null) {
            return FALLBACK_DELEGATE_VIEW_TYPE
        }

        throw NullPointerException("No AdapterDelegate added that matches position = $position in data source")
    }

    internal fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val delegate = getDelegateForViewType(viewType) ?:
                throw NullPointerException("No AdapterDelegate added for ViewType " + viewType)
        return delegate.onCreateViewHolder(parent)
    }

    internal fun onBindViewHolder(data: T, position: Int, viewHolder: RecyclerView.ViewHolder) {
        val delegate = getDelegateForViewType(viewHolder.itemViewType) ?:
                throw NullPointerException(("No delegate found for item at position = "
                        + position + " for viewType = " + viewHolder.itemViewType))
        delegate.onBindViewHolder(data, position, viewHolder)
    }

    internal fun onViewRecycled(viewHolder: RecyclerView.ViewHolder) {
        val delegate = getDelegateForViewType(viewHolder.itemViewType) ?:
                throw NullPointerException(("No delegate found for "
                        + viewHolder + " for item at position = " + viewHolder.adapterPosition
                        + " for viewType = " + viewHolder.itemViewType))
        delegate.onViewRecycled(viewHolder)
    }

    internal fun onFailedToRecycleView(viewHolder: RecyclerView.ViewHolder): Boolean {
        val delegate = getDelegateForViewType(viewHolder.itemViewType) ?:
                throw NullPointerException(("No delegate found for "
                        + viewHolder + " for item at position = " + viewHolder.adapterPosition
                        + " for viewType = " + viewHolder.itemViewType))
        return delegate.onFailedToRecycleView(viewHolder)
    }

    internal fun onViewAttachedToWindow(viewHolder: RecyclerView.ViewHolder) {
        val delegate = getDelegateForViewType(viewHolder.itemViewType) ?: throw NullPointerException(("No delegate found for "
                + viewHolder + " for item at position = " + viewHolder.adapterPosition
                + " for viewType = " + viewHolder.itemViewType))
        delegate.onViewAttachedToWindow(viewHolder)
    }

    internal fun onViewDetachedFromWindow(viewHolder: RecyclerView.ViewHolder) {
        val delegate = getDelegateForViewType(viewHolder.itemViewType) ?: throw NullPointerException(("No delegate found for "
                + viewHolder + " for item at position = " + viewHolder.adapterPosition
                + " for viewType = " + viewHolder.itemViewType))
        delegate.onViewDetachedFromWindow(viewHolder)
    }

    fun setFallbackDelegate(fallbackDelegate: AdapterDelegate<T>): AdapterDelegatesManager<T> {
        this.fallbackDelegate = fallbackDelegate
        return this
    }

    fun getViewType(delegate: AdapterDelegate<T>): Int {
        val index = delegates.indexOfValue(delegate)
        return when (index) {
            -1 -> -1
            else -> delegates.keyAt(index)
        }
    }

    fun getDelegateForViewType(viewType: Int): AdapterDelegate<T>? {
        return delegates.get(viewType, fallbackDelegate)
    }

    companion object {
        internal val FALLBACK_DELEGATE_VIEW_TYPE = Integer.MAX_VALUE - 1
    }
}