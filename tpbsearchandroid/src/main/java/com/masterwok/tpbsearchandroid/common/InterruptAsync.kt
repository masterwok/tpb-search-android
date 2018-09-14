@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package com.masterwok.tpbsearchandroid.common

import kotlinx.coroutines.experimental.*

internal fun <T> interruptAsync(
        executorCoroutineDispatcher: ExecutorCoroutineDispatcher
        , start: CoroutineStart = CoroutineStart.DEFAULT
        , parent: Job? = null
        , onCompletion: CompletionHandler? = null
        , block: suspend CoroutineScope.() -> T
): Deferred<T?> {
    var thread: Thread? = null

    val deferred = async(
            context = executorCoroutineDispatcher
            , start = start
            , parent = parent
            , onCompletion = onCompletion
    ) {
        thread = Thread.currentThread()

        return@async block()
    }

    deferred.invokeOnCompletion(true, true) {
        if (deferred.isCancelled
                && thread != null
                && thread?.isAlive == true
        ) {
            thread?.interrupt()
            thread = null
        }
    }

    return deferred
}

