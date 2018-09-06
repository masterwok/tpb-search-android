package com.masterwok.tpbsearchandroid.extensions

import android.content.Context
import android.os.Build
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat
import android.view.inputmethod.InputMethodManager

/**
 * Get the [InputMethodManager] using some [Context].
 */
fun Context.getInputMethodManager(): InputMethodManager {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        return getSystemService(InputMethodManager::class.java)
    }

    return getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
}


/**
 * Get color using [ContextCompat] and the provided [id].
 */
internal fun Context.getCompatColor(@ColorRes id: Int) = ContextCompat.getColor(this, id)
