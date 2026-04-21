package io.soma.cryptobook.core.data.datastore

import androidx.datastore.core.DataStore
import io.soma.cryptobook.core.data.CurrencyUnitProto
import io.soma.cryptobook.core.data.LanguageProto
import io.soma.cryptobook.core.data.UserPreferences
import io.soma.cryptobook.core.data.copy
import io.soma.cryptobook.core.domain.model.CurrencyUnit
import io.soma.cryptobook.core.domain.model.Language
import io.soma.cryptobook.core.domain.model.UserData
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import javax.inject.Inject

class CbPreferencesDataSource @Inject constructor(
    private val userPreferences: DataStore<UserPreferences>,
) {
    val lastTickSizeCheckedAtMillis = userPreferences.data
        .map { it.lastTickSizeCheckedAtMillis }

    val userData = userPreferences.data
        .map {
            UserData(
                language = when (it.language) {
                    null,
                    LanguageProto.LANGUAGE_UNSPECIFIED,
                    LanguageProto.UNRECOGNIZED,
                    LanguageProto.LANGUAGE_ENGLISH,
                    -> Language.ENGLISH

                    LanguageProto.LANGUAGE_KOREAN,
                    -> Language.KOREAN
                },
                currencyUnit = when (it.currencyUnit) {
                    null,
                    CurrencyUnitProto.CURRENCY_UNIT_UNSPECIFIED,
                    CurrencyUnitProto.UNRECOGNIZED,
                    CurrencyUnitProto.CURRENCY_UNIT_DOLLAR,
                    -> CurrencyUnit.DOLLAR

                    CurrencyUnitProto.CURRENCY_UNIT_WON,
                    -> CurrencyUnit.WON
                },
                usdKrwExchangeRate = BigDecimal.valueOf(it.usdKrwExchangeRate, 4),
            )
        }

    suspend fun setLanguage(language: Language) {
        userPreferences.updateData {
            it.copy {
                this.language = when (language) {
                    Language.ENGLISH -> LanguageProto.LANGUAGE_ENGLISH
                    Language.KOREAN -> LanguageProto.LANGUAGE_KOREAN
                }
            }
        }
    }

    suspend fun setCurrencyUnit(currencyUnit: CurrencyUnit) {
        userPreferences.updateData {
            it.copy {
                this.currencyUnit = when (currencyUnit) {
                    CurrencyUnit.DOLLAR -> CurrencyUnitProto.CURRENCY_UNIT_DOLLAR
                    CurrencyUnit.WON -> CurrencyUnitProto.CURRENCY_UNIT_WON
                }
            }
        }
    }

    suspend fun setUsdKrwExchangeRate(usdKrwExchangeRate: Long) {
        userPreferences.updateData {
            it.copy { this.usdKrwExchangeRate = usdKrwExchangeRate }
        }
    }

    suspend fun setLastTickSizeCheckedAtMillis(checkedAtMillis: Long) {
        userPreferences.updateData {
            it.copy { this.lastTickSizeCheckedAtMillis = checkedAtMillis }
        }
    }
}
