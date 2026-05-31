package io.soma.cryptobook.coindetail.data.orderbook

import io.soma.cryptobook.coindetail.domain.model.OrderBookLevelVO
import io.soma.cryptobook.coindetail.domain.model.OrderBookVO
import java.math.BigDecimal
import java.util.TreeMap

/** Outcome of feeding a depth diff into [OrderBookAssembler.onDiff]. */
enum class DepthSyncResult {
    /** The change was applied (or harmlessly dropped as stale). */
    Applied,

    /** Buffered because no usable snapshot is available yet. */
    Buffered,

    /** A sequence gap was detected; the caller must fetch a fresh snapshot. */
    NeedsResync,
}

/** REST depth snapshot input for [OrderBookAssembler.onSnapshot]. */
data class DepthSnapshot(
    val lastUpdateId: Long,
    val bids: List<OrderBookLevelVO>,
    val asks: List<OrderBookLevelVO>,
)

/** WebSocket depth diff input for [OrderBookAssembler.onDiff]. */
data class DepthDiff(
    val firstUpdateId: Long,
    val finalUpdateId: Long,
    val bids: List<OrderBookLevelVO>,
    val asks: List<OrderBookLevelVO>,
)

/**
 * Maintains a local Binance spot order book from a REST snapshot plus the
 * `<symbol>@depth` diff stream, following Binance's documented algorithm:
 *
 * 1. Diffs that arrive before a snapshot are buffered.
 * 2. On snapshot, drop buffered diffs whose `u <= lastUpdateId`.
 * 3. The first applied diff must satisfy `U <= lastUpdateId + 1 <= u`.
 * 4. Each subsequent diff must satisfy `U == previous u + 1`, otherwise a
 *    resync (fresh snapshot) is required.
 *
 * Quantity `0` removes a price level.
 *
 * Not thread-safe; callers must serialize access (e.g. via a Mutex).
 */
class OrderBookAssembler {
    private val bids = TreeMap<BigDecimal, BigDecimal>()
    private val asks = TreeMap<BigDecimal, BigDecimal>()
    private val bufferedDiffs = ArrayDeque<DepthDiff>()

    private var lastUpdateId = NO_UPDATE_ID
    private var prevFinalUpdateId = NO_UPDATE_ID
    private var synced = false

    fun onSnapshot(snapshot: DepthSnapshot) {
        bids.clear()
        asks.clear()
        snapshot.bids.forEach { applyLevel(bids, it) }
        snapshot.asks.forEach { applyLevel(asks, it) }
        lastUpdateId = snapshot.lastUpdateId
        prevFinalUpdateId = snapshot.lastUpdateId
        synced = false
        drainBuffer()
    }

    fun onDiff(diff: DepthDiff): DepthSyncResult {
        if (lastUpdateId == NO_UPDATE_ID) {
            bufferDiff(diff)
            return DepthSyncResult.Buffered
        }
        return applyOrdered(diff)
    }

    fun topOfBook(depth: Int): OrderBookVO = OrderBookVO(
        bids = bids.descendingMap().entries.asSequence()
            .take(depth)
            .map { OrderBookLevelVO(price = it.key, quantity = it.value) }
            .toList(),
        asks = asks.entries.asSequence()
            .take(depth)
            .map { OrderBookLevelVO(price = it.key, quantity = it.value) }
            .toList(),
    )

    private fun drainBuffer() {
        while (bufferedDiffs.isNotEmpty()) {
            val diff = bufferedDiffs.removeFirst()
            if (applyOrdered(diff) == DepthSyncResult.NeedsResync) {
                bufferedDiffs.clear()
                return
            }
        }
    }

    private fun applyOrdered(diff: DepthDiff): DepthSyncResult {
        if (diff.finalUpdateId <= lastUpdateId) {
            return DepthSyncResult.Applied
        }
        if (!synced) {
            if (diff.firstUpdateId > lastUpdateId + 1) {
                return DepthSyncResult.NeedsResync
            }
            applyChanges(diff)
            prevFinalUpdateId = diff.finalUpdateId
            synced = true
            return DepthSyncResult.Applied
        }
        if (diff.firstUpdateId != prevFinalUpdateId + 1) {
            return DepthSyncResult.NeedsResync
        }
        applyChanges(diff)
        prevFinalUpdateId = diff.finalUpdateId
        return DepthSyncResult.Applied
    }

    private fun applyChanges(diff: DepthDiff) {
        diff.bids.forEach { applyLevel(bids, it) }
        diff.asks.forEach { applyLevel(asks, it) }
    }

    private fun applyLevel(book: TreeMap<BigDecimal, BigDecimal>, level: OrderBookLevelVO) {
        if (level.quantity.signum() == 0) {
            book.remove(level.price)
        } else {
            book[level.price] = level.quantity
        }
    }

    private fun bufferDiff(diff: DepthDiff) {
        if (bufferedDiffs.size >= MAX_BUFFERED_DIFFS) {
            bufferedDiffs.removeFirst()
        }
        bufferedDiffs.addLast(diff)
    }

    private companion object {
        private const val NO_UPDATE_ID = -1L
        private const val MAX_BUFFERED_DIFFS = 2000
    }
}
