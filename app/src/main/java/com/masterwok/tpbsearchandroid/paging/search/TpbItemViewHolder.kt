package com.masterwok.bitcast.paging.search

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.masterwok.bitcast.R
import com.masterwok.bitcast.paging.common.NetworkPagedListAdapter
import com.masterwok.tpbsearchandroid.models.TorrentResult
import kotlinx.android.synthetic.main.item_search_result.view.*

class TpbItemViewHolder(
        itemView: View
        , private val onClick: (torrentResult: TorrentResult?) -> Unit
) : RecyclerView.ViewHolder(itemView)
        , NetworkPagedListAdapter.NetworkViewHolder<TorrentResult?> {

    companion object {
        fun create(
                parent: ViewGroup
                , onClick: (torrentResult: TorrentResult?) -> Unit
        ): TpbItemViewHolder {
            val view = LayoutInflater
                    .from(parent.context)
                    .inflate(R.layout.item_search_result, parent, false)

            return TpbItemViewHolder(view, onClick)
        }
    }

    private var model: TorrentResult? = null

    init {
        itemView.relativeLayoutSearchResult.setOnClickListener {
            onClick(model)
        }
    }

    private fun clear() {
        itemView.textViewTitle.text = null
        itemView.textViewUploadedOn.text = null
        itemView.textViewSize.text = null
        itemView.textViewSeeders.text = null
        itemView.textViewLeechers.text = null
    }

    override fun configure(model: TorrentResult?) {
        this.model = model

        if (model == null) {
            clear()
            return
        }

        itemView.textViewTitle.text = model.title
        itemView.textViewUploadedOn.text = model.displayUploadedOn
        itemView.textViewSize.text = model.displaySize
        itemView.textViewSeeders.text = model.seeders.toString()
        itemView.textViewLeechers.text = model.leechers.toString()
    }

}