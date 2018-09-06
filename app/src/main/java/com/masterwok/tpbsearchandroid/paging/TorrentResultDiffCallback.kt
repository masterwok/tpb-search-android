package com.masterwok.tpbsearchandroid.paging

import android.support.v7.util.DiffUtil
import com.masterwok.tpbsearchandroid.models.TorrentResult

val TorrentResultDiffCallback = object : DiffUtil.ItemCallback<TorrentResult>() {
    override fun areItemsTheSame(
            oldItem: TorrentResult?
            , newItem: TorrentResult?
    ): Boolean = oldItem == newItem

    override fun areContentsTheSame(
            oldItem: TorrentResult?
            , newItem: TorrentResult?
    ): Boolean = oldItem?.magnet == newItem?.magnet
}
