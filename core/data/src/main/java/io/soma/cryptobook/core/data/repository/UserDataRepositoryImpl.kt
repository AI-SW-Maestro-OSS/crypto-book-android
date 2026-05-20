package io.soma.cryptobook.core.data.repository

import io.soma.cryptobook.core.data.datastore.CbPreferencesDataSource
import io.soma.cryptobook.core.domain.model.AppTheme
import io.soma.cryptobook.core.domain.model.CurrencyUnit
import io.soma.cryptobook.core.domain.model.Language
import io.soma.cryptobook.core.domain.model.UserData
import io.soma.cryptobook.core.domain.repository.UserDataRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserDataRepositoryImpl @Inject constructor(
    private val cbPreferencesDataSource: CbPreferencesDataSource,
) : UserDataRepository {
    override val userData: Flow<UserData> =
        cbPreferencesDataSource.userData

    override suspend fun setLanguage(language: Language) {
        cbPreferencesDataSource.setLanguage(language)
    }

    override suspend fun setPriceCurrency(currencyUnit: CurrencyUnit) {
        cbPreferencesDataSource.setCurrencyUnit(currencyUnit)
    }

    override suspend fun setUsdKrwExchangeRate(usdKrwExchangeRate: Long) {
        cbPreferencesDataSource.setUsdKrwExchangeRate(usdKrwExchangeRate)
    }

    override suspend fun setAppTheme(appTheme: AppTheme) {
        cbPreferencesDataSource.setAppTheme(appTheme)
    }
}
