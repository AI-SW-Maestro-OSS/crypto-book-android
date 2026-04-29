package io.soma.cryptobook.core.network.error

import io.soma.cryptobook.core.domain.error.HttpResponseStatus

sealed interface ApiError {
    data object Network : ApiError

    data class Http(
        val status: HttpResponseStatus,
        val rawCode: Int,
        val errorRequestUrl: String,
        val message: String?,
    ) : ApiError

    data class UnexpectedBody(
        val rawCode: Int,
        val errorRequestUrl: String,
        val message: String?,
    ) : ApiError

    data class Unknown(
        val message: String?,
    ) : ApiError
}
