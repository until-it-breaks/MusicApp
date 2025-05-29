package com.musicapp.util

import com.musicapp.R
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.util.network.UnresolvedAddressException
import io.ktor.utils.io.errors.IOException
import java.net.UnknownHostException

/**
 * Return a integer representing a string resource id. Null is returned if no match was found.
 */
fun getErrorMessageResId(e: Exception): Int? {
    return when (e) {
        is ConnectTimeoutException -> R.string.connection_timed_out
        is SocketTimeoutException -> R.string.server_took_too_long
        is UnknownHostException, is UnresolvedAddressException -> R.string.could_not_connect
        is IOException -> R.string.network_error_exception
        else -> null
    }
}