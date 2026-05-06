package io.soma.cryptobook.core.presentation.mvi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * 사이드 이펙트([EFFECT])를 가진 단방향 뷰모델 인터페이스입니다. [STATE]를 가지고 있으며 [EVENT]를 처리할 수 있습니다.
 *
 * @param STATE 뷰모델의 상태를 나타내는 타입입니다. 예를 들어, 화면의 UI 상태는
 * 상태로 나타낼 수 있습니다.
 * @param EVENT 발생할 수 있으며 뷰모델에서 처리해야 하는 사용자 액션을 나타내는 타입입니다.
 * 예를 들어, 버튼 클릭은 이벤트로 나타낼 수 있습니다.
 * @param EFFECT 상태 변경에 대한 응답으로 발생할 수 있는 사이드 이펙트를 나타내는 타입입니다.
 * 예를 들어, 네비게이션 이벤트는 이펙트로 나타낼 수 있습니다.
 */
public interface UnidirectionalViewModel<STATE, EVENT, EFFECT> {

    /**
     * 뷰모델의 현재 [STATE]
     */
    public val state: StateFlow<STATE>

    /**
     * 뷰모델에 의해 생성된 사이드 이펙트([EFFECT])
     */
    public val effect: SharedFlow<EFFECT>

    /**
     * [EVENT]를 처리하고 뷰모델의 [STATE]를 업데이트하는 함수
     *
     * @param event 처리할 [EVENT]
     */
    public fun event(event: EVENT)
}

public data class StateDispatch<STATE, EVENT>(
    val state: State<STATE>,
    val dispatch: (EVENT) -> Unit,
)

@Suppress("MaxLineLength")
@Composable
public inline fun <reified STATE, EVENT, EFFECT> UnidirectionalViewModel<STATE, EVENT, EFFECT>.observe(
    crossinline handleEffect: (EFFECT) -> Unit,
): StateDispatch<STATE, EVENT> {
    val state = state.collectAsStateWithLifecycle()
    val dispatch: (EVENT) -> Unit = { event(it) }

    LaunchedEffect(key1 = effect) {
        effect.collect { effect ->
            handleEffect(effect)
        }
    }

    return StateDispatch(state = state, dispatch = dispatch)
}

@Suppress("MaxLineLength")
@Composable
public inline fun <reified STATE, EVENT, EFFECT> UnidirectionalViewModel<STATE, EVENT, EFFECT>.observeWithoutEffect(
    // no effect handler
): StateDispatch<STATE, EVENT> {
    val collectedState = state.collectAsStateWithLifecycle()
    val dispatch: (EVENT) -> Unit = { event(it) }

    return StateDispatch(
        state = collectedState,
        dispatch = dispatch,
    )
}
