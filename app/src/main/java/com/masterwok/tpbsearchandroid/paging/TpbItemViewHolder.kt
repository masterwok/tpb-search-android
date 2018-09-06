package com.masterwok.tpbsearchandroid.paging

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.masterwok.tpbsearchandroid.R
import com.masterwok.tpbsearchandroid.models.TorrentResult
import com.masterwok.tpbsearchandroid.paging.common.NetworkPagedListAdapter
import kotlinx.android.synthetic.main.item_search_result.view.*

class TpbItemViewHolder(
        itemView: View
) : RecyclerView.ViewHolder(itemView)
        , NetworkPagedListAdapter.NetworkViewHolder<TorrentResult?> {

    companion object {
        fun create(parent: ViewGroup): TpbItemViewHolder {
            val view = LayoutInflater
                    .from(parent.context)
                    .inflate(R.layout.item_search_result, parent, false)

            return TpbItemViewHolder(view)
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