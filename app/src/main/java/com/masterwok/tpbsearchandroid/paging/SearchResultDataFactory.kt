package com.masterwok.tpbsearchandroid.paging

import android.arch.lifecycle.MutableLiveData
import android.arch.paging.DataSource
import com.masterwok.tpbsearchandroid.contracts.QueryService
import com.masterwok.tpbsearchandroid.models.TorrentResult
import kotlinx.coroutines.experimental.Job

class SearchResultDataFactory constructor(
        private val queryService: QueryService
        , private val rootJob: Job
) : DataSource.Factory<Long, TorrentResult>() {

    private val searchLiveData = MutableLiveData<ThePirateBayDataSource>()

    private var query: String? = null

    override fun create(): DataSource<Long, TorrentResult> {
        val dataSource = ThePirateBayDataSource(
                queryService
                , rootJob
                , query
        )

        searchLiveData.postValue(dataSource)

        return dataSource
    }

    fun getMutableLiveData() = searchLiveData

    fun setQuery(query: String?) {
        this.query = query

        searchLiveData
                .value
                ?.invalidate()
    }

}