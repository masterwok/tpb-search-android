package com.masterwok.tpbsearchandroid.paging.common

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.masterwok.tpbsearchandroid.R
import kotlinx.android.synthetic.main.item_network_state.view.*


class NetworkStateViewHolder(
        itemView: View
        , private val retryCallback: () -> Unit
) : RecyclerView.ViewHolder(itemView) {

    companion object {
        fun create(parent: ViewGroup, retryCallback: () -> Unit): NetworkStateViewHolder {
            val view = LayoutInflater
                    .from(parent.context)
                    .inflate(R.layout.item_network_state, parent, false)

            return NetworkStateViewHolder(view, retryCallback)
        }
    }

    init {
        itemView.buttonRetry.setOnClickListener {
            retryCallback()
        }
    }

    fun configure(networkState: NetworkState?) {
        if (networkState == null) {
            return
        }

        when (networkState) {
            NetworkState.LOADING -> setLoadingViewState()
            NetworkState.LOADED -> setLoadedViewState()
            NetworkState.ERROR -> setErrorViewState("Something went wrong...")
        }
    }

    private fun setLoadingViewState() {
        itemView.progressBar.visibility = View.VISIBLE
        itemView.textViewErrorMessage.visibility = View.GONE
        itemView.buttonRetry.visibility = View.GONE
    }

    private fun setLoadedViewState() {
        itemView.progressBar.visibility = View.GONE
        itemView.textViewErrorMessage.visibility = View.GONE
        itemView.buttonRetry.visibility = View.GONE
    }

    private fun setErrorViewState(errorMessage: String) {
        itemView.progressBar.visibility = View.GONE
        itemView.buttonRetry.visibility = View.VISIBLE
        itemView.textViewErrorMessage.visibility = View.VISIBLE
        itemView.textViewErrorMessage.text = errorMessage
    }

}
