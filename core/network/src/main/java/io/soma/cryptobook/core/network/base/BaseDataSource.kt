package io.soma.cryptobook.core.network.base

import io.soma.cryptobook.core.domain.error.HttpResponseException
import io.soma.cryptobook.core.domain.error.HttpResponseStatus
import io.soma.cryptobook.core.domain.outcome.Outcome
import io.soma.cryptobook.core.network.error.ApiError
import retrofit2.Response
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

abstract class BaseDataSource {
    protected suspend fun <T> executeApiCall(
        block: suspend () -> Response<T>,
    ): Outcome<T, ApiError> {
        return try {
            responseToOutcome(block())
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: IOException) {
            Outcome.Failure(
                error = ApiError.Network,
                cause = exception,
            )
        } catch (exception: Exception) {
            Outcome.Failure(
                error = ApiError.Unknown(exception.message),
                cause = exception,
            )
        }
    }

    protected fun <T> checkResponse(response: Response<T>): T {
        if (response.isSuccessful) {
            val body = response.body()

            // Body가 null인 경우 HTTP 상태 코드로 판단
            if (body == null) {
                return when (response.code()) {
                    204, 205 -> {
                        // HTTP 204 No Content, 205 Reset Content는 body 없음이 정상
                        @Suppress("UNCHECKED_CAST")
                        Unit as T
                    }

                    else -> {
                        // 200~203은 body가 있어야 정상 - null이면 서버 오류
                        throw HttpResponseException(
                            status = HttpResponseStatus.InternalError,
                            rawCode = response.code(),
                            errorRequestUrl = response.raw().request.url.toString(),
                            msgForLogging = "Success ${response.code()} but unexpected null body",
                            cause =
                            Throwable(
                                "Body should not be null for status ${response.code()}",
                            ),
                        )
                    }
                }
            }

            return body
        } else {
            val errorBody = response.errorBody()?.string()
            throw HttpResponseException(
                status = HttpResponseStatus.create(response.code()),
                rawCode = response.code(),
                errorRequestUrl = response.raw().request.url.toString(),
                msgForLogging = "Http request failed: ${response.code()} " +
                    "${response.message()}, $errorBody",
                cause = Throwable(errorBody),
            )
        }
    }

    private fun <T> responseToOutcome(response: Response<T>): Outcome<T, ApiError> {
        if (response.isSuccessful) {
            val body = response.body() ?: return when (response.code()) {
                204, 205 -> {
                    @Suppress("UNCHECKED_CAST")
                    Outcome.success(Unit as T)
                }

                else -> Outcome.failure(
                    ApiError.UnexpectedBody(
                        rawCode = response.code(),
                        errorRequestUrl = response.raw().request.url.toString(),
                        message = "Success ${response.code()} but unexpected null body",
                    ),
                )
            }

            return Outcome.success(body)
        } else {
            val errorBody = response.errorBody()?.string()
            return Outcome.failure(
                ApiError.Http(
                    status = HttpResponseStatus.create(response.code()),
                    rawCode = response.code(),
                    errorRequestUrl = response.raw().request.url.toString(),
                    message = "Http request failed: ${response.code()} " +
                        "${response.message()}, $errorBody",
                ),
            )
        }
    }
}
