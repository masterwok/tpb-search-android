package com.masterwok.tpbsearchandroid.constants

import java.net.URLEncoder

private const val QueryEncoding = "UTF-8"

/**
 * Create a query using absolute path style.
 */
private fun pathQuery(
        host: String
        , query: String
        , pageIndex: Int
): String {
    val encodedQuery = URLEncoder.encode(query, QueryEncoding)

    return "$host/search/$encodedQuery/$pageIndex/7"
}


/**
 * Create a query using the query string style.
 */
private fun queryStringQuery(
        host: String
        , query: String
        , pageIndex: Int
): String {
    val encodedQuery = URLEncoder.encode(query, QueryEncoding)

    return "$host/s/?q=$encodedQuery&page=$pageIndex&orderby=99"
}


/**
 * A [List] of factories for creating a query string URL. Clones have different ways of
 * creating query URLs. Luckily it seems like there are only a few at the moment. As more
 * are added, query factories can be added and adjusted for each of the defined hosts.
 */
@JvmField
val QueryFactories: List<(query: String, pageIndex: Int) -> String> = listOf(
        { query, pageIndex -> queryStringQuery("https://superbay.in", query, pageIndex) }
        , { query, pageIndex -> queryStringQuery("https://superbay.in", query, pageIndex) }
        , { query, pageIndex -> queryStringQuery("https://piratebays.be", query, pageIndex) }
        , { query, pageIndex -> queryStringQuery("https://piratebays.fi", query, pageIndex) }
        , { query, pageIndex -> queryStringQuery("https://indiapirate.com", query, pageIndex) }
        , { query, pageIndex -> queryStringQuery("https://pirate.tel", query, pageIndex) }
        , { query, pageIndex -> queryStringQuery("https://piratebay.nz", query, pageIndex) }
        , { query, pageIndex -> queryStringQuery("https://piratebay6.org", query, pageIndex) }
        , { query, pageIndex -> queryStringQuery("https://uktpb.net", query, pageIndex) }
        , { query, pageIndex -> queryStringQuery("https://thepirateproxy.in", query, pageIndex) }
        , { query, pageIndex -> queryStringQuery("https://piratesbay.fi", query, pageIndex) }
        , { query, pageIndex -> queryStringQuery("https://proxyproxyproxy.net", query, pageIndex) }
        , { query, pageIndex -> queryStringQuery("https://piratepirate.in", query, pageIndex) }
        , { query, pageIndex -> queryStringQuery("https://fastpirate.link", query, pageIndex) }
        , { query, pageIndex -> queryStringQuery("https://thepiratebay.click", query, pageIndex) }
        , { query, pageIndex -> queryStringQuery("https://gameofbay.eu", query, pageIndex) }
        , { query, pageIndex -> queryStringQuery("https://freepirate.eu", query, pageIndex) }
        , { query, pageIndex -> queryStringQuery("https://freepirate.org", query, pageIndex) }
        , { query, pageIndex -> queryStringQuery("https://pirateproxy.fi", query, pageIndex) }
        , { query, pageIndex -> queryStringQuery("https://proxybayproxy.net", query, pageIndex) }
        , { query, pageIndex -> queryStringQuery("https://tpbproxy.fi", query, pageIndex) }
        , { query, pageIndex -> queryStringQuery("https://freeproxy.click", query, pageIndex) }
        , { query, pageIndex -> queryStringQuery("https://piratepirate.net", query, pageIndex) }
        , { query, pageIndex -> queryStringQuery("https://piratebays.top", query, pageIndex) }
        , { query, pageIndex -> queryStringQuery("https://proxyproxy.fi", query, pageIndex) }
        , { query, pageIndex -> queryStringQuery("https://piratemirror.org", query, pageIndex) }
        , { query, pageIndex -> queryStringQuery("https://piratebaypirate.net", query, pageIndex) }
        , { query, pageIndex -> queryStringQuery("https://tpbproxy.click", query, pageIndex) }
        , { query, pageIndex -> queryStringQuery("https://thepirateproxy.click", query, pageIndex) }
        , { query, pageIndex -> queryStringQuery("https://thepirateway.click", query, pageIndex) }
        , { query, pageIndex -> queryStringQuery("https://proxybay.blue", query, pageIndex) }
        , { query, pageIndex -> queryStringQuery("https://piratepiratepirate.org", query, pageIndex) }
        , { query, pageIndex -> queryStringQuery("https://unblocktpb.org", query, pageIndex) }
        , { query, pageIndex -> queryStringQuery("https://tpbunblock.net", query, pageIndex) }
        , { query, pageIndex -> queryStringQuery("https://ukpirateproxy.com", query, pageIndex) }
        , { query, pageIndex -> queryStringQuery("https://thepiratebayproxy.net", query, pageIndex) }
        , { query, pageIndex -> queryStringQuery("https://tpbproxy.in", query, pageIndex) }
        , { query, pageIndex -> queryStringQuery("https://thepiratebayproxy.in", query, pageIndex) }
        , { query, pageIndex -> queryStringQuery("https://tpb.review", query, pageIndex) }
        , { query, pageIndex -> queryStringQuery("https://Piratebayproxy.in", query, pageIndex) }
        , { query, pageIndex -> queryStringQuery("https://proxybay.life", query, pageIndex) }
        , { query, pageIndex -> queryStringQuery("https://tpb.fun", query, pageIndex) }
        , { query, pageIndex -> queryStringQuery("https://thepiratebayproxy.one", query, pageIndex) }
        , { query, pageIndex -> queryStringQuery("https://pirateproxy.bid", query, pageIndex) }
        , { query, pageIndex -> queryStringQuery("https://pirateproxy.men", query, pageIndex) }
        , { query, pageIndex -> queryStringQuery("https://thepiratebay-org.prox.space", query, pageIndex) }
        , { query, pageIndex -> pathQuery("https://priatebays.fi", query, pageIndex) }
        , { query, pageIndex -> pathQuery("https://pirateproxy.sh", query, pageIndex) }
        , { query, pageIndex -> pathQuery("https://thepiratebay.red", query, pageIndex) }
        , { query, pageIndex -> pathQuery("https://tpbmirror.org", query, pageIndex) }
)


