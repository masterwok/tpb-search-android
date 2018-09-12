package com.masterwok.bitcast.paging.common


/**
 * This enumeration represents the current state of some network request.
 *
 */
enum class NetworkState(val value: Int) {

    /**
     * Request is in progress.
     */
    LOADING(0),

    /**
     * Request has completed successfully.
     */
    LOADED(1),

    /**
     * Request resulted in an error.
     */
    ERROR(2);

    companion object {
        private val map = NetworkState
                .values()
                .associateBy(NetworkState::value)

        /**
         * Convert an [Int] [value] to a [NetworkState].
         */
        fun getValue(value: Int) = map[value]
    }
}
