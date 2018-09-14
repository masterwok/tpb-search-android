@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package com.masterwok.tpbsearchandroid.common.extensions

import android.util.Log
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.ExecutorCoroutineDispatcher
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.ticker
import kotlinx.coroutines.experimental.selects.whileSelect
import java.util.concurrent.CopyOnWriteArraySet
import kotlin.system.measureTimeMillis


suspend fun <T> List<Deferred<T>>.awaitCount(
        count: Int
        , timeoutMs: Long
): List<T> {
    require(count <= size)
    val Tag = "DERP"

    val toAwait = CopyOnWriteArraySet<Deferred<T>>(this)
    val result = ArrayList<T>()
    val ticket = ticker(timeoutMs)

    val elapsedTime = measureTimeMillis {
        whileSelect {

            ticket.onReceive {
                toAwait.forEach { it.cancel() }
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
    }

    Log.d(Tag, "Elapsed time: $elapsedTime")
    toAwait.forEach { it.cancel() }

    return result
}

