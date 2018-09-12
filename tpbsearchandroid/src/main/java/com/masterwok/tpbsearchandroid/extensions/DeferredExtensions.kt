@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package com.masterwok.tpbsearchandroid.extensions

import android.util.Log
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.channels.ticker
import kotlinx.coroutines.experimental.selects.whileSelect
import kotlin.system.measureTimeMillis


internal suspend fun <T> List<Deferred<T>>.awaitCount(
        count: Int
        , timeoutMs: Long
): List<T> {
    require(count <= size)

    val toAwait = HashSet(this)
    val results = ArrayList<T>()
    val iterator = toAwait.iterator()
    val ticker = ticker(timeoutMs)

    forEach { deferred ->
        deferred.invokeOnCompletion {
            val hasValue = !deferred.isCompletedExceptionally

            if (hasValue) {
                Log.d("DERP", "(Completed) Value: ${deferred.getCompleted()}")
            } else {
                Log.d("DERP", "(Completed) Exception: $it")
            }
        }
    }

    val elapsedTime = measureTimeMillis {
        whileSelect {
            ticker.onReceive { _ ->
                toAwait.forEach { it.cancel() }
                false
            }

            if (iterator.hasNext()) {
                Log.d("DERP", "WAITING")
                iterator.next().onAwait {
                    iterator.remove()
                    results.add(it)
                    Log.d("DERP", "ADDED RESULT")

                    results.size < count
                }
            }
        }
    }

    Log.d("DERP", "Elapsed time: $elapsedTime")

    return results
}

