package com.masterwok.tpbsearchandroid.models

data class QueryResult<T>(
        var state: State = State.PENDING
        , var pageIndex: Int = 0
        , var lastPageIndex: Int = 0
        , var items: List<T> = ArrayList()
        , var url: String? = null
) {
    enum class State {
        PENDING,
        SUCCESS,
        INVALID,
        ERROR
    }


    fun isSuccessful(): Boolean = state == State.SUCCESS

    fun getItemCount(): Int = items.size

    override fun toString(): String = "State: $state" +
            ", Page: $pageIndex/$lastPageIndex" +
            ", Item Count: ${items.size}" +
            ", Url: $url"
}

