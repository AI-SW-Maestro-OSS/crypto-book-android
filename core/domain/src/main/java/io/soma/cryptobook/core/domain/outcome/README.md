# Outcome Module

이 모듈은 `Outcome`을 제공합니다. `Outcome`은 아키텍처 경계를 넘어 exception을 던지는 대신, 예상 가능한
성공과 실패를 값으로 표현하기 위한 작은 result 타입입니다.

`Outcome`은 호출자가 정상적인 애플리케이션 흐름 안에서 처리해야 하는 오류에 사용합니다. 프로그래밍 오류,
잘못된 내부 상태, 복구할 수 없는 실패에는 exception을 계속 사용해야 합니다.

## 핵심 타입

```kotlin
sealed interface Outcome<out SUCCESS, out FAILURE> {
    data class Success<out SUCCESS>(val data: SUCCESS) : Outcome<SUCCESS, Nothing>

    data class Failure<out FAILURE>(
        val error: FAILURE,
        val cause: Any? = null,
    ) : Outcome<Nothing, FAILURE>
}
```

성공 타입과 실패 타입을 모두 명시합니다.

```kotlin
Outcome<User, LoadUserError>
Outcome<Unit, FileOperationError>
Outcome<Profile, AccountSettingError>
```

이렇게 하면 호출자가 처리해야 하는 실패 계약이 타입에 드러납니다. `error`는 보통 도메인별 sealed class나
sealed interface로 정의합니다. 선택 값인 `cause`에는 원래 발생한 저수준 exception이나 진단용 객체를 보존할 수
있습니다.

## Factory 함수

성공 결과를 만들 때는 `Outcome.success(data)`를 사용합니다.

```kotlin
return Outcome.success(profile)
```

실패 결과를 만들 때는 `Outcome.failure(error)`를 사용합니다.

```kotlin
return Outcome.failure(ValidateAccountNameError.EmptyName)
```

원인을 함께 보존해야 한다면 `Failure` 생성자를 직접 사용합니다.

```kotlin
return Outcome.Failure(
    error = FileOperationError.Unavailable(uri, message),
    cause = exception,
)
```

## 성공 값 변환

성공 값만 바꾸고 실패 타입은 그대로 유지해야 할 때 `mapSuccess`를 사용합니다.

```kotlin
val result: Outcome<UiProfile, AccountSettingError> =
    profileOutcome.mapSuccess { profile ->
        profile.toUiProfile()
    }
```

이 함수는 반복적인 `when` 코드를 대신합니다.

```kotlin
val result = when (profileOutcome) {
    is Outcome.Success -> Outcome.Success(profileOutcome.data.toUiProfile())
    is Outcome.Failure -> profileOutcome
}
```

DTO를 도메인 모델로 바꾸거나, 도메인 모델을 UI 모델로 바꾸는 등 성공 데이터만 변환할 때 사용합니다.

## Outcome 작업 연결

성공했을 때만 다음 작업을 실행하고, 그 다음 작업도 `Outcome`을 반환한다면 `flatMapSuccess`를 사용합니다.

```kotlin
val result: Outcome<SavedMessages, SyncError> =
    connect().flatMapSuccess {
        fetchMessages()
    }.flatMapSuccess { messages ->
        saveMessages(messages)
    }
```

중간 단계 중 하나라도 실패하면 해당 실패가 그대로 반환되고, 남은 작업은 실행되지 않습니다.

연결되는 작업들의 실패 타입은 같아야 합니다.

```kotlin
Outcome<A, SyncError>.flatMapSuccess { Outcome<B, SyncError> }
```

서로 다른 계층이 다른 오류 타입을 노출한다면, 체이닝 전에 공통 오류 타입으로 먼저 변환해야 합니다.

## 실패 값 변환

성공 값은 그대로 두고 실패 타입만 바꿔야 할 때 `mapFailure`를 사용합니다.

```kotlin
val result: Outcome<User, AccountError> =
    storageOutcome.mapFailure { error, _ ->
        AccountError.Storage(error)
    }
```

이 함수는 아키텍처 경계에서 유용합니다. Repository는 저수준 storage 또는 network 오류를 feature별 도메인 오류로
번역할 수 있고, 상위 계층은 구현 세부사항을 알 필요가 없어집니다.

