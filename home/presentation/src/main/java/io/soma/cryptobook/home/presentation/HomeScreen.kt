package io.soma.cryptobook.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.soma.cryptobook.core.designsystem.component.appbar.CryptoMediumTopAppBar
import io.soma.cryptobook.core.designsystem.component.button.CryptoStandardIconButton
import io.soma.cryptobook.core.designsystem.component.coinlist.CoinListItemData
import io.soma.cryptobook.core.designsystem.component.coinlist.CryptoCoinListSection
import io.soma.cryptobook.core.designsystem.component.coinlist.SortDirection
import io.soma.cryptobook.core.designsystem.component.scaffold.CryptoScaffold
import io.soma.cryptobook.core.designsystem.resource.CryptoString
import io.soma.cryptobook.core.designsystem.theme.resource.CryptoDrawable
import io.soma.cryptobook.core.designsystem.theme.theme.CryptoTheme
import io.soma.cryptobook.core.designsystem.util.asText
import io.soma.cryptobook.core.domain.model.CoinSortColumn
import io.soma.cryptobook.core.domain.model.CoinSortDirection
import io.soma.cryptobook.core.presentation.format.KrwPriceFormatter
import io.soma.cryptobook.core.presentation.format.SymbolFormatter
import io.soma.cryptobook.core.presentation.format.TickSizePriceFormatter
import io.soma.cryptobook.core.presentation.format.VolumeFormatter
import io.soma.cryptobook.core.presentation.jank.TrackScrollJank
import java.math.BigDecimal

@Composable
fun HomeRoute(modifier: Modifier = Modifier, viewModel: HomeViewModel = hiltViewModel()) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    HomeScreen(
        state = uiState,
        onEvent = viewModel::handleEvent,
        modifier = modifier,
    )
}

@Composable
internal fun HomeScreen(
    state: HomeUiState,
    onEvent: (HomeEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val lazyListState = rememberLazyListState()
    TrackScrollJank(scrollableState = lazyListState, stateName = "home:coinList")

    CryptoScaffold(
        modifier = modifier,
        topBar = {
            CryptoMediumTopAppBar(
                title = "Crypto-Book-Android",
                actions = {
                    CryptoStandardIconButton(
                        vectorIconRes = CryptoDrawable.ic_search,
                        contentDescription = "search",
                        onClick = { onEvent(HomeEvent.SearchIconClick) },
                        modifier = Modifier,
                    )
                },
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CryptoTheme.colorScheme.background.secondary),
        ) {
            state.realtimeStatusMessage?.let { msg ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFF3CD))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(text = msg(), color = Color(0xFF8A6D3B))
                }
            }

            // Error message
            state.errorMsg?.let { msg ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Red.copy(alpha = 0.1f))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(text = msg(), color = Color.Red)
                }
            }

            CryptoCoinListSection(
                coins = state.coins.map { it.toCoinListItemData(state.usdKrwExchangeRate) },
                isLoading = state.isLoading,
                symbolSort = state.sortDirectionFor(CoinSortColumn.SYMBOL),
                volumeSort = state.sortDirectionFor(CoinSortColumn.VOLUME),
                priceSort = state.sortDirectionFor(CoinSortColumn.PRICE),
                changeSort = state.sortDirectionFor(CoinSortColumn.CHANGE),
                onSymbolClick = { onEvent(HomeEvent.OnSortClick(CoinSortColumn.SYMBOL)) },
                onVolumeClick = { onEvent(HomeEvent.OnSortClick(CoinSortColumn.VOLUME)) },
                onPriceClick = { onEvent(HomeEvent.OnSortClick(CoinSortColumn.PRICE)) },
                onChangeClick = { onEvent(HomeEvent.OnSortClick(CoinSortColumn.CHANGE)) },
                onCoinClick = { symbol -> onEvent(HomeEvent.OnCoinClicked(symbol)) },
                lazyListState = lazyListState,
            )
        }
    }
}

private fun HomeUiState.sortDirectionFor(column: CoinSortColumn): SortDirection =
    if (sortColumn != column) {
        SortDirection.None
    } else {
        when (sortDirection) {
            CoinSortDirection.NONE -> SortDirection.None
            CoinSortDirection.ASC -> SortDirection.Asc
            CoinSortDirection.DESC -> SortDirection.Desc
        }
    }

/**
 * Convert CoinItem to CoinListItemData.
 *
 * @param usdKrwExchangeRate KRW per USD; when not positive the secondary price is hidden.
 */
private fun CoinItem.toCoinListItemData(usdKrwExchangeRate: BigDecimal): CoinListItemData {
    val symbolParts = SymbolFormatter.split(symbol)
    val secondaryPrice = usdKrwExchangeRate
        .takeIf { it.signum() > 0 }
        ?.let { KrwPriceFormatter.format(price.multiply(it)) }
    return CoinListItemData(
        symbol = symbol,
        baseSymbol = symbolParts.base,
        quoteSymbol = symbolParts.quote,
        imageUrl = imageUrl,
        price = TickSizePriceFormatter.format(price, tickSize),
        secondaryPrice = secondaryPrice,
        volume = VolumeFormatter.format(quoteVolume),
        changePercent = priceChangePercentage24h,
    )
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    val sampleCoins = listOf(
        CoinItem("BTCUSDT", "", BigDecimal("68500.52"), 2.35, quoteVolume = BigDecimal("1240000000")),
        CoinItem("ETHUSDT", "", BigDecimal("3500.25"), -1.75, quoteVolume = BigDecimal("850400000")),
        CoinItem("BNBUSDT", "", BigDecimal("580.10"), 0.0, quoteVolume = BigDecimal("120700000")),
        CoinItem("SOLUSDT", "", BigDecimal("145.30"), 5.20, quoteVolume = BigDecimal("67800000")),
        CoinItem("XRPUSDT", "", BigDecimal("0.52"), -0.85, quoteVolume = BigDecimal("45300000")),
    )

    HomeScreen(
        state = HomeUiState(coins = sampleCoins, usdKrwExchangeRate = BigDecimal("1350")),
        onEvent = {},
        modifier = Modifier.background(CryptoTheme.colorScheme.background.primary),
    )
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenLoadingPreview() {
    HomeScreen(
        state = HomeUiState(isLoading = true),
        onEvent = {},
        modifier = Modifier.background(CryptoTheme.colorScheme.background.primary),
    )
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenErrorPreview() {
    HomeScreen(
        state = HomeUiState(errorMsg = CryptoString.crypto_error_network.asText()),
        onEvent = {},
        modifier = Modifier.background(CryptoTheme.colorScheme.background.primary),
    )
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenRealtimeWarningPreview() {
    HomeScreen(
        state = HomeUiState(
            realtimeStatusMessage = CryptoString.crypto_realtime_recovering.asText(),
        ),
        onEvent = {},
        modifier = Modifier.background(CryptoTheme.colorScheme.background.primary),
    )
}
