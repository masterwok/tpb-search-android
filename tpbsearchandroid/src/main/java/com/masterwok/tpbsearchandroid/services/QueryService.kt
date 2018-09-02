package com.masterwok.tpbsearchandroid.services

import android.util.Log
import com.masterwok.tpbsearchandroid.contracts.QueryService
import com.masterwok.tpbsearchandroid.models.PagedResult
import com.masterwok.tpbsearchandroid.models.SearchResultItem
import kotlinx.coroutines.experimental.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.util.concurrent.TimeUnit

class QueryService constructor(
        private val queryFactories: List<(query: String, pageIndex: Int) -> String>
) : QueryService {

    companion object {
        private const val Tag = "QueryService"

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

    private fun List<PagedResult>.flatten(
            pageIndex: Int
    ): PagedResult = PagedResult(
            pageIndex = pageIndex
            , lastPageIndex = maxBy { it.lastPageIndex }?.lastPageIndex ?: 0
            , items = flatMap { it.items }.distinctBy { it.infoHash }
    )

    private suspend fun producePagedResults(
            queryFactories: List<(query: String, pageIndex: Int) -> String>
            , query: String
            , pageIndex: Int
            , maxSuccessfulHosts: Int
            , queryTimeout: Long
            , requestTimeout: Long
    ): ArrayList<PagedResult> {
        val results = ArrayList<PagedResult>()
        val rootJob = Job()

        try {
            withTimeout(queryTimeout, TimeUnit.MILLISECONDS) {
                queryFactories.map {
                    async(parent = rootJob) {
                        val pagedResult = queryHost(
                                it
                                , query
                                , pageIndex
                                , requestTimeout
                        )

                        // A page should always have some results.
                        if (pagedResult.itemCount > 0
                                // Guard heuristic, we know the last page index should be greater than
                                // or equal to the requested page index. The proxy doesn't have the page
                                // if this check fails.
                                && pagedResult.lastPageIndex >= pageIndex) {
                            results.add(pagedResult)
                        }

                        if (results.size == maxSuccessfulHosts) {
                            rootJob.cancelAndJoin()
                            return@async
                        }
                    }
                }.awaitAll()
            }
        } catch (ex: TimeoutCancellationException) {
            Log.w(Tag, "Query timed out, successful queries: ${results.size}")
        } catch (ignored: JobCancellationException) {
            // Ignored
        } catch (ex: Exception) {
            Log.w(Tag, "Unexpected exception", ex)
        }

        return results
    }

    override suspend fun query(
            query: String
            , pageIndex: Int
            , queryTimeout: Long
            , requestTimeout: Long
            , maxSuccessfulHosts: Int
    ): PagedResult {
        val results: ArrayList<PagedResult> = ArrayList()

        try {
            results.addAll(producePagedResults(
                    queryFactories = queryFactories
                    , query = query
                    , pageIndex = pageIndex
                    , maxSuccessfulHosts = Math.min(queryFactories.size, maxSuccessfulHosts)
                    , queryTimeout = queryTimeout
                    , requestTimeout = requestTimeout
            ))
        } catch (ex: Exception) {
            Log.e(Tag, "Unknown error occurred: ${ex.message}")
        } finally {
            return results.flatten(pageIndex)
        }
    }

    private suspend fun queryHost(
            queryFactory: (query: String, pageIndex: Int) -> String
            , query: String
            , pageIndex: Int
            , requestTimeout: Long
    ): PagedResult {
        val requestUrl = queryFactory(query, pageIndex)
        var response: Document? = null

        try {
            withTimeout(requestTimeout, TimeUnit.MILLISECONDS) {
                response = makeRequest(requestUrl).await()
            }
        } catch (ex: TimeoutCancellationException) {
            Log.w(Tag, "Request timeout: $requestUrl")
        } catch (ex: JobCancellationException) {
            // Ignored..
        } catch (ex: Exception) {
            Log.w(Tag, "Request failed: $requestUrl")
        }

        return PagedResult(
                pageIndex = pageIndex
                , lastPageIndex = response?.tryParseLastPageIndex() ?: 0
                , items = response?.select(SearchResultPath)
                ?.mapNotNull { it.tryParseSearchResultItem() }
                ?.sortedByDescending { it.seeders }
                ?.distinctBy { it.infoHash }
                ?.toList()
                ?: ArrayList()
        )
    }

    private fun Element.tryParseLastPageIndex(): Int {
        try {
            val pageLinks = select(PageSelectPath)

            val imageLink = pageLinks.last().select("img").firstOrNull()

            if (imageLink == null) {
                val last = pageLinks.lastOrNull()?.text() ?: "0"
                val pageCount = Integer.parseInt(last) + 1

                return Math.max(pageCount - 1, 0)
            }

            val pageCount = Integer.parseInt(pageLinks
                    ?.dropLast(1)
                    ?.last()
                    ?.text() ?: "0"
            )

            return Math.max(pageCount - 1, 0)
        } catch (ex: Exception) {
            return 0
        }
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
            return null
        }
    }

    private fun getInfoHash(magnet: String): String = InfoHashRegex
            .find(magnet)
            ?.groupValues
            ?.get(1)
            ?: ""

}