package io.soma.cryptobook.core.data.repository

import io.soma.cryptobook.core.data.datastore.CryptoPreferencesDataSource
import io.soma.cryptobook.core.domain.model.AppTheme
import io.soma.cryptobook.core.domain.model.CoinSortColumn
import io.soma.cryptobook.core.domain.model.CoinSortDirection
import io.soma.cryptobook.core.domain.model.CurrencyUnit
import io.soma.cryptobook.core.domain.model.Language
import io.soma.cryptobook.core.domain.model.UserData
import io.soma.cryptobook.core.domain.repository.UserDataRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserDataRepositoryImpl @Inject constructor(
    private val cryptoPreferencesDataSource: CryptoPreferencesDataSource,
) : UserDataRepository {
    override val userData: Flow<UserData> =
        cryptoPreferencesDataSource.userData

    override suspend fun setLanguage(language: Language) {
        cryptoPreferencesDataSource.setLanguage(language)
    }

    override suspend fun setPriceCurrency(currencyUnit: CurrencyUnit) {
        cryptoPreferencesDataSource.setCurrencyUnit(currencyUnit)
    }

    override suspend fun setUsdKrwExchangeRate(usdKrwExchangeRate: Long) {
        cryptoPreferencesDataSource.setUsdKrwExchangeRate(usdKrwExchangeRate)
    }

    override suspend fun setAppTheme(appTheme: AppTheme) {
        cryptoPreferencesDataSource.setAppTheme(appTheme)
    }

    override suspend fun setCoinSort(column: CoinSortColumn, direction: CoinSortDirection) {
        cryptoPreferencesDataSource.setCoinSort(column, direction)
    }

    override suspend fun setWatchlistCoinSort(
        column: CoinSortColumn,
        direction: CoinSortDirection,
    ) {
        cryptoPreferencesDataSource.setWatchlistCoinSort(column, direction)
    }
}
