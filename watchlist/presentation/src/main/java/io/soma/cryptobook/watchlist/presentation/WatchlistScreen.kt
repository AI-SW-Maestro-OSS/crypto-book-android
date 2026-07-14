package io.soma.cryptobook.watchlist.presentation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.soma.cryptobook.core.designsystem.component.appbar.CryptoMediumTopAppBar
import io.soma.cryptobook.core.designsystem.component.coinlist.CoinListItemData
import io.soma.cryptobook.core.designsystem.component.coinlist.CryptoCoinListSection
import io.soma.cryptobook.core.designsystem.component.coinlist.SortDirection
import io.soma.cryptobook.core.designsystem.component.scaffold.CryptoScaffold
import io.soma.cryptobook.core.designsystem.resource.CryptoString
import io.soma.cryptobook.core.designsystem.theme.theme.CryptoTheme
import io.soma.cryptobook.core.domain.model.CoinSortColumn
import io.soma.cryptobook.core.domain.model.CoinSortDirection
import io.soma.cryptobook.core.presentation.format.KrwPriceFormatter
import io.soma.cryptobook.core.presentation.format.SymbolFormatter
import io.soma.cryptobook.core.presentation.format.TickSizePriceFormatter
import io.soma.cryptobook.core.presentation.format.VolumeFormatter
import io.soma.cryptobook.core.presentation.jank.TrackScrollJank
import io.soma.cryptobook.core.ui.EventsEffect
import java.math.BigDecimal

/**
 * The Watchlist screen.
 */
@Composable
fun WatchlistScreen(
    onNavigateToCoinDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WatchlistViewModel = hiltViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val context = LocalContext.current

    EventsEffect(viewModel = viewModel) { event ->
        when (event) {
            is WatchlistEvent.NavigateToCoinDetail -> onNavigateToCoinDetail(event.symbol)

            is WatchlistEvent.ShowToast -> {
                Toast
                    .makeText(
                        context,
                        event.text.toString(context.resources),
                        Toast.LENGTH_SHORT,
                    )
                    .show()
            }
        }
    }

    WatchlistScreenContent(
        state = state,
        onCoinClick = { symbol -> viewModel.trySendAction(WatchlistAction.CoinClick(symbol)) },
        onSortClick = { column -> viewModel.trySendAction(WatchlistAction.SortClick(column)) },
        modifier = modifier,
    )
}

@Composable
internal fun WatchlistScreenContent(
    state: WatchlistState,
    onCoinClick: (String) -> Unit,
    onSortClick: (CoinSortColumn) -> Unit,
    modifier: Modifier = Modifier,
) {
    val lazyListState = rememberLazyListState()
    TrackScrollJank(scrollableState = lazyListState, stateName = "watchlist:coinList")

    CryptoScaffold(
        modifier = modifier,
        topBar = {
            CryptoMediumTopAppBar(
                title = stringResource(CryptoString.crypto_top_level_watchlist),
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
                onSymbolClick = { onSortClick(CoinSortColumn.SYMBOL) },
                onVolumeClick = { onSortClick(CoinSortColumn.VOLUME) },
                onPriceClick = { onSortClick(CoinSortColumn.PRICE) },
                onChangeClick = { onSortClick(CoinSortColumn.CHANGE) },
                onCoinClick = onCoinClick,
                lazyListState = lazyListState,
                emptyContent = { WatchlistEmpty() },
            )
        }
    }
}

@Composable
private fun WatchlistEmpty(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(CryptoString.crypto_watchlist_empty),
            color = CryptoTheme.colorScheme.text.secondary,
        )
    }
}

private fun WatchlistState.sortDirectionFor(column: CoinSortColumn): SortDirection =
    if (sortColumn != column) {
        SortDirection.None
    } else {
        when (sortDirection) {
            CoinSortDirection.NONE -> SortDirection.None
            CoinSortDirection.ASC -> SortDirection.Asc
            CoinSortDirection.DESC -> SortDirection.Desc
        }
    }

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
private fun WatchlistScreenContentPreview() {
    val sampleCoins = listOf(
        CoinItem(
            symbol = "BTCUSDT",
            imageUrl = "",
            price = BigDecimal("68500.52"),
            priceChangePercentage24h = 2.35,
            quoteVolume = BigDecimal("1240000000"),
        ),
        CoinItem(
            symbol = "ETHUSDT",
            imageUrl = "",
            price = BigDecimal("3500.25"),
            priceChangePercentage24h = -1.75,
            quoteVolume = BigDecimal("850400000"),
        ),
    )

    WatchlistScreenContent(
        state = WatchlistState(coins = sampleCoins, usdKrwExchangeRate = BigDecimal("1350")),
        onCoinClick = {},
        onSortClick = {},
        modifier = Modifier.background(CryptoTheme.colorScheme.background.primary),
    )
}

@Preview(showBackground = true)
@Composable
private fun WatchlistScreenContentEmptyPreview() {
    WatchlistScreenContent(
        state = WatchlistState(isLoading = false),
        onCoinClick = {},
        onSortClick = {},
        modifier = Modifier.background(CryptoTheme.colorScheme.background.primary),
    )
}
