package com.masterwok.tpbsearchandroid.services

import android.util.Log
import com.masterwok.tpbsearchandroid.contracts.QueryService
import com.masterwok.tpbsearchandroid.models.SearchResultItem
import kotlinx.coroutines.experimental.TimeoutCancellationException
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.withTimeout
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.util.concurrent.TimeUnit

class QueryService constructor(
        private val hosts: List<String>
) : QueryService {

    companion object {
        private const val Tag = "QueryService"

        private const val RequestTimeout = 5000L

        private const val TitleSelectPath = "td:nth-child(2) > div"
        private const val MagnetSelectPath = "td:nth-child(2) > a:nth-child(2)"
        private const val SeedersSelectPath = "td:nth-child(3)"
        private const val LeechersSelectPath = "td:nth-child(4)"

        private val InfoHashRegex = Regex("btih:(.*)&dn")
    }

    private fun makeRequest(url: String) = async<Document> {
        Jsoup.connect(url).get()
    }

    private suspend fun queryHost(
            host: String
            , query: String
            , pageIndex: Int
    ): List<SearchResultItem> {
        val url = "$host/search/$query/$pageIndex/7"
        var response: Document? = null

        try {
            withTimeout(RequestTimeout, TimeUnit.MILLISECONDS) {
                response = makeRequest(url).await()
            }
        } catch (ex: TimeoutCancellationException) {
            Log.w(Tag, "Request timeout: $url")
        } catch (ex: Exception) {
            Log.w(Tag, "Request failed: $url")
        }

        val searchResultTableRows = response?.select("table#searchResult tbody tr")

        return searchResultTableRows
                ?.mapNotNull { it.tryParseSearchResultItem() }
                ?.toList()
                ?: ArrayList()
    }

    private fun Element.tryParseSearchResultItem(): SearchResultItem? {
        try {
            val magnet = select(MagnetSelectPath)
                    ?.first()
                    ?.attr("href")
                    ?: ""

            return SearchResultItem(
                    title = select(TitleSelectPath)?.first()?.text() ?: ""
                    , magnet = magnet
                    , infoHash = getInfoHash(magnet)
                    , seeders = Integer.parseInt(select(SeedersSelectPath)?.first()?.text() ?: "0")
                    , leechers = Integer.parseInt(select(LeechersSelectPath)?.first()?.text()
                    ?: "0")

            )

        } catch (ex: Exception) {
            Log.w(Tag, "Failed to parse result: ${ex.message}")
        }

        return null
    }

    private fun getInfoHash(magnet: String): String = InfoHashRegex
            .find(magnet)
            ?.groupValues
            ?.get(1)
            ?: ""


    suspend fun query(query: String): List<SearchResultItem> {
        return hosts
                .map { async { queryHost(it, query, 0) } }
                .flatMap { it.await() }
                .sortedByDescending { it.seeders }
                .distinctBy { it.infoHash }
//        async { queryHost(Config.Hosts.first(), query, 0) }
//
//        return ArrayList()
    }

}