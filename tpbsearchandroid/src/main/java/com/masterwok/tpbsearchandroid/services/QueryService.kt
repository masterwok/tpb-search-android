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


    override suspend fun query(
            query: String
            , pageIndex: Int
            , queryTimeout: Long
            , requestTimeout: Int
            , maxSuccessfulHosts: Int
    ): QueryResult<TorrentResult> = getFirstValidResult(
            queryFactories = queryFactories
            , query = query
            , pageIndex = pageIndex
            , requestTimeoutMs = requestTimeout
            , queryTimeoutMs = queryTimeout
    )

    private suspend fun getFirstValidResult(
            queryFactories: List<(query: String, pageIndex: Int) -> String>
            , query: String
            , pageIndex: Int
            , requestTimeoutMs: Int
            , queryTimeoutMs: Long
    ): QueryResult<TorrentResult> = createDeferredQueries(
            queryFactories = queryFactories
            , query = query
            , pageIndex = pageIndex
            , requestTimeoutMs = requestTimeoutMs
    ).awaitCount(
            count = 1
            , timeoutMs = queryTimeoutMs
            , keepUnsuccessful = false
            , predicate = { queryResult -> queryResult?.isSuccessful() == true }
    ).firstOrNull()
            ?: QueryResult(state = QueryResult.State.ERROR)

    private fun createDeferredQueries(
            queryFactories: List<(query: String, pageIndex: Int) -> String>
            , query: String
            , pageIndex: Int
            , requestTimeoutMs: Int
    ) = queryFactories.map { queryFactory ->
        interruptAsync(queryExecutor, start = CoroutineStart.LAZY) {
            val queryResult = queryEndpoint(
                    queryFactory(query, pageIndex)
                    , pageIndex = pageIndex
                    , requestTimeoutMs = requestTimeoutMs
            )

            yield()

            queryResult
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

}

