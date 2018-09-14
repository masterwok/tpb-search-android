@file:Suppress("EXPERIMENTAL_FEATURE_WARNING", "unused")

package com.masterwok.tpbsearchandroid.common.extensions

import android.util.Log
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.channels.ticker
import kotlinx.coroutines.experimental.selects.whileSelect
import java.util.concurrent.CopyOnWriteArraySet
import kotlin.system.measureTimeMillis


/**
 * Await the provided [count] of [Deferred] instances to complete before
 * some [timeoutMs] occurs. Results will include unsuccessful items should
 * [keepUnsuccessful] be set to true. Results are considered successful
 * if they pass the provided [predicate].
 */
suspend fun <T> List<Deferred<T>>.awaitCount(
        count: Int
        , timeoutMs: Long
        , keepUnsuccessful: Boolean
        , predicate: (T) -> Boolean
): List<T> {
    require(count <= size)

    val toAwait = CopyOnWriteArraySet<Deferred<T>>(this)
    val result = ArrayList<T>()
    val ticket = ticker(timeoutMs)
    var successCount = 0

    whileSelect {
        ticket.onReceive {
            toAwait.forEach { deferred -> deferred.cancel() }
            false
        }

        toAwait.forEach { deferred ->
            deferred.onAwait {
                toAwait.remove(deferred)

                if (predicate(it)) {
                    result.add(it)
                    successCount++
                } else if (keepUnsuccessful) {
                    result.add(it)
                }

                successCount < count
            }
        }
    }

    toAwait.forEach { deferred -> deferred.cancel() }

    return result
}

/**
 * Await the provided [count] of [Deferred] instances to complete before
 * some [timeoutMs] occurs.
 */
suspend fun <T> List<Deferred<T>>.awaitCount(
        count: Int
        , timeoutMs: Long
): List<T> {
    require(count <= size)

    val toAwait = CopyOnWriteArraySet<Deferred<T>>(this)
    val result = ArrayList<T>()
    val ticket = ticker(timeoutMs)

    whileSelect {
        ticket.onReceive {
            toAwait.forEach { deferred -> deferred.cancel() }
            false
        }

        toAwait.forEach { deferred ->
            deferred.onAwait {
                toAwait.remove(deferred)
                result.add(it)

                result.size < count
            }
        }
    }

    toAwait.forEach { deferred -> deferred.cancel() }

    return result
}