변환 함수는 기존 `cause`를 전달받지만, 반환되는 `Failure`가 이 `cause`를 자동으로 보존하지는 않습니다. 반환되는
`Outcome`에도 cause를 유지해야 한다면 명시적인 `when`을 사용하고 `Outcome.Failure(error, cause)`를 직접
생성해야 합니다.

## 성공과 실패를 모두 변환

성공 값과 실패 값을 모두 변환해야 할 때 `map`을 사용합니다.

```kotlin
val result: Outcome<UiUser, UiError> =
    userOutcome.map(
        transformSuccess = { user -> user.toUiUser() },
        transformFailure = { error, _ -> error.toUiError() },
    )
```

`mapFailure`와 마찬가지로 기존 `cause`는 failure transform에 전달되지만, 반환되는 `Failure`에 자동으로 복사되지는
않습니다.

## Side Effect 처리

반환값이 필요 없고 성공/실패에 따라 side effect만 실행해야 할 때 `handle`을 사용합니다.

```kotlin
outcome.handle(
    onSuccess = { profile ->
        updateState { it.copy(profile = profile) }
    },
    onFailure = { error ->
        handleError(error)
    },
)
```

ViewModel에서 state를 갱신하거나, effect를 발행하거나, 오류를 로깅할 때 자주 사용합니다.

성공 또는 실패 callback 안에서 suspend 함수를 호출해야 한다면 `handleAsync`를 사용합니다.

```kotlin
outcome.handleAsync(
    onSuccess = { id ->
        cache.set(accountId, id)
    },
    onFailure = { error ->
        logger.error(tag = TAG) { error.toString() }
    },
)
```

## 하나의 값으로 접기

성공과 실패를 모두 하나의 반환값으로 바꿔야 할 때 `fold`를 사용합니다.

```kotlin
val errorMessage: String? = validationOutcome.fold(
    onSuccess = { null },
    onFailure = { error -> error.toMessage() },
)
```

검증 결과를 UI 오류 메시지로 바꾸거나, UI state 일부를 계산하거나, `Outcome`에서 Boolean 값을 만들 때 유용합니다.

```kotlin
val isValid = validationOutcome.fold(
    onSuccess = { true },
    onFailure = { false },
)
```

## 적절한 Helper 선택

| 하고 싶은 일 | Helper |
| --- | --- |
| 성공 만들기 | `Outcome.success(data)` |
| 실패 만들기 | `Outcome.failure(error)` |
| cause가 있는 실패 만들기 | `Outcome.Failure(error, cause)` |
| 성공 값만 변환하기 | `mapSuccess` |
| 실패 값만 변환하기 | `mapFailure` |
| 성공과 실패 값을 모두 변환하기 | `map` |
| 성공 후 다른 `Outcome` 작업 실행하기 | `flatMapSuccess` |
| 동기 side effect 실행하기 | `handle` |
| suspend side effect 실행하기 | `handleAsync` |
| 성공 또는 실패를 하나의 반환값으로 바꾸기 | `fold` |

## Outcome과 Exception

호출자가 처리해야 하는 예상 가능한 실패에는 `Outcome`을 사용합니다.

```kotlin
Outcome.failure(ValidationError.EmptyField)
Outcome.failure(FileOperationError.Unavailable(uri))
Outcome.failure(AccountSettingError.NotFound(accountId))
```

프로그래밍 오류, 잘못된 내부 상태, 복구할 수 없는 실패에는 exception을 사용합니다.

```kotlin
require(index >= 0) { "index must be non-negative" }
checkNotNull(accountId) { "Account ID must be available" }
error("Unexpected state: $state")
```

외부 API는 내부적으로 exception을 던질 수 있습니다. 호출자가 그 실패를 합리적으로 처리할 수 있다면, 모듈 또는 계층
경계를 넘어 반환하기 전에 도메인별 실패로 변환합니다.

```kotlin
return try {
    val profile = dataSource.loadProfile(accountId)
    Outcome.success(profile)
} catch (exception: IOException) {
    Outcome.Failure(
        error = AccountSettingError.StorageError(exception.message),
        cause = exception,
    )
}
```
