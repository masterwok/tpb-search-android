@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package com.masterwok.tpbsearchandroid.services

import com.masterwok.tpbsearchandroid.common.extensions.awaitCount
import com.masterwok.tpbsearchandroid.contracts.QueryService
import com.masterwok.tpbsearchandroid.extensions.getQueryResult
import com.masterwok.tpbsearchandroid.common.interruptAsync
import com.masterwok.tpbsearchandroid.models.QueryResult
import com.masterwok.tpbsearchandroid.models.TorrentResult
import kotlinx.coroutines.experimental.*
import org.jsoup.Jsoup
import java.util.concurrent.Executors

class QueryService constructor(
        private val queryFactories: List<(query: String, pageIndex: Int) -> String>
) : QueryService {

    private val queryExecutor = Executors
            .newCachedThreadPool()
            .asCoroutineDispatcher()

    private fun createAsyncRequests(
            queryFactories: List<(query: String, pageIndex: Int) -> String>
            , query: String
            , pageIndex: Int
            , requestTimeout: Int
    ): List<Deferred<QueryResult<TorrentResult>?>> = queryFactories.map { queryFactory ->
        interruptAsync(queryExecutor, start = CoroutineStart.LAZY) {
            try {
                val result = queryEndpoint(
                        queryFactory(query, pageIndex)
                        , pageIndex
                        , requestTimeout
                )

                // Yield further processing should this coroutine no longer be active.
                yield()

                result
            } catch (ex: Exception) {
                QueryResult<TorrentResult>(state = QueryResult.State.ERROR)
            }
        }
    }

    private fun queryEndpoint(
            url: String
            , pageIndex: Int
            , requestTimeoutMs: Int
    ): QueryResult<TorrentResult> {
        return try {
            Jsoup.connect(url)
                    .timeout(requestTimeoutMs)
                    .get()
                    .getQueryResult(pageIndex)
        } catch (ex: Exception) {
            QueryResult(state = QueryResult.State.ERROR)
        }
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
                    , requestTimeout = requestTimeout
            ).awaitCount(
                    count = maxSuccessfulHosts
                    , timeoutMs = queryTimeout
                    , keepUnsuccessful = true
                    , predicate = { queryResult -> queryResult?.isSuccessful() == true }
            ).flatten(
                    pageIndex = pageIndex
            )
        } catch (ex: Exception) {
            return QueryResult(state = QueryResult.State.ERROR)
        }
    }

    private fun List<QueryResult<TorrentResult>?>.flatten(
            pageIndex: Int
    ): QueryResult<TorrentResult> {
        val results = filterNotNull()

        val successResults = results.filter { it.state == QueryResult.State.SUCCESS }
        val invalidResults = results.filter { it.state == QueryResult.State.INVALID }
        val errorResults = results.filter { it.state == QueryResult.State.ERROR }

        if (successResults.isEmpty()) {
            // All results were invalid (this might mean this page is broken, consider skipping)
            if (invalidResults.isNotEmpty() && errorResults.isEmpty()) {
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

