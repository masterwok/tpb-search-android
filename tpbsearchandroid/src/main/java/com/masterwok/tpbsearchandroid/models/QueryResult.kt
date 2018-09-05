package com.masterwok.tpbsearchandroid.models

data class QueryResult<T>(
        var state: State = State.SUCCESS
        , var pageIndex: Int = 0
        , var lastPageIndex: Int = 0
        , var items: List<T> = ArrayList()
        , var errorMessage: String? = null
) {
    enum class State {
        SUCCESS,
        ERROR,
        TIMEOUT
    }

    fun getItemCount(): Int = items.size

    override fun toString(): String = "State: $state, " +
            "Page: $pageIndex/$lastPageIndex, " +
            "Item Count: ${items.size}"
}

