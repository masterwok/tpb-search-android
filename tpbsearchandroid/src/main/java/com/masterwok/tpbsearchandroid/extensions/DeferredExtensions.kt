@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package com.masterwok.tpbsearchandroid.extensions

import android.util.Log
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.channels.ticker
import kotlinx.coroutines.experimental.selects.select
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

    val derp = measureTimeMillis {
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

    val x = 1

    return results
}
//internal suspend fun <T> List<Deferred<T>>.awaitCount(
//        count: Int
//        , timeoutMs: Long
//): List<T> {
//    require(count <= size)
//
//    val toAwait = HashSet(this)
//    val results = ArrayList<T>()
//    val ticker = ticker(timeoutMs)
//
//    val processed = HashSet<Deferred<T>>()
//
//    this.forEach {
//        it.invokeOnCompletion {
//            Log.d("DERP", "Completed: $it")
//        }
//    }
//
//    val derp = measureTimeMillis {
//        whileSelect {
//            ticker.onReceive { _ ->
//                toAwait.forEach { it.cancel() }
//                false
//            }
//
//            toAwait.minus(processed).forEachIndexed { index, deferred ->
//
//                deferred.onAwait {
//                    processed.add(deferred)
//                    results.add(it)
//
//                    results.size < count
//                }
//            }
//        }
//    }
//
//    toAwait.forEach { it.cancel(TimeoutException()) }
//
//    val x = 1
//
//    return results
//}
