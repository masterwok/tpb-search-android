package com.masterwok.tpbsearchandroid.fragments

import android.arch.lifecycle.Observer
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.masterwok.tpbsearchandroid.extensions.disableInitialInsertScroll
import com.masterwok.tpbsearchandroid.extensions.dismissKeyboard
import com.masterwok.tpbsearchandroid.extensions.getCompatColor
import com.masterwok.tpbsearchandroid.R
import com.masterwok.tpbsearchandroid.paging.SearchPagedListAdapter
import com.masterwok.tpbsearchandroid.viewmodels.SearchViewModel
import kotlinx.android.synthetic.main.fragment_search.*

class SearchFragment : Fragment() {

    // In real world app, this would be injected.
    private val viewModel: SearchViewModel = SearchViewModel()

    private lateinit var searchAdapter: SearchPagedListAdapter

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        searchAdapter = SearchPagedListAdapter {
            viewModel.retry()
        }

        viewModel.getSearchResultLiveData().observe(this, Observer {
            swipeRefreshLayoutSearch.isRefreshing = false
            searchAdapter.submitList(it)
        })

        viewModel.getNetworkStateLiveData().observe(this, Observer {
            searchAdapter.setNetworkState(it)
        })
    }

    override fun onCreateView(
            inflater: LayoutInflater
            , container: ViewGroup?
            , savedInstanceState: Bundle?
    ): View = inflater.inflate(
            R.layout.fragment_search
            , container
            , false
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        swipeRefreshLayoutSearch.setProgressBackgroundColorSchemeColor(
                context!!.getCompatColor(R.color.castPurple)
        )

        subscribeToViewComponents()
        initSearchRecyclerView()
    }


    private fun initSearchRecyclerView() {
        searchRecyclerView.apply {
            this.layoutManager = LinearLayoutManager(context)
            adapter = searchAdapter.apply {
                disableInitialInsertScroll(layoutManager)
            }
        }
    }

    private fun subscribeToViewComponents() {
        fun queryAndDismissKeyboard() {
            imageButtonSearch.dismissKeyboard()

            viewModel.query(editTextSearch.text.toString())
        }

        imageButtonSearch.setOnClickListener {
            queryAndDismissKeyboard()
        }

        swipeRefreshLayoutSearch.setOnRefreshListener {
            viewModel.refresh()
        }

        editTextSearch.setOnKeyListener { _, _, keyEvent ->
            if (keyEvent.action == KeyEvent.ACTION_DOWN) {
                return@setOnKeyListener false
            }

            when (keyEvent.keyCode) {
                KeyEvent.KEYCODE_DPAD_CENTER -> queryAndDismissKeyboard()
                KeyEvent.KEYCODE_ENTER -> queryAndDismissKeyboard()
                else -> return@setOnKeyListener false
            }

            true
        }
    }
}