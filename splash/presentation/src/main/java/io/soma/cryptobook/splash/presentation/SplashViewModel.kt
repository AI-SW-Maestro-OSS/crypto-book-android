package io.soma.cryptobook.splash.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.soma.cryptobook.core.domain.outcome.handle
import io.soma.cryptobook.core.domain.repository.CoinRepository
import io.soma.cryptobook.core.domain.usecase.RefreshExchangeRateUseCase
import io.soma.cryptobook.core.domain.usecase.RefreshTickSizesIfRequiredUseCase
import io.soma.cryptobook.splash.domain.usecase.CheckUpdateRequirementUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val checkUpdateRequirementUseCase: CheckUpdateRequirementUseCase,
    private val refreshExchangeRateUseCase: RefreshExchangeRateUseCase,
    private val refreshTickSizesIfRequiredUseCase: RefreshTickSizesIfRequiredUseCase,
    private val coinRepository: CoinRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Loading)
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val currentVersion = context.packageManager
                .getPackageInfo(context.packageName, 0)
                .versionName.orEmpty()

            val versionCheckJob = async {
                runCatching {
                    checkUpdateRequirementUseCase(currentVersion)
                }.getOrDefault(false)
            }
            val prefetchJob = async {
                coinRepository.getCoinPrices()
                    .handle(
                        onSuccess = {
                            android.util.Log.d("SplashViewModel", "코인 가격 프리패치 성공")
                        },
                        onFailure = { error ->
                            android.util.Log.e("SplashViewModel", "코인 가격 프리패치 실패: $error")
                        },
                    )
            }


            val exchangeRateJob = async {
                runCatching { refreshExchangeRateUseCase() }
                    .onFailure { e ->
                        android.util.Log.e("SplashViewModel", "환율 갱신 실패: ${e.message}", e)
                    }
                    .onSuccess {
                        android.util.Log.d("SplashViewModel", "환율 갱신 성공")
                    }
            }

            val tickSizeJob = async {
                runCatching { refreshTickSizesIfRequiredUseCase() }
                    .onFailure { e ->
                        android.util.Log.e("SplashViewModel", "tickSize 갱신 실패: ${e.message}", e)
                    }
                    .onSuccess {
                        android.util.Log.d("SplashViewModel", "tickSize 갱신 성공")
                    }
            }

            val delayJob = async { delay(2000) }

            val isUpdateRequired = versionCheckJob.await()
            prefetchJob.await()
            exchangeRateJob.await()
            tickSizeJob.await()
            delayJob.await()

            _uiState.value = SplashUiState.Success(isUpdateRequired = isUpdateRequired)
        }
    }
}

sealed interface SplashUiState {
    data object Loading : SplashUiState
    data class Success(val isUpdateRequired: Boolean) : SplashUiState

    fun shouldKeepSplashScreen() = this is Loading
    fun shouldNavigateToUpdate() = this is Success && isUpdateRequired
}
