package com.masterwok.tpbsearchandroid.models

data class SearchResultItem(
        val title: String
        , val magnetUri: String
        , val infoHash: String
        , val seeds: Int
        , val leechers: Int
)