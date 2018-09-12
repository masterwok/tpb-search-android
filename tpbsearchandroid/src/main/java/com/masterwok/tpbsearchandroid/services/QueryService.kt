@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package com.masterwok.tpbsearchandroid.services

import android.util.Log
import com.masterwok.tpbsearchandroid.contracts.QueryService
import com.masterwok.tpbsearchandroid.extensions.awaitCount
import com.masterwok.tpbsearchandroid.extensions.getQueryResult
import com.masterwok.tpbsearchandroid.extensions.isValidResult
import com.masterwok.tpbsearchandroid.models.QueryResult
import com.masterwok.tpbsearchandroid.models.TorrentResult
import kotlinx.coroutines.experimental.CoroutineStart
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class QueryService constructor(
        private val queryFactories: List<(query: String, pageIndex: Int) -> String>
        , private val verboseLogging: Boolean = false
) : QueryService {

    companion object {
        const val Tag = "QueryService"
    }

    private fun makeRequest(
            url: String
            , timeoutMs: Int
    ): Document? = try {
        Jsoup.connect(url).timeout(timeoutMs).get()
    } catch (ex: Exception) {
        null
    }

    private fun createAsyncRequests(
            queryFactories: List<(query: String, pageIndex: Int) -> String>
            , query: String
            , pageIndex: Int
            , timeoutMs: Int
    ): List<Deferred<QueryResult<TorrentResult>>> = queryFactories.map {
        async(start = CoroutineStart.LAZY) {
            try {
                val document = makeRequest(
                        it(query, pageIndex)
                        , timeoutMs
                )

                if (document.isValidResult()) {
                    document.getQueryResult(pageIndex)
                }

                QueryResult<TorrentResult>(state = QueryResult.State.INVALID)
            } catch (ex: Exception) {
                QueryResult<TorrentResult>(state = QueryResult.State.ERROR)
            }
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
                    , timeoutMs = requestTimeout
            ).awaitCount(
                    count = maxSuccessfulHosts
                    , timeoutMs = queryTimeout
            ).flatten(
                    pageIndex = pageIndex
            )
        } catch (ex: Exception) {
            if (verboseLogging) {
                Log.d(Tag, "An exception occurred while querying", ex)
            }
        }

        return QueryResult(state = QueryResult.State.ERROR)
    }

    private fun List<QueryResult<TorrentResult>>.flatten(
            pageIndex: Int
    ): QueryResult<TorrentResult> {
        val items = this

        return QueryResult(state = QueryResult.State.ERROR)
    }

}

