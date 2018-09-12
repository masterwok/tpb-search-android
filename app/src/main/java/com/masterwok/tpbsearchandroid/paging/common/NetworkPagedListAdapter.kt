package com.masterwok.bitcast.paging.common

import android.arch.paging.PagedListAdapter
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup

class NetworkPagedListAdapter<T>(
        diffCallback: DiffUtil.ItemCallback<T>
        , private val retryCallback: () -> Unit
        , private val resultViewHolderFactory: (parent: ViewGroup) -> RecyclerView.ViewHolder
) : PagedListAdapter<T, RecyclerView.ViewHolder>(diffCallback) {

    interface NetworkViewHolder<T> {
        fun configure(model: T)
    }

    private enum class ViewType(val value: Int) {
        NETWORK(0),
        ITEM(1);

        companion object {
            private val map = ViewType
                    .values()
                    .associateBy(ViewType::value)

            fun getValue(value: Int) = map[value]
        }
    }

    private var networkState: NetworkState? = null

    private fun hasExtraRow(): Boolean = networkState != null
            && networkState != NetworkState.LOADED

    fun setNetworkState(newNetworkState: NetworkState?) {
        val previousNetworkState = networkState
        val previousExtraRow = hasExtraRow()

        networkState = newNetworkState

        val hasExtraRow = hasExtraRow()

        if (previousExtraRow != hasExtraRow) {
            if (previousExtraRow) {
                notifyItemRemoved(itemCount)
            } else {
                notifyItemInserted(itemCount)
            }
        } else if (hasExtraRow && previousNetworkState != newNetworkState) {
            notifyItemChanged(itemCount - 1)
        }
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + if (hasExtraRow()) 1 else 0
    }

    override fun getItemViewType(position: Int): Int =
            if (hasExtraRow() && position == itemCount - 1) {
                ViewType.NETWORK.value
            } else {
                ViewType.ITEM.value
            }

    override fun onCreateViewHolder(
            parent: ViewGroup
            , viewType: Int
    ): RecyclerView.ViewHolder = when (ViewType.getValue(viewType)) {
        ViewType.NETWORK -> NetworkStateViewHolder.create(parent, retryCallback)
        ViewType.ITEM -> resultViewHolderFactory.invoke(parent)
        else -> throw RuntimeException("Unexpected view type in network adapter.")
    }

    override fun onBindViewHolder(
            holder: RecyclerView.ViewHolder
            , position: Int
    ) {
        if(holder is NetworkStateViewHolder) {
            holder.configure(networkState)
            return
        }

        @Suppress("UNCHECKED_CAST")
        (holder as NetworkViewHolder<T?>).configure(getItem(position))
    }
}