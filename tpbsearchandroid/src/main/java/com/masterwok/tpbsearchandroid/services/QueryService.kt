package com.masterwok.tpbsearchandroid.services

import android.util.Log
import com.masterwok.tpbsearchandroid.constants.Config
import com.masterwok.tpbsearchandroid.contracts.QueryService
import com.masterwok.tpbsearchandroid.models.SearchResultItem
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.withTimeout
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

class QueryService constructor(
        private val hosts: List<String>
) : QueryService {

    companion object {
        private const val Tag = "QueryService"
        private const val UserAgent = "Mozilla/5.0"
        private const val RequestTimeout = 5000L

        private val TorrentItemRegex = Regex("""<a[\s\S]*?(?=detLink)[\s\S]*?(?=>)>(.*)</a>[\s\S]*?(?=magnet:)(.*)</a>[\s\S]*?(?=>\d*</td>)>(\d*)</td>[\s\S]*?(?=>\d*</td>)>(\d*)</td>""")
    }

    private fun makeRequest(url: String) = async {
        val urlConnection = URL(url).openConnection() as HttpURLConnection

        urlConnection.setRequestProperty("User-Agent", UserAgent)

        val response = urlConnection
                .inputStream
                .bufferedReader()
                .use { it.readText() }

        urlConnection.disconnect()

        response
    }

    private fun buildUrl(
            host: String
            , query: String
            , pageIndex: Int
    ): String = "$host/search/$query/$pageIndex/7"

    private suspend fun queryHost(
            host: String
            , query: String
            , pageIndex: Int
    ): List<SearchResultItem> {
        val url = buildUrl(host, query, pageIndex)
        var response = ""

        try {
            withTimeout(RequestTimeout, TimeUnit.MILLISECONDS) {
                response = makeRequest(url).await()
            }
        } catch (ex: Exception) {
            Log.w(Tag, "Request timeout: $url")
        }

        return TorrentItemRegex
                .findAll(response)
                .map { it.toSearchResultItem() }
                .toList()
    }

    private fun MatchResult.toSearchResultItem() = SearchResultItem(
            title = groupValues[1]
            , magnetUri = groupValues[2]
            , infoHash = getInfoHash(groupValues[2])
            , seeds = Integer.parseInt(groupValues[3])
            , leechers = Integer.parseInt(groupValues[4])
    )

    private fun getInfoHash(magnet: String): String = Regex("""btih:(.*)&dn""")
            .find(magnet)
            ?.groupValues
            ?.get(1)
            ?: ""


    suspend fun query(query: String): List<SearchResultItem> {
        val result = Config
                .Hosts
                .map { async { queryHost(it, query, 0) } }
                .flatMap { it.await() }
                .sortedByDescending { it.seeds }
                .distinctBy { it.infoHash }

        return result
    }

}