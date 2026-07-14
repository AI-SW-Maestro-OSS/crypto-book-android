package io.soma.cryptobook.home.presentation

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.soma.cryptobook.core.designsystem.resource.CryptoString
import io.soma.cryptobook.core.designsystem.util.Text
import io.soma.cryptobook.core.designsystem.util.asText
import io.soma.cryptobook.core.domain.error.CoinPriceError
import io.soma.cryptobook.core.domain.image.CoinImageResolver
import io.soma.cryptobook.core.domain.model.CoinPriceVO
import io.soma.cryptobook.core.domain.model.CoinSortColumn
import io.soma.cryptobook.core.domain.model.CoinSortDirection
import io.soma.cryptobook.core.domain.model.CoinSortState
import io.soma.cryptobook.core.domain.model.next
import io.soma.cryptobook.core.domain.outcome.Outcome
import io.soma.cryptobook.core.domain.outcome.handle
import io.soma.cryptobook.core.domain.usecase.GetUserDataUseCase
import io.soma.cryptobook.core.domain.usecase.MarketRealtimeState
import io.soma.cryptobook.core.domain.usecase.ObserveMarketRealtimeState
import io.soma.cryptobook.core.domain.usecase.ObserveSortedCoinListUseCase
import io.soma.cryptobook.core.ui.BaseViewModel
import io.soma.cryptobook.home.domain.usecase.ObserveCoinListUseCase
import io.soma.cryptobook.home.domain.usecase.ObserveCoinSortUseCase
import io.soma.cryptobook.home.domain.usecase.SetCoinSortUseCase
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

/**
 * ViewModel for the Home screen.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    observeCoinListUseCase: ObserveCoinListUseCase,
    observeCoinSortUseCase: ObserveCoinSortUseCase,
    observeSortedCoinListUseCase: ObserveSortedCoinListUseCase,
    observeMarketRealtimeState: ObserveMarketRealtimeState,
    getUserDataUseCase: GetUserDataUseCase,
    private val setCoinSortUseCase: SetCoinSortUseCase,
    private val coinImageResolver: CoinImageResolver,
) : BaseViewModel<HomeState, HomeEvent, HomeAction>(
    initialState = HomeState(isLoading = true),
) {

    init {
        combine(
            observeSortedCoinListUseCase(
                prices = observeCoinListUseCase(),
                sort = observeCoinSortUseCase(),
            ),
            getUserDataUseCase(),
        ) { outcome, userData ->
            HomeAction.Internal.ReceiveCoinList(
                outcome = outcome,
                usdKrwExchangeRate = userData.usdKrwExchangeRate,
            )
        }
            .onEach(::sendAction)
            .launchIn(viewModelScope)

        observeCoinSortUseCase()
            .map { HomeAction.Internal.ReceiveCoinSort(sort = it) }
            .onEach(::sendAction)
            .launchIn(viewModelScope)

        observeMarketRealtimeState()
            .map { HomeAction.Internal.ReceiveRealtimeState(realtimeState = it) }
            .onEach(::sendAction)
            .launchIn(viewModelScope)
    }

    override fun handleAction(action: HomeAction) {
        when (action) {
            is HomeAction.CoinClick -> handleCoinClick(action)
            HomeAction.SearchClick -> handleSearchClick()
            is HomeAction.SortClick -> handleSortClick(action)
            is HomeAction.Internal.ReceiveCoinList -> handleReceiveCoinList(action)
            is HomeAction.Internal.ReceiveCoinSort -> handleReceiveCoinSort(action)
            is HomeAction.Internal.ReceiveRealtimeState -> handleReceiveRealtimeState(action)
        }
    }

    private fun handleCoinClick(action: HomeAction.CoinClick) {
        sendEvent(HomeEvent.NavigateToCoinDetail(symbol = action.symbol))
    }

    private fun handleSearchClick() {
        sendEvent(HomeEvent.NavigateToSearch)
    }

    private fun handleSortClick(action: HomeAction.SortClick) {
        val next = CoinSortState(column = state.sortColumn, direction = state.sortDirection)
            .next(action.column)
        viewModelScope.launch { setCoinSortUseCase(next.column, next.direction) }
    }

    private fun handleReceiveCoinList(action: HomeAction.Internal.ReceiveCoinList) {
        action.outcome.handle(
            onSuccess = { coins ->
                mutableStateFlow.update {
                    it.copy(
                        isLoading = false,
                        errorMsg = null,
                        coins = coins.map { coin ->
                            coin.toCoinItem(coinImageResolver.getImageUrl(coin.symbol))
                        },
                        usdKrwExchangeRate = action.usdKrwExchangeRate,
                    )
                }
            },
            onFailure = { error ->
                val text = error.toText()
                mutableStateFlow.update { it.copy(isLoading = false, errorMsg = text) }
                sendEvent(HomeEvent.ShowToast(text = text))
            },
        )
    }

    private fun handleReceiveCoinSort(action: HomeAction.Internal.ReceiveCoinSort) {
        mutableStateFlow.update {
            it.copy(sortColumn = action.sort.column, sortDirection = action.sort.direction)
        }
    }

    private fun handleReceiveRealtimeState(action: HomeAction.Internal.ReceiveRealtimeState) {
        mutableStateFlow.update {
            it.copy(realtimeStatusMessage = action.realtimeState.toRealtimeStatusText())
        }
    }
}

/**
 * State for the Home screen.
 */
