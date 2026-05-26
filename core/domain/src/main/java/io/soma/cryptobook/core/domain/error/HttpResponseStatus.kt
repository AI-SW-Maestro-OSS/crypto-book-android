package io.soma.cryptobook.core.domain.error

enum class HttpResponseStatus(val code: Int, val msg: String) {
    // Success
    Ok(200, "Ok"),

    // Client Errors
    BadRequest(400, "Bad Request"),
    Forbidden(403, "Forbidden"),
    NotFound(404, "Not Found"),
    ClientTimeout(408, "Client Timeout"),
    IpBanned(418, "IP Banned"),
    RateLimitExceeded(429, "Rate Limit Exceeded"),

    // Server Errors
    InternalError(500, "Internal Error"),
    Unavailable(503, "Service Unavailable"),

    // Fallback
    Unknown(-1, "Unknown"),
    ;

    companion object {
        fun create(code: Int): HttpResponseStatus =
            entries.firstOrNull { it.code == code } ?: Unknown
    }

    override fun toString(): String = "$code $msg"
}
