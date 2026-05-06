package io.soma.cryptobook.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.soma.cryptobook.core.designsystem.theme.ScreenBackground
import io.soma.cryptobook.core.designsystem.theme.component.CbTitleTopAppBar
import io.soma.cryptobook.core.domain.model.CurrencyUnit
import io.soma.cryptobook.core.domain.model.Language
import io.soma.cryptobook.core.domain.model.UserData
import io.soma.cryptobook.core.presentation.mvi.observeWithoutEffect
import io.soma.cryptobook.settings.presentation.component.ExchangeRateCard
import io.soma.cryptobook.settings.presentation.component.SettingsOptionCard
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SettingsRoute(modifier: Modifier = Modifier, viewModel: SettingsViewModel = hiltViewModel()) {
    val (state, dispatch) = viewModel.observeWithoutEffect()

    Column(modifier = Modifier.fillMaxSize()) {
        CbTitleTopAppBar("Settings")
        SettingsScreen(
            state = state.value,
            onEvent = dispatch,
            modifier = modifier,
        )
    }
}

@Composable
internal fun SettingsScreen(
    state: SettingsContract.State,
    onEvent: (SettingsContract.Event) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentLanguage = state.userData?.language ?: Language.ENGLISH
    val currentCurrency = state.userData?.currencyUnit ?: CurrencyUnit.DOLLAR
    val exchangeRate = state.userData?.usdKrwExchangeRate

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Price Currency Unit
        SettingsOptionCard(
            title = "Price Currency Unit",
            description = "Choose the currency unit for all prices.",
            options = listOf("Dollar", "Won"),
            selectedIndex = when (currentCurrency) {
                CurrencyUnit.DOLLAR -> 0
                CurrencyUnit.WON -> 1
            },
            onOptionSelected = { index ->
                val currency = when (index) {
                    0 -> CurrencyUnit.DOLLAR
                    else -> CurrencyUnit.WON
                }
                onEvent(SettingsContract.Event.SetCurrencyUnit(currency))
            },
        )

        // Language
        SettingsOptionCard(
            title = "Language",
            description = "Change the display language of the app.",
            options = listOf("English", "Korean"),
            selectedIndex = when (currentLanguage) {
                Language.ENGLISH -> 0
                Language.KOREAN -> 1
            },
            onOptionSelected = { index ->
                val language = when (index) {
                    0 -> Language.ENGLISH
                    else -> Language.KOREAN
                }
                onEvent(SettingsContract.Event.SetLanguage(language))
            },
        )

        // Exchange Rate
        ExchangeRateCard(
            title = "Exchange Rate",
            rateText = formatExchangeRate(exchangeRate),
            updateTimeText = "Rates updated just now",
            onRefreshClick = {
                // TODO: Add refresh event
            },
        )
    }
}

private fun formatExchangeRate(rate: BigDecimal?): String {
    if (rate == null) return "Loading..."
    val numberFormat = NumberFormat.getNumberInstance(Locale.US)
    return "1 USD = ${numberFormat.format(rate)} WON"
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    SettingsScreen(
        state = SettingsContract.State(
            userData = UserData(
                language = Language.KOREAN,
                currencyUnit = CurrencyUnit.DOLLAR,
                usdKrwExchangeRate = BigDecimal("1450"),
            ),
            isLoading = false,
        ),
        onEvent = {},
    )
}
