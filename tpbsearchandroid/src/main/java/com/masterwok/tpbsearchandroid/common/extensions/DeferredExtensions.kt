@file:Suppress("EXPERIMENTAL_FEATURE_WARNING", "unused")

package com.masterwok.tpbsearchandroid.common.extensions

import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.channels.ticker
import kotlinx.coroutines.experimental.selects.whileSelect
import java.util.concurrent.CopyOnWriteArraySet


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
    val ticker = ticker(timeoutMs)
    var successCount = 0

    whileSelect {
        ticker.onReceive {
            toAwait.forEach { deferred -> deferred.cancel() }
            false
        }

        toAwait.forEach { deferred ->
            deferred.onAwait {
                toAwait.remove(deferred)

                // Break out of the whileSelect if all deferred instances are cancelled or completed.
                if (toAwait.all { deferred -> deferred.isCancelled || deferred.isCompleted }) {
                    return@onAwait false
                }

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

    // Ensure all deferred instances are cancelled should the success count be reached before
    // the timeout occurs.
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
    val ticker = ticker(timeoutMs)

    whileSelect {
        ticker.onReceive {
            toAwait.forEach { deferred -> deferred.cancel() }
            false
        }

        toAwait.forEach { deferred ->
            deferred.onAwait {
                toAwait.remove(deferred)

                // Break out of the whileSelect if all deferred instances are cancelled or completed.
                if (toAwait.all { deferred -> deferred.isCancelled || deferred.isCompleted }) {
                    return@onAwait false
                }

                result.add(it)

                result.size < count
            }
        }
    }

    // Ensure all deferred instances are cancelled should the success count be reached before
    // the timeout occurs.
    toAwait.forEach { deferred -> deferred.cancel() }

    return result
}

