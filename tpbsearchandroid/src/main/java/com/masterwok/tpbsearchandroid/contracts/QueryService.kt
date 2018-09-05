package com.masterwok.tpbsearchandroid.contracts

import com.masterwok.tpbsearchandroid.models.QueryResult
import com.masterwok.tpbsearchandroid.models.TorrentResult

const val DefaultRequestTimeout = 5000L
const val DefaultQueryTimeout = 10000L
const val DefaultMaxSuccessfulHosts = 5

interface QueryService {

    /**
     * Query each host entry using the provided [query] and [pageIndex]. The [query]
     * is the search query, and the [pageIndex] is the result page index. The value
     * of [maxSuccessfulHosts] is the maximum number of successful queries to wait for.
     * A successful query is a query that return 1 or more items. Once the maximum number
     * of successful queries is reached, then all other jobs are cancelled. If the maximum
     * number of successful queries is not reached before the provided [queryTimeout], then
     * any successful results at that point are returned.
     */
    suspend fun query(
            query: String
            , pageIndex: Int = 0
            , queryTimeout: Long = DefaultQueryTimeout
            , requestTimeout: Long = DefaultRequestTimeout
            , maxSuccessfulHosts: Int = DefaultMaxSuccessfulHosts
    ): QueryResult<TorrentResult>

}