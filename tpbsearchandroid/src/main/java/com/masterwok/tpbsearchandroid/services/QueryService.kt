@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package com.masterwok.tpbsearchandroid.services

import android.util.Log
import com.masterwok.tpbsearchandroid.common.extensions.awaitCount
import com.masterwok.tpbsearchandroid.contracts.QueryService
import com.masterwok.tpbsearchandroid.extensions.getQueryResult
import com.masterwok.tpbsearchandroid.common.interruptAsync
import com.masterwok.tpbsearchandroid.models.QueryResult
import com.masterwok.tpbsearchandroid.models.TorrentResult
import kotlinx.coroutines.experimental.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.concurrent.Executors

class QueryService constructor(
        private val queryFactories: List<(query: String, pageIndex: Int) -> String>
        , private val verboseLogging: Boolean = false
) : QueryService {

    companion object {
        const val Tag = "QueryService"
    }

    private val queryExecutor = Executors
            .newCachedThreadPool()
            .asCoroutineDispatcher()

    private fun createAsyncRequests(
            queryFactories: List<(query: String, pageIndex: Int) -> String>
            , query: String
            , pageIndex: Int
            , timeoutMs: Int
    ): List<Deferred<QueryResult<TorrentResult>?>> = queryFactories.map { queryFactory ->
        interruptAsync(queryExecutor, start = CoroutineStart.LAZY) {
            try {
                val url = queryFactory(query, pageIndex)

                val result = makeRequest(
                        url
                        , timeoutMs
                ).getQueryResult(pageIndex, url)

                yield()

                Log.d(Tag, "Active: $isActive, $result")

                result
            } catch (ex: Exception) {
                QueryResult<TorrentResult>(state = QueryResult.State.ERROR)
            }
        }
    }

    private fun makeRequest(
            url: String
            , requestTimeoutMs: Int
    ): Document? = try {
        Jsoup.connect(url).timeout(requestTimeoutMs).get()
    } catch (ex: Exception) {
        null
    }

    override suspend fun query(
            query: String
            , pageIndex: Int
            , queryTimeout: Long
            , requestTimeout: Int
            , maxSuccessfulHosts: Int
    ): QueryResult<TorrentResult> {
        try {
            return createAsyncRequests(
                    queryFactories = queryFactories
                    , query = query
                    , pageIndex = pageIndex
                    , timeoutMs = requestTimeout
            ).awaitCount(
                    count = maxSuccessfulHosts
//                    , timeoutMs = queryTimeout
//                    count = queryFactories.size
                    , timeoutMs = 3000
            ).flatten(
                    pageIndex = pageIndex
            )
        } catch (ex: Exception) {
            if (verboseLogging) {
                Log.d(Tag, "An exception occurred during query", ex)
            }

            return QueryResult(state = QueryResult.State.ERROR)
        }
    }

    private fun List<QueryResult<TorrentResult>?>.flatten(
            pageIndex: Int
    ): QueryResult<TorrentResult> {
        val results = filterNotNull()
        var qwer = this

        val successResults = results.filter { it.state == QueryResult.State.SUCCESS }
        val invalidResults = results.filter { it.state == QueryResult.State.INVALID }
        val errorResults = results.filter { it.state == QueryResult.State.ERROR }

        if (successResults.isEmpty()) {
            if (invalidResults.isNotEmpty()) {
                return QueryResult(state = QueryResult.State.INVALID)
            }

            return QueryResult(state = QueryResult.State.ERROR)
        }

        val items = successResults
                .flatMap { it.items }
                .distinctBy { it.infoHash }

        return QueryResult(
                state = QueryResult.State.SUCCESS
                , pageIndex = pageIndex
                , lastPageIndex = successResults.first().lastPageIndex
                , items = items
        )
    }

}

