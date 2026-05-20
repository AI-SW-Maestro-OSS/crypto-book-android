package io.soma.cryptobook.core.domain.repository

import io.soma.cryptobook.core.domain.model.AppTheme
import io.soma.cryptobook.core.domain.model.CurrencyUnit
import io.soma.cryptobook.core.domain.model.Language
import io.soma.cryptobook.core.domain.model.UserData
import kotlinx.coroutines.flow.Flow

interface UserDataRepository {
    val userData: Flow<UserData>

    suspend fun setLanguage(language: Language)

    suspend fun setPriceCurrency(currencyUnit: CurrencyUnit)

    suspend fun setUsdKrwExchangeRate(usdKrwExchangeRate: Long)

    suspend fun setAppTheme(appTheme: AppTheme)
}
