package com.masterwok.tpbsearchandroid.contracts

import com.masterwok.tpbsearchandroid.models.PagedResult

const val DefaultRequestTimeout = 10000L

interface QueryService {

    /**
     * Query each host entry using the provided [query] and [pageIndex]. The [query]
     * is the search query, and the [pageIndex] is the result page index.
     */
    suspend fun query(
            query: String
            , pageIndex: Int = 0
            , requestTimeout: Long = DefaultRequestTimeout
    ): List<PagedResult>

}