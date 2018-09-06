package com.masterwok.tpbsearchandroid.extensions

import android.support.v7.widget.RecyclerView

/**
 * Disable the initial scroll that occurs when inserting items into a list when the
 * initial scroll position is 0. This seems to occur when using a [@see PagedListAdapter]
 * however this may be limited to some use cases. If a scroll view is auto scrolling to
 * the bottom when items are inserted, then try applying this extension to the adapter.
 */
fun <T : RecyclerView.ViewHolder> RecyclerView.Adapter<T>.disableInitialInsertScroll(
        layoutManager: RecyclerView.LayoutManager
) {
    registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            if (positionStart == 0) {
                layoutManager.scrollToPosition(0)
            }
        }
    })
}