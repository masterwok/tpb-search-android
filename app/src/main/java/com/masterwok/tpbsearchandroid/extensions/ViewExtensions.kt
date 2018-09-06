package com.masterwok.tpbsearchandroid.extensions

import android.view.View

/**
 * Dismiss soft input (keyboard) from the window using a [View] context.
 */
fun View.dismissKeyboard() = context
        .getInputMethodManager()
        .hideSoftInputFromWindow(
                windowToken
                , 0
        )
