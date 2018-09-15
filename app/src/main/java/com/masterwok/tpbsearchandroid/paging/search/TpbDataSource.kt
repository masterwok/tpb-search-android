package com.masterwok.tpbsearchandroid.paging.search


import android.arch.lifecycle.MutableLiveData
import android.arch.paging.PageKeyedDataSource
import android.util.Log
import com.masterwok.tpbsearchandroid.contracts.DefaultRequestTimeout
import com.masterwok.tpbsearchandroid.paging.common.NetworkState
import com.masterwok.tpbsearchandroid.contracts.QueryService
import com.masterwok.tpbsearchandroid.models.QueryResult
import com.masterwok.tpbsearchandroid.models.TorrentResult
import kotlinx.coroutines.experimental.CoroutineStart
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import kotlinx.coroutines.experimental.sync.Mutex
import kotlinx.coroutines.experimental.sync.withLock

class TpbDataSource constructor(
        private val queryService: QueryService
        , private val rootJob: Job
        , private val query: String?
        , private val verboseLogging: Boolean = false
) : PageKeyedDataSource<Long, TorrentResult>() {

    companion object {
        private const val Tag = "TpbDataSource"

        private const val QueryTimeout = 10000L
    }

    val networkState: MutableLiveData<NetworkState> = MutableLiveData()

    private val searchResults = ArrayList<TorrentResult>()
    private var lastPageIndex: Int = 0
    private var retryJob: Job? = null
    private var queryJob: Job? = null
    private val mutex = Mutex()

    override fun loadInitial(
            params: LoadInitialParams<Long>
            , callback: LoadInitialCallback<Long, TorrentResult>
    ) {
        queryJob?.cancel()

        if (query == null) {
            return
        }

        networkState.postValue(NetworkState.LOADING)

        queryJob = launch {
            mutex.withLock {

                val requestedLoadSize = params.requestedLoadSize
                val queryResult = queryService.query(
                        query = query
                        , pageIndex = 0
                        , queryTimeout = QueryTimeout
                        , requestTimeout = DefaultRequestTimeout
                )

                if (queryResult.isSuccessful()) {
                    searchResults.addAll(queryResult.items)
                    lastPageIndex = queryResult.lastPageIndex

                    networkState.postValue(NetworkState.LOADED)

                    callback.onResult(getItemRange(0, requestedLoadSize - 1), null, 1)

                    return@withLock
                }

                setRetry { loadInitial(params, callback) }
                networkState.postValue(NetworkState.ERROR)
            }
        }
    }

    override fun loadAfter(
            params: LoadParams<Long>
            , callback: LoadCallback<Long, TorrentResult>
    ) {
        if (query == null) {
            return
        }

        networkState.postValue(NetworkState.LOADING)

        launch {
            mutex.withLock {
                val pageIndex = params.key.toInt()
                val requestedLoadSize = params.requestedLoadSize
                val itemOffset = pageIndex * requestedLoadSize
                val endIndex = itemOffset + requestedLoadSize
                val isLastPage = pageIndex == lastPageIndex

                if (verboseLogging) {
                    Log.d(Tag, "page: $pageIndex/$lastPageIndex")
                }

                // Already have items in requested range
                if (endIndex <= searchResults.size - 1) {
                    networkState.postValue(NetworkState.LOADED)

                    if (verboseLogging) {
                        Log.d(Tag, "Already had range, page: $pageIndex/$lastPageIndex")
                    }

                    callback.onResult(
                            getItemRange(itemOffset, endIndex)
                            , if (isLastPage) null else pageIndex + 1L
                    )

                    return@withLock
                }

                val queryResult = queryService.query(query, pageIndex, QueryTimeout)

                if (verboseLogging) {
                    Log.d(Tag, queryResult.toString())
                }

                if (queryResult.isSuccessful()) {
                    networkState.postValue(NetworkState.LOADED)

                    searchResults.addAll(queryResult.items)

                    callback.onResult(
                            getItemRange(itemOffset, endIndex)
                            , if (isLastPage) null else pageIndex + 1L
                    )

                    return@withLock
                }

                // Timeout or error, retry request.
                if (queryResult.state == QueryResult.State.ERROR) {
                    networkState.postValue(NetworkState.ERROR)
                    setRetry { loadAfter(params, callback) }
                    return@withLock
                }

                if (queryResult.state == QueryResult.State.INVALID) {
                    // Invalid and last page (can't skip), return empty results.
                    if (isLastPage) {
                        networkState.postValue(NetworkState.LOADED)
                        callback.onResult(ArrayList(), null)
                        return@withLock
                    }

                    // Invalid and not last page, skip to next page.
                    networkState.postValue(NetworkState.ERROR)
                    val skipPageParams = LoadParams<Long>(params.key + 1, params.requestedLoadSize)
                    setRetry { loadAfter(skipPageParams, callback) }
                }
            }
        }
    }

    override fun loadBefore(
            params: LoadParams<Long>
            , callback: LoadCallback<Long, TorrentResult>
    ) {
        // Intentionally left blank..
    }

    fun retry() {
        retryJob?.start()
    }

    private fun setRetry(action: () -> Unit) {
        retryJob = launch(parent = rootJob, start = CoroutineStart.LAZY) {
            action()
        }
    }

    override fun invalidate() = runBlocking {
        queryJob?.cancel()

        searchResults.clear()
        lastPageIndex = 0

        super.invalidate()
    }

    private fun getItemRange(
            startIndex: Int
            , endIndex: Int
    ): ArrayList<TorrentResult> {
        val lastItemIndex = Math.max(0, searchResults.size - 1)
        val toIndex = Math.min(endIndex, lastItemIndex)

        return if (startIndex > toIndex) {
            ArrayList()
        } else {
            ArrayList(searchResults.subList(startIndex, toIndex))
        }
    }

}

