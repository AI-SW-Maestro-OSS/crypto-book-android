package io.soma.cryptobook.core.domain.model

import java.math.BigDecimal

data class UserData(
    val language: Language,
    val currencyUnit: CurrencyUnit,
    val usdKrwExchangeRate: BigDecimal,
    val appTheme: AppTheme,
    val coinSortColumn: CoinSortColumn,
    val coinSortDirection: CoinSortDirection,
)
