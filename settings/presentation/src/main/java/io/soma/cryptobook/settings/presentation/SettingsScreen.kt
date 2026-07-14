package io.soma.cryptobook.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.soma.cryptobook.core.designsystem.component.appbar.CryptoMediumTopAppBar
import io.soma.cryptobook.core.designsystem.component.scaffold.CryptoScaffold
import io.soma.cryptobook.core.designsystem.resource.CryptoString
import io.soma.cryptobook.core.designsystem.theme.theme.CryptoTheme
import io.soma.cryptobook.core.domain.model.AppTheme
import io.soma.cryptobook.core.domain.model.CoinSortColumn
import io.soma.cryptobook.core.domain.model.CoinSortDirection
import io.soma.cryptobook.core.domain.model.CurrencyUnit
import io.soma.cryptobook.core.domain.model.Language
import io.soma.cryptobook.core.domain.model.UserData
import io.soma.cryptobook.settings.presentation.component.CryptoSettingCard
import io.soma.cryptobook.settings.presentation.component.CryptoSettingDivider
import io.soma.cryptobook.settings.presentation.component.CryptoSettingSelectionRow
import io.soma.cryptobook.settings.presentation.component.ExchangeRateCard
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

/**
 * The Settings screen.
 */
@Composable
fun SettingsScreen(modifier: Modifier = Modifier, viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    CryptoScaffold(
        modifier = modifier,
        topBar = {
            CryptoMediumTopAppBar(
                title = "Settings",
            )
        },
    ) {
        SettingsScreenContent(
            state = state,
            onAppThemeSelect = { appTheme ->
                viewModel.trySendAction(SettingsAction.AppThemeSelect(appTheme))
            },
            onCurrencyUnitSelect = { currencyUnit ->
                viewModel.trySendAction(SettingsAction.CurrencyUnitSelect(currencyUnit))
            },
            onLanguageSelect = { language ->
                viewModel.trySendAction(SettingsAction.LanguageSelect(language))
            },
        )
    }
}

@Composable
internal fun SettingsScreenContent(
    state: SettingsState,
    onAppThemeSelect: (AppTheme) -> Unit,
    onCurrencyUnitSelect: (CurrencyUnit) -> Unit,
    onLanguageSelect: (Language) -> Unit,
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
            .background(CryptoTheme.colorScheme.background.secondary)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        CryptoSettingCard {
            // App Theme
            CryptoSettingSelectionRow(
                title = stringResource(CryptoString.crypto_settings_theme_title),
                dialogTitle = stringResource(CryptoString.crypto_settings_theme_title),
                options = listOf(
                    stringResource(CryptoString.crypto_settings_theme_system),
                    stringResource(CryptoString.crypto_settings_theme_light),
                    stringResource(CryptoString.crypto_settings_theme_dark),
                ),
                selectedIndex = themeOptions.indexOf(currentAppTheme),
                onOptionSelected = { index -> onAppThemeSelect(themeOptions[index]) },
            )

            CryptoSettingDivider()

            // Price Currency Unit
            CryptoSettingSelectionRow(
                title = stringResource(CryptoString.crypto_settings_currency_unit_title),
                dialogTitle = stringResource(CryptoString.crypto_settings_currency_unit_title),
                options = listOf(
                    stringResource(CryptoString.crypto_settings_currency_dollar),
                    stringResource(CryptoString.crypto_settings_currency_won),
                ),
                selectedIndex = currencyOptions.indexOf(currentCurrency),
                onOptionSelected = { index -> onCurrencyUnitSelect(currencyOptions[index]) },
            )

            CryptoSettingDivider()

            // Language
            CryptoSettingSelectionRow(
                title = stringResource(CryptoString.crypto_settings_language_title),
                dialogTitle = stringResource(CryptoString.crypto_settings_language_title),
                options = listOf(
                    stringResource(CryptoString.crypto_settings_language_system),
                    stringResource(CryptoString.crypto_settings_language_english_native),
                    stringResource(CryptoString.crypto_settings_language_korean_native),
                ),
                selectedIndex = languageOptions.indexOf(currentLanguage),
                onOptionSelected = { index -> onLanguageSelect(languageOptions[index]) },
            )
        }

        // Exchange Rate
        ExchangeRateCard(
            title = stringResource(CryptoString.crypto_settings_exchange_rate_title),
            rateText = formatExchangeRate(exchangeRate),
            updateTimeText = stringResource(
                CryptoString.crypto_settings_exchange_rate_updated_now,
            ),
            onRefreshClick = {
                // TODO: Add refresh action
            },
        )
    }
}

@Composable
private fun formatExchangeRate(rate: BigDecimal?): String {
    if (rate == null) return stringResource(CryptoString.crypto_settings_exchange_rate_loading)
    val numberFormat = NumberFormat.getNumberInstance(Locale.US)
    return stringResource(
        CryptoString.crypto_settings_exchange_rate_format,
        numberFormat.format(rate),
    )
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenContentPreview() {
    SettingsScreenContent(
        state = SettingsState(
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
        onAppThemeSelect = {},
        onCurrencyUnitSelect = {},
        onLanguageSelect = {},
    )
}
