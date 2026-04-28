package io.soma.cryptobook.core.domain.outcome

/**
 * 작업의 결과를 나타내는 sealed interface.
 *
 * @param SUCCESS 작업이 성공했을 때 반환되는 값의 타입.
 * @param FAILURE 작업이 실패했을 때 반환되는 오류의 타입.
 */
sealed interface Outcome<out SUCCESS, out FAILURE> {

    data class Success<out SUCCESS>(val data: SUCCESS) : Outcome<SUCCESS, Nothing>

    data class Failure<out FAILURE>(
        val error: FAILURE,
        val cause: Any? = null,
    ) : Outcome<Nothing, FAILURE>

    val isSuccess: Boolean
        get() = this is Success

    val isFailure: Boolean
        get() = this is Failure

    companion object {
        /**
         * 주어진 값으로 [Success] 결과를 생성한다.
         *
         * @param data 성공 결과의 값.
         */
        fun <SUCCESS> success(data: SUCCESS): Outcome<SUCCESS, Nothing> = Success(data)

        /**
         * 주어진 오류로 [Failure] 결과를 생성한다.
         *
         * @param error 실패 결과의 오류.
         */
        fun <FAILURE> failure(error: FAILURE): Outcome<Nothing, FAILURE> = Failure(error)
    }
}

/**
 * [Outcome]의 성공 값과 실패 오류를 새로운 값으로 변환한다.
 *
 * @param transformSuccess [Success]의 값을 새로운 값으로 변환하는 함수.
 * @param transformFailure [Failure]의 오류를 새로운 값으로 변환하는 함수.
 */
inline fun <IN_SUCCESS, IN_FAILURE, OUT_SUCCESS, OUT_FAILURE> Outcome<IN_SUCCESS, IN_FAILURE>.map(
    transformSuccess: (IN_SUCCESS) -> OUT_SUCCESS,
    transformFailure: (IN_FAILURE, Any?) -> OUT_FAILURE,
): Outcome<OUT_SUCCESS, OUT_FAILURE> {
    return when (this) {
        is Outcome.Success -> Outcome.success(transformSuccess(data))
        is Outcome.Failure -> Outcome.failure(transformFailure(error, cause))
    }
}

/**
 * [Outcome]의 성공 값을 새로운 값으로 변환한다.
 *
 * @param transformSuccess [Success]의 값을 새로운 값으로 변환하는 함수.
 */
inline fun <IN_SUCCESS, FAILURE, OUT_SUCCESS> Outcome<IN_SUCCESS, FAILURE>.mapSuccess(
    transformSuccess: (IN_SUCCESS) -> OUT_SUCCESS,
): Outcome<OUT_SUCCESS, FAILURE> {
    return when (this) {
        is Outcome.Success -> Outcome.success(transformSuccess(data))
        is Outcome.Failure -> this
    }
}

/**
 * [Outcome]의 성공 값을 새로운 [Outcome]으로 flat map한다.
 */
inline fun <IN_SUCCESS, FAILURE, OUT_SUCCESS> Outcome<IN_SUCCESS, FAILURE>.flatMapSuccess(
    transformSuccess: (IN_SUCCESS) -> Outcome<OUT_SUCCESS, FAILURE>,
): Outcome<OUT_SUCCESS, FAILURE> {
    return when (this) {
        is Outcome.Success -> transformSuccess(data)
        is Outcome.Failure -> this
    }
}

/**
 * [Outcome]의 실패 오류를 새로운 값으로 변환한다.
 *
 * @param transformFailure [Failure]의 오류를 새로운 값으로 변환하는 함수.
 */
inline fun <SUCCESS, IN_FAILURE, OUT_FAILURE> Outcome<SUCCESS, IN_FAILURE>.mapFailure(
    transformFailure: (IN_FAILURE, Any?) -> OUT_FAILURE,
): Outcome<SUCCESS, OUT_FAILURE> {
    return when (this) {
        is Outcome.Success -> this
        is Outcome.Failure -> Outcome.failure(transformFailure(error, cause))
    }
}

/**
 * [Outcome]의 값에 따라 주어진 함수를 실행한다.
 *
 * @param onSuccess 결과가 [Success]일 때 실행할 함수.
 * @param onFailure 결과가 [Failure]일 때 실행할 함수.
 */
fun <SUCCESS, FAILURE> Outcome<SUCCESS, FAILURE>.handle(
    onSuccess: (SUCCESS) -> Unit,
    onFailure: (FAILURE) -> Unit,
) {
    when (this) {
        is Outcome.Success -> onSuccess(data)
        is Outcome.Failure -> onFailure(error)
    }
}

/**
 * [Outcome]의 값에 따라 주어진 suspend 함수를 실행한다.
 *
 * @param onSuccess 결과가 [Success]일 때 실행할 suspend 함수.
 * @param onFailure 결과가 [Failure]일 때 실행할 suspend 함수.
 */
suspend fun <SUCCESS, FAILURE> Outcome<SUCCESS, FAILURE>.handleAsync(
    onSuccess: suspend (SUCCESS) -> Unit,
    onFailure: suspend (FAILURE) -> Unit,
) {
    when (this) {
        is Outcome.Success -> onSuccess(data)
        is Outcome.Failure -> onFailure(error)
    }
}

/**
 * [Outcome]의 성공 값 또는 실패 오류를 하나의 새로운 값으로 접는다.
 */
inline fun <SUCCESS, FAILURE, R> Outcome<SUCCESS, FAILURE>.fold(
    onSuccess: (SUCCESS) -> R,
    onFailure: (FAILURE) -> R,
): R {
    return when (this) {
        is Outcome.Success -> onSuccess(data)
        is Outcome.Failure -> onFailure(error)
    }
}
