package com.masterwok.tpbsearchandroid.services

import android.util.Log
import com.masterwok.tpbsearchandroid.contracts.QueryService
import com.masterwok.tpbsearchandroid.extensions.getQueryResult
import com.masterwok.tpbsearchandroid.models.QueryResult
import com.masterwok.tpbsearchandroid.models.TorrentResult
import kotlinx.coroutines.experimental.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.*
import java.util.concurrent.TimeUnit

class QueryService constructor(
        private val queryFactories: List<(query: String, pageIndex: Int) -> String>
) : QueryService {

    companion object {
        private const val Tag = "QueryService"
    }

    override suspend fun query(
            query: String
            , pageIndex: Int
            , queryTimeout: Long
            , requestTimeout: Long
            , maxSuccessfulHosts: Int
    ): QueryResult<TorrentResult> {
        val results: ArrayList<QueryResult<TorrentResult>> = ArrayList()

        try {
            results.addAll(queryAllHosts(
                    queryFactories = queryFactories
                    , query = query
                    , pageIndex = pageIndex
                    , maxSuccessfulHosts = Math.min(queryFactories.size, maxSuccessfulHosts)
                    , queryTimeout = queryTimeout
                    , requestTimeout = requestTimeout
            ))
        } catch (ex: Exception) {
            Log.e(Tag, "Unknown error occurred: ${ex.message}")
        } finally {
            return results.flatten(pageIndex)
        }
    }

    private fun List<QueryResult<TorrentResult>>.flatten(
            pageIndex: Int
    ): QueryResult<TorrentResult> {
        val items = flatMap { it.items }.distinctBy { it.infoHash }

        val lastPageIndex = this.maxBy { it.lastPageIndex }
                ?.lastPageIndex
                ?: 0

        return QueryResult(
                state = QueryResult.State.SUCCESS
                , pageIndex = pageIndex
                , lastPageIndex = lastPageIndex
                , items = items
        )
    }

    private fun makeRequest(url: String) = async<Document> {
        Jsoup.connect(url).get()
    }

    private suspend fun queryHost(
            queryFactory: (query: String, pageIndex: Int) -> String
            , query: String
            , pageIndex: Int
            , requestTimeout: Long
    ): QueryResult<TorrentResult> {
        val requestUrl = queryFactory(query, pageIndex)
        var response: Document? = null

        val tmpResult = QueryResult<TorrentResult>(
                pageIndex = pageIndex
        )

        try {
            withTimeout(requestTimeout, TimeUnit.MILLISECONDS) {
                response = makeRequest(requestUrl).await()
            }
        } catch (ex: TimeoutCancellationException) {
            Log.w(Tag, "Request timeout: $requestUrl")
            return tmpResult.apply { state = QueryResult.State.TIMEOUT }
        } catch (ex: JobCancellationException) {
            // Ignored..
        } catch (ex: Exception) {
            Log.w(Tag, "Request failed: $requestUrl")
            return tmpResult.apply { state = QueryResult.State.ERROR }
        }

        return response.getQueryResult(pageIndex)
    }

    private suspend fun queryAllHosts(
            queryFactories: List<(query: String, pageIndex: Int) -> String>
            , query: String
            , pageIndex: Int
            , maxSuccessfulHosts: Int
            , queryTimeout: Long
            , requestTimeout: Long
    ): ArrayList<QueryResult<TorrentResult>> {
        val results = ArrayList<QueryResult<TorrentResult>>()
        val rootJob = Job()

        try {
            withTimeout(queryTimeout, TimeUnit.MILLISECONDS) {
                queryFactories.map {
                    async(parent = rootJob) {
                        val pagedResult = queryHost(
                                it
                                , query
                                , pageIndex
                                , requestTimeout
                        )

                        if (pagedResult.state == QueryResult.State.SUCCESS) {
                            results.add(pagedResult)
                        }

                        if (results.size == maxSuccessfulHosts) {
                            rootJob.cancelAndJoin()
                            return@async
                        }
                    }
                }.awaitAll()
            }
        } catch (ex: TimeoutCancellationException) {
            Log.w(Tag, "Query timed out, successful queries: ${results.size}")
        } catch (ignored: JobCancellationException) {
            // Ignored
        } catch (ex: Exception) {
            Log.w(Tag, "Unexpected exception", ex)
        }

        return results
    }


}