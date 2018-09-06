package com.masterwok.tpbsearchandroid.paging.search

import android.arch.lifecycle.MutableLiveData
import android.arch.paging.DataSource
import com.masterwok.tpbsearchandroid.contracts.QueryService
import com.masterwok.tpbsearchandroid.models.TorrentResult
import kotlinx.coroutines.experimental.Job

class TpbDataFactory constructor(
        private val queryService: QueryService
        , private val rootJob: Job
        , private val verboseLogging: Boolean = false
) : DataSource.Factory<Long, TorrentResult>() {

    private val searchLiveData = MutableLiveData<TpbDataSource>()

    private var query: String? = null

    override fun create(): DataSource<Long, TorrentResult> {
        val dataSource = TpbDataSource(
                queryService
                , rootJob
                , query
                , verboseLogging
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