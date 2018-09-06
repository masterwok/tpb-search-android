package com.masterwok.tpbsearchandroid.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import com.masterwok.tpbsearchandroid.constants.QueryFactories
import com.masterwok.tpbsearchandroid.contracts.QueryService
import com.masterwok.tpbsearchandroid.models.TorrentResult
import com.masterwok.tpbsearchandroid.paging.common.NetworkState
import com.masterwok.tpbsearchandroid.paging.search.TpbDataFactory
import com.masterwok.tpbsearchandroid.paging.search.TpbDataSource
import kotlinx.coroutines.experimental.Job
import java.util.concurrent.Executors


class SearchViewModel : ViewModel() {

    // In a real world app this dependency should be injected.
    private val queryService: QueryService = com
            .masterwok
            .tpbsearchandroid
            .services
            .QueryService(
                    QueryFactories
                    , verboseLogging = true
            )

    private val rootJob = Job()

    private val executor = Executors.newFixedThreadPool(5)

    private val searchDataFactory = TpbDataFactory(
            queryService
            , rootJob
            , verboseLogging = true
    )

    private val pagedListConfig = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setInitialLoadSizeHint(30)
            .setPageSize(30)
            .build()

    private val searchResultItemLiveData = LivePagedListBuilder<Long, TorrentResult>(
            searchDataFactory
            , pagedListConfig
    ).setFetchExecutor(executor).build()

    private val networkState = Transformations.switchMap(
            searchDataFactory.getMutableLiveData()
    ) { dataSource: TpbDataSource? -> dataSource?.networkState }

    private fun invalidate() = searchDataFactory
            .getMutableLiveData()
            .value
            ?.invalidate()

    fun getSearchResultLiveData(): LiveData<PagedList<TorrentResult>> = searchResultItemLiveData

    fun getNetworkStateLiveData(): LiveData<NetworkState> = networkState

    fun retry() = searchDataFactory
            .getMutableLiveData()
            .value
            ?.retry()

    fun refresh() = invalidate()

    fun query(query: String?) {
        if (query == null || query.isEmpty()) {
            searchDataFactory.setQuery(null)
            return
        }

        searchDataFactory.setQuery(query)
    }

    override fun onCleared() {
        super.onCleared()

        // Ensure any pending retry or refresh is cancelled.
        rootJob.cancel()
    }

}