data class HomeState(
    val coins: List<CoinItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMsg: Text? = null,
    val realtimeStatusMessage: Text? = null,
    val sortColumn: CoinSortColumn = CoinSortColumn.NONE,
    val sortDirection: CoinSortDirection = CoinSortDirection.NONE,
    val usdKrwExchangeRate: BigDecimal = BigDecimal.ZERO,
)

/**
 * One-shot UI events for the Home screen.
 */
sealed class HomeEvent {
    data class NavigateToCoinDetail(val symbol: String) : HomeEvent()

    data object NavigateToSearch : HomeEvent()

    data class ShowToast(val text: Text) : HomeEvent()
}

/**
 * User and system actions for the Home screen.
 */
sealed class HomeAction {
    data class CoinClick(val symbol: String) : HomeAction()

    data object SearchClick : HomeAction()

    data class SortClick(val column: CoinSortColumn) : HomeAction()

    /**
     * Internal actions dispatched by the ViewModel from coroutines.
     */
    sealed class Internal : HomeAction() {
        data class ReceiveCoinList(
            val outcome: Outcome<List<CoinPriceVO>, CoinPriceError>,
            val usdKrwExchangeRate: BigDecimal,
        ) : Internal()

        data class ReceiveCoinSort(val sort: CoinSortState) : Internal()

        data class ReceiveRealtimeState(val realtimeState: MarketRealtimeState) : Internal()
    }
}

/**
 * A single coin row rendered by the Home screen.
 */
data class CoinItem(
    val symbol: String,
    val imageUrl: String,
    val price: BigDecimal,
    val priceChangePercentage24h: Double,
    val tickSize: BigDecimal? = null,
    val quoteVolume: BigDecimal = BigDecimal.ZERO,
)

fun CoinPriceVO.toCoinItem(imageUrl: String) = CoinItem(
    symbol = symbol,
    imageUrl = imageUrl,
    price = price,
    priceChangePercentage24h = priceChangePercentage24h,
    tickSize = tickSize,
    quoteVolume = quoteVolume,
)

private fun MarketRealtimeState.toRealtimeStatusText(): Text? = when (this) {
    MarketRealtimeState.Connected,
    MarketRealtimeState.Connecting,
    MarketRealtimeState.Inactive,
    -> null

    MarketRealtimeState.Recovering -> CryptoString.crypto_realtime_recovering.asText()

    is MarketRealtimeState.Failed -> CryptoString.crypto_realtime_disconnected.asText()
}

private fun CoinPriceError.toText(): Text = when (this) {
    CoinPriceError.Network -> CryptoString.crypto_error_network.asText()
    CoinPriceError.RateLimited -> CryptoString.crypto_error_rate_limited.asText()
    CoinPriceError.Server -> CryptoString.crypto_error_server.asText()
    CoinPriceError.UnexpectedResponse -> CryptoString.crypto_error_unexpected_response.asText()
    is CoinPriceError.Unknown -> CryptoString.crypto_error_unknown.asText()
}
