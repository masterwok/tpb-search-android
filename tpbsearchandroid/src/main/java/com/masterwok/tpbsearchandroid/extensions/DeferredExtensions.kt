@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package com.masterwok.tpbsearchandroid.extensions

import android.util.Log
import com.masterwok.tpbsearchandroid.models.QueryResult
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.TimeoutCancellationException
import kotlinx.coroutines.experimental.channels.produce
import kotlinx.coroutines.experimental.channels.ticker
import kotlinx.coroutines.experimental.selects.select
import kotlinx.coroutines.experimental.selects.whileSelect
import kotlinx.coroutines.experimental.sync.Mutex
import kotlinx.coroutines.experimental.withTimeout
import kotlin.system.measureTimeMillis


//internal suspend fun <T> List<Deferred<T>>.awaitCount(
//        count: Int
//        , timeoutMs: Long
//): List<T> {
//    require(count <= size)
//
//    val toAwait = HashSet(this)
//    val results = ArrayList<T>()
//    val iterator = toAwait.iterator()
//    val ticker = ticker(timeoutMs)
//
//    forEach { deferred ->
//        deferred.invokeOnCompletion {
//            val hasValue = !deferred.isCompletedExceptionally
//
//            if (hasValue) {
//                Log.d("DERP", "(Completed) Value: ${deferred.getCompleted()}")
//            } else {
//                Log.d("DERP", "(Completed) Exception: $it")
//            }
//        }
//    }
//
//    val elapsedTime = measureTimeMillis {
//        whileSelect {
//            ticker.onReceive { _ ->
//                toAwait.forEach { it.cancel() }
//                false
//            }
//
//            if (iterator.hasNext()) {
//                Log.d("DERP", "WAITING")
//                iterator.next().onAwait {
//                    iterator.remove()
//                    results.add(it)
//                    Log.d("DERP", "ADDED RESULT")
//
//                    results.size < count
//                }
//            }
//        }
//    }
//
//    Log.d("DERP", "Elapsed time: $elapsedTime")
//
//    return results
//}
//internal suspend fun <T> List<Deferred<T>>.awaitCount(
//        count: Int
//        , timeoutMs: Long
//): List<T> {
//    require(count <= size)
//
//    val toAwait = HashSet(this)
//    val results = ArrayList<T>()
//    val ticker = ticker(timeoutMs)
//    var completedCount = 0
//
//    forEach { deferred ->
//        deferred.invokeOnCompletion {
//            completedCount++
//
//            val hasValue = !deferred.isCompletedExceptionally
//
//            if (hasValue) {
//                Log.d("DERP", "(Completed|$completedCount/$size) Value: ${deferred.getCompleted()}")
//            } else {
//                Log.d("DERP", "(Completed|$completedCount/$size) Exception: $it")
//            }
//        }
//    }
//
//    val processed = HashSet<Deferred<T>>()
//
//    val elapsedTime = measureTimeMillis {
//        whileSelect {
//            ticker.onReceive { _ ->
//                Log.d("DERP", "TIMED OUT YO")
//                toAwait.forEach { it.cancel() }
//                false
//            }
//
//            minus(processed).forEach { deferred ->
//                produce {
//                    processed.add(deferred)
//                    send(deferred.await())
//                }.onReceive { result ->
//                    results.add(result)
//                    results.size < count
//                }
//            }
//        }
//    }
//
//    Log.d("DERP", "Elapsed time: $elapsedTime, Result Size: ${results.size}")
//    toAwait.forEach { it.cancel() }
//
//    return results
//}

internal suspend fun <T> List<Deferred<T>>.awaitCount(
        count: Int
        , timeoutMs: Long
): List<T> {
    require(count <= size)

    val toAwait = HashSet(this)
    val results = ArrayList<T>()
    val ticker = ticker(timeoutMs)
    var completedCount = 0

    forEach { deferred ->
        deferred.invokeOnCompletion {
            completedCount++

            val hasValue = !deferred.isCompletedExceptionally

            if (hasValue) {
                Log.d("DERP", "(Completed|$completedCount/$size) Value: ${deferred.getCompleted()}")
            } else {
                Log.d("DERP", "(Completed|$completedCount/$size) Exception: $it")
            }
        }
    }

    var startedCount = 0

    val elapsedTime = measureTimeMillis {
        val iterator = iterator()

        while (iterator.hasNext()) {
            val result = select<Boolean> {
                ticker.onReceive { _ ->
                    Log.d("DERP", "TIMED OUT YO")
                    toAwait.forEach { it.cancel() }
                    false
                }

                iterator.next().onAwait {
                    Log.d("DERP", "Starting: ${++startedCount}")
                    results.add(it)

                    results.size < count
                }

            }

            if (!result) {
                toAwait.forEach { it.cancel() }
                break
            }
        }
    }

    Log.d("DERP", "Elapsed time: $elapsedTime, Result Size: ${results.size}")

    return results
}
