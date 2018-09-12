@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package com.masterwok.tpbsearchandroid.extensions

import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.channels.ticker
import kotlinx.coroutines.experimental.selects.whileSelect


internal suspend fun <T> List<Deferred<T>>.awaitCount(
        count: Int
        , timeoutMs: Long
): List<T> {
    require(count <= size)

    val toAwait = HashSet(this)
    val results = ArrayList<T>()
    val ticker = ticker(timeoutMs)

    whileSelect {
        toAwait.forEach { deferred ->
            deferred.onAwait {
                toAwait.remove(deferred)
                results.add(it)

                results.size <= count
            }
        }

        ticker.onReceive {
            toAwait.forEach { deferred ->
                deferred.cancel()
            }

            false
        }
    }

    return results
}
