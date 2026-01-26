package io.soma.cryptobook.coindetail.presentation

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.soma.cryptobook.coindetail.domain.usecase.ObserveCoinDetailUseCase
import io.soma.cryptobook.coindetail.presentation.mapper.CoinDetailPresentationModelMapper
import io.soma.cryptobook.core.domain.message.MessageHelper
import io.soma.cryptobook.core.domain.navigation.NavigationHelper
import io.soma.cryptobook.core.presentation.MviViewModel

@HiltViewModel(assistedFactory = CoinDetailViewModel.Factory::class)
class CoinDetailViewModel @AssistedInject constructor(
    @Assisted private val coinName: String,
    private val observeCoinDetailUseCase: ObserveCoinDetailUseCase,
    private val mapper: CoinDetailPresentationModelMapper,
    private val navigationHelper: NavigationHelper,
    private val messageHelper: MessageHelper,
) : MviViewModel<CoinDetailEvent, CoinDetailUiState, CoinDetailSideEffect>(
    CoinDetailUiState(symbol = coinName),
) {
    @AssistedFactory
    interface Factory {
        fun create(coinName: String): CoinDetailViewModel
    }

    init {
        observeCoinDetail()
    }

    override fun handleEvent(event: CoinDetailEvent) {
        when (event) {
            CoinDetailEvent.OnBackClicked -> {
                navigationHelper.back()
            }
        }
    }

    private fun observeCoinDetail() {
        intent {
            observeCoinDetailUseCase(symbol = coinName).collect { result ->
                when (result) {
                    is ObserveCoinDetailUseCase.Result.Success -> {
                        reduce {
                            mapper.toUiState(
                                vo = result.coinDetail,
                                isLoading = false,
                                errorMsg = null,
                            )
                        }
                    }

                    is ObserveCoinDetailUseCase.Result.Error.Connection -> {
                        reduce { copy(isLoading = false, errorMsg = "연결 오류") }
                        messageHelper.showToast("연결 오류가 발생했습니다")
                    }

                    is ObserveCoinDetailUseCase.Result.Error.Disconnected -> {
                        reduce { copy(isLoading = false, errorMsg = "연결 끊김") }
                        messageHelper.showToast("실시간 연결이 끊겼습니다")
                    }
                }
            }
        }
    }
}
