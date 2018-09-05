package com.masterwok.tpbsearchandroid.models

data class QueryResult<T>(
        val state: State
        , val pagedResult: PagedResult<T> = PagedResult()
) {
    enum class State {
        SUCCESS,
        ERROR,
        TIMEOUT
    }
}

