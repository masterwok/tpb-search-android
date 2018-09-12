package com.masterwok.tpbsearchandroid.extensions

import com.masterwok.tpbsearchandroid.models.QueryResult
import com.masterwok.tpbsearchandroid.models.TorrentResult
import org.jsoup.nodes.Element
import java.util.*

private const val SearchResultPath = "table#searchResult tbody tr"
private const val TitleSelectPath = "td:nth-child(2) > div"
private const val MagnetSelectPath = "td:nth-child(2) > a:nth-child(2)"
private const val SeedersSelectPath = "td:nth-child(3)"
private const val LeechersSelectPath = "td:nth-child(4)"
private const val PageSelectPath = "body > div:nth-child(6) > a"
private const val InfoSelector = "td:nth-child(2) > font"

private val InfoRegex = Regex("""Uploaded\s*([\d\W]*),\s*Size\s*(.*),""")
private val InfoHashRegex = Regex("btih:(.*)&dn")


internal fun Element?.isValidResult(): Boolean = this
        ?.select(SearchResultPath)
        ?.isNotEmpty() == true


internal fun Element?.getQueryResult(pageIndex: Int, url: String): QueryResult<TorrentResult> {
    val items = this?.select(SearchResultPath)
            ?.mapNotNull { it.tryParseSearchResultItem() }
            ?.sortedByDescending { it.seeders }
            ?.distinctBy { it.infoHash }
            ?.toList()
            ?: ArrayList()

    return QueryResult(
            pageIndex = pageIndex
            , lastPageIndex = this?.tryParseLastPageIndex() ?: 0
            , items = items
            , url = url
    )
}

private fun Element.tryParseLastPageIndex(): Int {
    val pageLinks = select(PageSelectPath)
    val pageCount: Int

    val imageLink = pageLinks
            .last()
            ?.select("img")
            ?.firstOrNull()

    // Doesn't have arrow/next image link (last page).
    if (imageLink == null) {
        val last = pageLinks
                .lastOrNull()
                ?.text()
                ?: "0"

        pageCount = Integer.parseInt(last) + 1
    } else {
        // Has arrow/next image link, drop it and get the value of last one.
        val last = pageLinks
                .dropLast(1)
                .lastOrNull()
                ?.text()
                ?: "0"

        pageCount = Integer.parseInt(last)
    }

    // Ensure last page index >= 0
    return Math.max(pageCount - 1, 0)
}

private fun Element.tryParseSearchResultItem(): TorrentResult? {
    try {
        val magnet = select(MagnetSelectPath)
                ?.first()
                ?.attr("href")
                ?: ""

        val infoTextMatch = getInfoTextMatch()

        val title = select(TitleSelectPath)
                ?.first()
                ?.text()
                ?: ""

        val seedersText = select(SeedersSelectPath)
                ?.first()
                ?.text()
                ?: "0"

        val leechersText = select(LeechersSelectPath)
                ?.first()
                ?.text()
                ?: "0"

        return TorrentResult(
                title = title
                , magnet = magnet
                , infoHash = getInfoHash(magnet)
                , seeders = Integer.parseInt(seedersText)
                , leechers = Integer.parseInt(leechersText)
                , displayUploadedOn = getUploadedOn(infoTextMatch)
                , displaySize = getSize(infoTextMatch)

        )
    } catch (ex: Exception) {
        return null
    }
}

private fun Element.getInfoTextMatch(): MatchResult? {
    val infoText = select(InfoSelector)
            ?.text()

    return InfoRegex.find(infoText ?: "")
}

private fun getCurrentYear() = Calendar
        .getInstance()
        .get(Calendar.YEAR)

private fun getUploadedOn(infoTextMatch: MatchResult?): String {
    val text = infoTextMatch
            ?.groupValues
            ?.get(1)
            ?: ""

    return if (text.contains(':')) {
        "${text.substringBefore(' ')}-${getCurrentYear()}"
    } else {
        (text).replace("""[\s]""".toRegex(), "-")
    }
}

private fun getSize(infoTextMatch: MatchResult?): String = infoTextMatch
        ?.groupValues
        ?.get(2)
        ?: ""


private fun getInfoHash(magnet: String): String = InfoHashRegex
        .find(magnet)
        ?.groupValues
        ?.get(1)
        ?: ""
