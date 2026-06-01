package io.soma.cryptobook.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.soma.cryptobook.core.designsystem.resource.CryptoString
import io.soma.cryptobook.core.designsystem.theme.component.appbar.CbMediumTopAppBar
import io.soma.cryptobook.core.designsystem.theme.component.scaffold.CbScaffold
import io.soma.cryptobook.core.designsystem.theme.component.snackbar.CbSnackbarHost
import io.soma.cryptobook.core.designsystem.theme.component.snackbar.model.rememberCbSnackbarHostState
import io.soma.cryptobook.core.designsystem.theme.theme.CbTheme
import io.soma.cryptobook.core.domain.model.AppTheme
import io.soma.cryptobook.core.domain.model.CoinSortColumn
import io.soma.cryptobook.core.domain.model.CoinSortDirection
import io.soma.cryptobook.core.domain.model.CurrencyUnit
import io.soma.cryptobook.core.domain.model.Language
import io.soma.cryptobook.core.domain.model.UserData
import io.soma.cryptobook.core.presentation.mvi.observe
import io.soma.cryptobook.settings.presentation.component.CryptoSettingCard
import io.soma.cryptobook.settings.presentation.component.CryptoSettingDivider
import io.soma.cryptobook.settings.presentation.component.CryptoSettingSelectionRow
import io.soma.cryptobook.settings.presentation.component.ExchangeRateCard
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SettingsRoute(modifier: Modifier = Modifier, viewModel: SettingsViewModel = hiltViewModel()) {
    val snackbarHostState = rememberCbSnackbarHostState()
    val resources = LocalContext.current.resources
    val (state, dispatch) = viewModel.observe { effect ->
        when (effect) {
            is SettingsContract.Effect.ShowSnackbar -> snackbarHostState.showSnackbar(
                message = effect.message.toString(resources),
                actionLabel = effect.actionLabel?.toString(resources),
            )
        }
    }

    CbScaffold(
        modifier = modifier,
        topBar = {
            CbMediumTopAppBar(
                title = "Settings",
            )
        },
        snackbarHost = { CbSnackbarHost(snackbarHostState) },
    ) {
        SettingsScreen(
            state = state.value,
            onEvent = dispatch,
        )
    }
}

@Composable
internal fun SettingsScreen(
    state: SettingsContract.State,
    onEvent: (SettingsContract.Event) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentLanguage = state.userData?.language ?: Language.SYSTEM
    val currentCurrency = state.userData?.currencyUnit ?: CurrencyUnit.DOLLAR
    val currentAppTheme = state.userData?.appTheme ?: AppTheme.SYSTEM
    val exchangeRate = state.userData?.usdKrwExchangeRate

    val themeOptions = listOf(AppTheme.SYSTEM, AppTheme.LIGHT, AppTheme.DARK)
    val currencyOptions = listOf(CurrencyUnit.DOLLAR, CurrencyUnit.WON)
    val languageOptions = listOf(Language.SYSTEM, Language.ENGLISH, Language.KOREAN)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CbTheme.colorScheme.background.secondary)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        CryptoSettingCard {
            // App Theme
            CryptoSettingSelectionRow(
                title = stringResource(CryptoString.cb_settings_theme_title),
                dialogTitle = stringResource(CryptoString.cb_settings_theme_title),
                options = listOf(
                    stringResource(CryptoString.cb_settings_theme_system),
                    stringResource(CryptoString.cb_settings_theme_light),
                    stringResource(CryptoString.cb_settings_theme_dark),
                ),
                selectedIndex = themeOptions.indexOf(currentAppTheme),
                onOptionSelected = { index ->
                    onEvent(SettingsContract.Event.SetAppTheme(themeOptions[index]))
                },
            )

            CryptoSettingDivider()

            // Price Currency Unit
            CryptoSettingSelectionRow(
                title = stringResource(CryptoString.cb_settings_currency_unit_title),
                dialogTitle = stringResource(CryptoString.cb_settings_currency_unit_title),
                options = listOf(
                    stringResource(CryptoString.cb_settings_currency_dollar),
                    stringResource(CryptoString.cb_settings_currency_won),
                ),
                selectedIndex = currencyOptions.indexOf(currentCurrency),
                onOptionSelected = { index ->
                    onEvent(SettingsContract.Event.SetCurrencyUnit(currencyOptions[index]))
                },
            )

            CryptoSettingDivider()

            // Language
            CryptoSettingSelectionRow(
                title = stringResource(CryptoString.cb_settings_language_title),
                dialogTitle = stringResource(CryptoString.cb_settings_language_title),
                options = listOf(
                    stringResource(CryptoString.cb_settings_language_system),
                    stringResource(CryptoString.cb_settings_language_english_native),
                    stringResource(CryptoString.cb_settings_language_korean_native),
                ),
                selectedIndex = languageOptions.indexOf(currentLanguage),
                onOptionSelected = { index ->
                    onEvent(SettingsContract.Event.SetLanguage(languageOptions[index]))
                },
            )
        }

        // Exchange Rate
        ExchangeRateCard(
            title = stringResource(CryptoString.cb_settings_exchange_rate_title),
            rateText = formatExchangeRate(exchangeRate),
            updateTimeText = stringResource(
                CryptoString.cb_settings_exchange_rate_updated_now,
            ),
            onRefreshClick = {
                // TODO: Add refresh event
            },
        )
    }
}

@Composable
private fun formatExchangeRate(rate: BigDecimal?): String {
    if (rate == null) return stringResource(CryptoString.cb_settings_exchange_rate_loading)
    val numberFormat = NumberFormat.getNumberInstance(Locale.US)
    return stringResource(
        CryptoString.cb_settings_exchange_rate_format,
        numberFormat.format(rate),
    )
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
                appTheme = AppTheme.SYSTEM,
                coinSortColumn = CoinSortColumn.NONE,
                coinSortDirection = CoinSortDirection.NONE,
                watchlistCoinSortColumn = CoinSortColumn.NONE,
                watchlistCoinSortDirection = CoinSortDirection.NONE,
            ),
            isLoading = false,
        ),
        onEvent = {},
    )
}
