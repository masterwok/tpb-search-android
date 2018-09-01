package com.masterwok.tpbsearchandroid.models

data class SearchResultItem(
        val title: String
        , val magnet: String
        , val infoHash: String
        , val seeders: Int
        , val leechers: Int
)