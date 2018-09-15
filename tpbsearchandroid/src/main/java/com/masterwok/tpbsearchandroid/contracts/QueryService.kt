@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package com.masterwok.tpbsearchandroid.contracts

import com.masterwok.tpbsearchandroid.models.QueryResult
import com.masterwok.tpbsearchandroid.models.TorrentResult

const val DefaultRequestTimeout = 5000
const val DefaultQueryTimeout = 10000L


/**
 * Contract that provides simple interface for querying The Pirate Bay.
 */
interface QueryService {

    /**
     * Simultaneously query each host using the provided [query] and [pageIndex]. The [query]
     * is the search query, and the [pageIndex] is the result page index. Each request will
     * timeout after some [requestTimeoutMs] and the whole query will timeout after some
     * [queryTimeoutMs].
     */
    suspend fun query(
            query: String
            , pageIndex: Int = 0
            , queryTimeoutMs: Long = DefaultQueryTimeout
            , requestTimeoutMs: Int = DefaultRequestTimeout
    ): QueryResult<TorrentResult>

}