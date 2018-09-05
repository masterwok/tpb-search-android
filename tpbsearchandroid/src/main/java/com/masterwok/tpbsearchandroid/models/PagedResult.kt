package com.masterwok.tpbsearchandroid.models


/**
 * A [PagedResult] is a single page of result [items].
 */
@Suppress("unused")
data class PagedResult<T>(
        val pageIndex: Int
        , val lastPageIndex: Int
        , val items: List<T>
) {
    constructor(): this(0, 0, ArrayList<T>())

    val itemCount = items.size

    override fun toString(): String = "Page Index: $pageIndex" +
            ", Item Count: ${items.size}" +
            ", Last Page Index: $lastPageIndex"
}