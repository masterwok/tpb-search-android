package com.masterwok.tpbsearchandroid.paging.common


enum class NetworkState(val value: Int) {
    LOADING(0),
    LOADED(1),
    ERROR(2);

    companion object {
        private val map = NetworkState
                .values()
                .associateBy(NetworkState::value)

        fun getValue(value: Int) = map[value]
    }
}
