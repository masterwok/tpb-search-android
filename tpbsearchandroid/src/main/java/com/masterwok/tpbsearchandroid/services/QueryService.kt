package com.masterwok.tpbsearchandroid.services

import android.util.Log
import com.masterwok.tpbsearchandroid.contracts.QueryService
import com.masterwok.tpbsearchandroid.models.PagedResult
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

        private const val DefaultRequestTimeout = 10000L

        private const val SearchResultPath = "table#searchResult tbody tr"
        private const val TitleSelectPath = "td:nth-child(2) > div"
        private const val MagnetSelectPath = "td:nth-child(2) > a:nth-child(2)"
        private const val SeedersSelectPath = "td:nth-child(3)"
        private const val LeechersSelectPath = "td:nth-child(4)"
        private const val PageSelectPath = "body > div:nth-child(6) > a"

        private val InfoHashRegex = Regex("btih:(.*)&dn")
    }

    private fun makeRequest(url: String) = async<Document> {
        Jsoup.connect(url).get()
    }

    suspend fun query(
            query: String
            , requestTimeout: Long = DefaultRequestTimeout
    ): List<PagedResult> {
        return hosts
                .map { async { queryHost(it, query, 0, requestTimeout) } }
                .map { it.await() }

//        val job = async { queryHost(hosts.first(), query, 0, requestTimeout) }
//
//        return arrayListOf(job.await())
    }

    private suspend fun queryHost(
            host: String
            , query: String
            , pageIndex: Int
            , requestTimeout: Long
    ): PagedResult {
        val requestUrl = "$host/search/$query/$pageIndex/7"
        var response: Document? = null

        try {
            withTimeout(requestTimeout, TimeUnit.MILLISECONDS) {
                response = makeRequest(requestUrl).await()
            }
        } catch (ex: TimeoutCancellationException) {
            Log.w(Tag, "Request timeout: $requestUrl")
        } catch (ex: Exception) {
            Log.w(Tag, "Request failed: $requestUrl")
        }

        return PagedResult(
                requestUrl
                , pageIndex = pageIndex
                , lastPageIndex = response?.tryParseLastPageIndex() ?: 0
                , results = response?.select(SearchResultPath)
                ?.mapNotNull { it.tryParseSearchResultItem() }
                ?.sortedByDescending { it.seeders }
                ?.distinctBy { it.infoHash }
                ?.toList()
                ?: ArrayList()
        )
    }

    private fun Element.tryParseLastPageIndex(): Int = try {
        val pageCount = Integer.parseInt(select(PageSelectPath)
                ?.dropLast(1)
                ?.last()
                ?.text() ?: "0"
        )

        Math.max(pageCount - 1, 0)
    } catch (ex: Exception) {
        0
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

}