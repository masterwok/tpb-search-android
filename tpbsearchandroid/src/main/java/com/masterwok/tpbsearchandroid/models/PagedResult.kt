package com.masterwok.tpbsearchandroid.models


/**
 * A [PagedResult] is a single page of result [items] from a single site.
 */
@Suppress("unused")
data class PagedResult(
        val pageIndex: Int
        , val lastPageIndex: Int
        , val items: List<SearchResultItem>
) {
    val itemCount = items.size

    override fun toString(): String = "Page Index: $pageIndex" +
            ", Last Page Index: $lastPageIndex" +
            ", Item Count: ${items.size}"
}