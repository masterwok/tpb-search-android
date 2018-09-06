package com.masterwok.tpbsearchandroid.paging

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.masterwok.tpbsearchandroid.R
import com.masterwok.tpbsearchandroid.models.TorrentResult
import kotlinx.android.synthetic.main.item_search_result.view.*

class SearchResultViewHolder(
        itemView: View
) : RecyclerView.ViewHolder(itemView) {

    companion object {
        fun create(parent: ViewGroup): SearchResultViewHolder {
            val view = LayoutInflater
                    .from(parent.context)
                    .inflate(R.layout.item_search_result, parent, false)

            return SearchResultViewHolder(view)
        }
    }

    private fun clear() {
        itemView.textViewTitle.text = null
        itemView.textViewUploadedOn.text = null
        itemView.textViewSize.text = null
        itemView.textViewSeeders.text = null
        itemView.textViewLeechers.text = null
    }

    fun configure(searchResultItem: TorrentResult?) {
        if (searchResultItem == null) {
            clear()
            return
        }

        itemView.textViewTitle.text = searchResultItem.title
        itemView.textViewUploadedOn.text = searchResultItem.displayUploadedOn
        itemView.textViewSize.text = searchResultItem.displaySize
        itemView.textViewSeeders.text = searchResultItem.seeders.toString()
        itemView.textViewLeechers.text = searchResultItem.leechers.toString()
    }

}