package io.soma.cryptobook.splash.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.soma.cryptobook.core.domain.manager.BuildInfoManager
import io.soma.cryptobook.core.domain.outcome.handle
import io.soma.cryptobook.core.domain.repository.CoinRepository
import io.soma.cryptobook.core.domain.usecase.RefreshExchangeRateUseCase
import io.soma.cryptobook.core.domain.usecase.RefreshTickSizesIfRequiredUseCase
import io.soma.cryptobook.splash.domain.usecase.CheckUpdateRequirementUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val buildInfoManager: BuildInfoManager,
    private val checkUpdateRequirementUseCase: CheckUpdateRequirementUseCase,
    private val refreshExchangeRateUseCase: RefreshExchangeRateUseCase,
    private val refreshTickSizesIfRequiredUseCase: RefreshTickSizesIfRequiredUseCase,
    private val coinRepository: CoinRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Loading)
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        checkUpdateRequirement()
        prefetchCoinPrices()
        refreshExchangeRate()
        refreshTickSizes()
    }

    /**
     * Gates the splash: the app cannot decide between the main screen and the update
     * screen until the version check resolves, so this is the only work awaited before
     * leaving [SplashUiState.Loading].
     */
    private fun checkUpdateRequirement() {
        viewModelScope.launch {
            val currentVersion = buildInfoManager.versionName
            val isUpdateRequired = runCatching {
                checkUpdateRequirementUseCase(currentVersion)
            }.getOrDefault(false)

            _uiState.value = SplashUiState.Success(isUpdateRequired = isUpdateRequired)
        }
    }

    /**
     * Background warm-up. The home screen re-fetches and observes this data reactively,
     * so it must not block the splash from clearing.
     */
    private fun prefetchCoinPrices() {
        viewModelScope.launch {
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
    }

    private fun refreshExchangeRate() {
        viewModelScope.launch {
            runCatching { refreshExchangeRateUseCase() }
                .onFailure { e ->
                    android.util.Log.e("SplashViewModel", "환율 갱신 실패: ${e.message}", e)
                }
                .onSuccess {
                    android.util.Log.d("SplashViewModel", "환율 갱신 성공")
                }
        }
    }

    private fun refreshTickSizes() {
        viewModelScope.launch {
            runCatching { refreshTickSizesIfRequiredUseCase() }
                .onFailure { e ->
                    android.util.Log.e("SplashViewModel", "tickSize 갱신 실패: ${e.message}", e)
                }
                .onSuccess {
                    android.util.Log.d("SplashViewModel", "tickSize 갱신 성공")
                }
        }
    }
}

sealed interface SplashUiState {
    data object Loading : SplashUiState
    data class Success(val isUpdateRequired: Boolean) : SplashUiState

    fun shouldKeepSplashScreen() = this is Loading
    fun shouldNavigateToUpdate() = this is Success && isUpdateRequired
}
