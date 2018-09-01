package com.masterwok.tpbsearchandroid.models

/**
 * A single result item. This represents a single row from the search
 * results on the pirate bay.
 */
data class SearchResultItem(
        val title: String
        , val magnet: String
        , val infoHash: String
        , val seeders: Int
        , val leechers: Int
)