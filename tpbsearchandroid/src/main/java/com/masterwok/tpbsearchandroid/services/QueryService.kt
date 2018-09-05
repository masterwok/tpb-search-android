package com.masterwok.tpbsearchandroid.services

import android.util.Log
import com.masterwok.tpbsearchandroid.contracts.QueryService
import com.masterwok.tpbsearchandroid.extensions.getQueryResult
import com.masterwok.tpbsearchandroid.extensions.isValidResult
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


    // Possible response states:
    // 1. Response was successful -> SUCCESS
    // 2. All responses timed out -> TIMEOUT
    // 3. Invalid page error      -> INVALID
    // 4. Exception               -> ERROR


    override suspend fun query(
            query: String
            , pageIndex: Int
            , queryTimeout: Long
            , requestTimeout: Long
            , maxSuccessfulHosts: Int
    ): QueryResult<TorrentResult> {
        var results: ArrayList<QueryResult<TorrentResult>> = ArrayList()

        try {
            results = queryAllHosts(
                    queryFactories = queryFactories
                    , query = query
                    , pageIndex = pageIndex
                    , maxSuccessfulHosts = Math.min(queryFactories.size, maxSuccessfulHosts)
                    , queryTimeout = queryTimeout
                    , requestTimeout = requestTimeout
            )
        } catch (ex: Exception) {
            Log.e(Tag, "Unknown error occurred: ${ex.message}")
            return QueryResult(state = QueryResult.State.ERROR)
        }

        return results.flatten(pageIndex)
    }

    private fun List<QueryResult<TorrentResult>>.flatten(
            pageIndex: Int
    ): QueryResult<TorrentResult> {
        if(isEmpty()) {
            val x = 1
        }

        val allResultsInTimeoutState = all { it.state == QueryResult.State.TIMEOUT }
        val allResultsInInvalidState = all { it.state == QueryResult.State.INVALID }
        val allResultsInErrorState = all { it.state == QueryResult.State.ERROR }

        // Invalid state returned (probably bad page, consider skipping page on retry)
        if (allResultsInInvalidState) {
            return QueryResult(
                    state = QueryResult.State.INVALID
                    , pageIndex = pageIndex
                    , lastPageIndex = 0
            )
        }

        if (allResultsInTimeoutState) {
            return QueryResult(
                    state = QueryResult.State.TIMEOUT
                    , pageIndex = pageIndex
                    , lastPageIndex = 0
            )
        }

        if (allResultsInErrorState) {
            return QueryResult(
                    state = QueryResult.State.ERROR
                    , pageIndex = pageIndex
                    , lastPageIndex = 0
            )
        }

        // There were 1 or more successful results, flatten and return them.
        val successfulResults = filter { it.isSuccessful() }

        val items = successfulResults
                .flatMap { it.items }
                .distinctBy { it.infoHash }

        val lastPageIndex = successfulResults
                .maxBy { it.lastPageIndex }
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

        val unsuccessfulResult = QueryResult<TorrentResult>(pageIndex = pageIndex)

        try {
            withTimeout(requestTimeout, TimeUnit.MILLISECONDS) {
                response = makeRequest(requestUrl).await()
            }
        } catch (ex: TimeoutCancellationException) {
            Log.w(Tag, "Request timeout: $requestUrl")
            return unsuccessfulResult.apply { state = QueryResult.State.TIMEOUT }
        } catch (ex: JobCancellationException) {
            // Ignored..
        } catch (ex: Exception) {
            Log.w(Tag, "Request failed: $requestUrl")
            return unsuccessfulResult.apply { state = QueryResult.State.ERROR }
        }

        if (!response.isValidResult()) {
            return unsuccessfulResult.apply { state = QueryResult.State.INVALID }
        }

        return try {
            val queryResult = response.getQueryResult(pageIndex)

            // Item had items, consider successful
            return if (queryResult.getItemCount() > 0) {
                queryResult.apply { state = QueryResult.State.SUCCESS }
            } else {
                unsuccessfulResult.apply { state = QueryResult.State.INVALID }
            }
        } catch (ex: Exception) {
            Log.e(Tag, "Failed parsing result", ex)
            unsuccessfulResult.apply { state = QueryResult.State.ERROR }
        }
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
                queryFactories.map { queryFactory ->
                    async(parent = rootJob) {
                        results.add(queryHost(
                                queryFactory
                                , query
                                , pageIndex
                                , requestTimeout
                        ))

                        if(results.isNotEmpty()) {
                            val successfulResultCount = results.count { it.isSuccessful() }

                            if (successfulResultCount == maxSuccessfulHosts) {
                                rootJob.cancelAndJoin()
                                return@async
                            }
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