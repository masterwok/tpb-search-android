package com.masterwok.tpbsearchandroid.paging

import android.arch.paging.PagedListAdapter
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.masterwok.tpbsearchandroid.paging.common.NetworkState
import com.masterwok.tpbsearchandroid.paging.common.NetworkStateViewHolder
import com.masterwok.tpbsearchandroid.models.TorrentResult

private val diffCallback = object : DiffUtil.ItemCallback<TorrentResult>() {
    override fun areItemsTheSame(
            oldItem: TorrentResult?
            , newItem: TorrentResult?
    ): Boolean = oldItem == newItem

    override fun areContentsTheSame(
            oldItem: TorrentResult?
            , newItem: TorrentResult?
    ): Boolean = oldItem?.magnet == newItem?.magnet
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

class SearchPagedListAdapter constructor(
        private val retryCallback: () -> Unit
) : PagedListAdapter<TorrentResult, RecyclerView.ViewHolder>(
        diffCallback
) {
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
        ViewType.ITEM -> SearchResultViewHolder.create(parent)
        else -> throw RuntimeException("Unexpected view type in search adapter.")
    }

    override fun onBindViewHolder(
            holder: RecyclerView.ViewHolder
            , position: Int
    ) = if (holder is SearchResultViewHolder) {
        holder.configure(getItem(position))
    } else {
        (holder as NetworkStateViewHolder).configure(networkState)
    }

}