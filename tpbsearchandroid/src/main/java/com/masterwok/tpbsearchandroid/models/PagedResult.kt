package com.masterwok.tpbsearchandroid.models

@Suppress("unused")
data class PagedResult(
        val requestUrl: String
        , val pageIndex: Int
        , val lastPageIndex: Int
        , val results: List<SearchResultItem>
) {
    val itemCount = results.size

    override fun toString(): String = "Request Url: $requestUrl" +
            ", Page Index: $pageIndex" +
            ", Last Page Index: $lastPageIndex" +
            ", Item Count: ${results.size}"
}