package com.masterwok.tpbsearchandroid.services

import android.util.Log
import com.masterwok.tpbsearchandroid.contracts.QueryService
import com.masterwok.tpbsearchandroid.extensions.getPagedResult
import com.masterwok.tpbsearchandroid.models.PagedResult
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
    ): PagedResult<TorrentResult> {
        val results: ArrayList<PagedResult<TorrentResult>> = ArrayList()

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

    private fun List<PagedResult<TorrentResult>>.flatten(
            pageIndex: Int
    ): PagedResult<TorrentResult> = PagedResult(
            pageIndex = pageIndex
            , lastPageIndex = maxBy { it.lastPageIndex }?.lastPageIndex ?: 0
            , items = flatMap { it.items }.distinctBy { it.infoHash }
    )

    private fun makeRequest(url: String) = async<Document> {
        Jsoup.connect(url).get()
    }

    private suspend fun queryHost(
            queryFactory: (query: String, pageIndex: Int) -> String
            , query: String
            , pageIndex: Int
            , requestTimeout: Long
    ): PagedResult<TorrentResult> {
        val requestUrl = queryFactory(query, pageIndex)
        var response: Document? = null

        try {
            withTimeout(requestTimeout, TimeUnit.MILLISECONDS) {
                response = makeRequest(requestUrl).await()
            }
        } catch (ex: TimeoutCancellationException) {
            Log.w(Tag, "Request timeout: $requestUrl")
        } catch (ex: JobCancellationException) {
            // Ignored..
        } catch (ex: Exception) {
            Log.w(Tag, "Request failed: $requestUrl")
        }

        return response.getPagedResult(pageIndex)
    }

    private suspend fun queryAllHosts(
            queryFactories: List<(query: String, pageIndex: Int) -> String>
            , query: String
            , pageIndex: Int
            , maxSuccessfulHosts: Int
            , queryTimeout: Long
            , requestTimeout: Long
    ): ArrayList<PagedResult<TorrentResult>> {
        val results = ArrayList<PagedResult<TorrentResult>>()
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

                        // A page should always have some results.
                        if (pagedResult.itemCount > 0
                                // Guard heuristic, we know the last page index should be greater than
                                // or equal to the requested page index. The proxy doesn't have the page
                                // if this check fails.
                                && pagedResult.lastPageIndex >= pageIndex) {
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