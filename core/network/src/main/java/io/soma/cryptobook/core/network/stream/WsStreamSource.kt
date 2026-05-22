package io.soma.cryptobook.core.network.stream

import io.soma.cryptobook.core.network.market.WsMarketMessage
import kotlinx.coroutines.flow.Flow

/**
 * 선언형 Binance 마켓 스트림 구독 API.
 *
 * - 동일한 [streams] 집합에 대한 호출은 upstream을 공유한다.
 * - 첫 collector가 등장하면 세션과 구독을 확보하고, 마지막 collector가 떠난 뒤
 *   [STOP_TIMEOUT_MS] 동안 새 collector가 없으면 unsubscribe 및 세션 lease를 해제한다.
 * - 반환된 Flow는 [streams]에 속한 메시지만 emit한다.
 * - 구독이 최종 실패하면 Flow는 throw로 종료된다.
 */
interface WsStreamSource {
    fun subscribe(streams: Set<String>): Flow<WsMarketMessage>

    companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
