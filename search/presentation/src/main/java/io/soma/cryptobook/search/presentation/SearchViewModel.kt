package io.soma.cryptobook.search.presentation

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.soma.cryptobook.core.domain.image.CoinImageResolver
import io.soma.cryptobook.core.domain.outcome.handle
import io.soma.cryptobook.core.presentation.mvi.BaseViewModel
import io.soma.cryptobook.search.domain.model.SearchCoin
import io.soma.cryptobook.search.domain.usecase.FilterSearchCoinsUseCase
import io.soma.cryptobook.search.domain.usecase.ObserveSearchCoinsUseCase
import io.soma.cryptobook.search.presentation.SearchContract.Effect
import io.soma.cryptobook.search.presentation.SearchContract.Event
import io.soma.cryptobook.search.presentation.SearchContract.State
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val observeSearchCoinsUseCase: ObserveSearchCoinsUseCase,
    private val filterSearchCoinsUseCase: FilterSearchCoinsUseCase,
    private val coinImageResolver: CoinImageResolver,
) : BaseViewModel<State, Event, Effect>(State()) {

    init {
        observeSearchResults()
    }

    override fun event(event: Event) {
        when (event) {
            is Event.OnSearchTermChanged -> onSearchTermChanged(event.searchTerm)
            is Event.OnListItemClick -> navigateToCoinDetail(event.coinName)
            Event.OnBackClicked -> navigateBack()
        }
    }

    private fun onSearchTermChanged(searchTerm: String) {
        updateState {
            it.copy(
                searchTerm = searchTerm,
            )
        }
    }

    private fun observeSearchResults() {
        combine(
            state.map { it.searchTerm }
                .distinctUntilChanged(),
            observeSearchCoinsUseCase(),
        ) { searchTerm, outcome ->
            searchTerm to outcome
        }.onEach { (searchTerm, outcome) ->
            outcome.handle(
                onSuccess = { coins ->
                    updateState {
                        it.copy(
                            viewState = createViewState(
                                searchTerm = searchTerm,
                                coins = coins,
                            ),
                        )
                    }
                },
                onFailure = {
                    updateState {
                        it.copy(
                            viewState = State.ViewState.Error(
                                message = "코인 목록을 불러오지 못했습니다.",
                            ),
                        )
                    }
                },
            )
        }.launchIn(viewModelScope)
    }

    private fun createViewState(searchTerm: String, coins: List<SearchCoin>): State.ViewState {
        val trimmedSearchTerm = searchTerm.trim()

        if (trimmedSearchTerm.isBlank()) {
            return State.ViewState.Empty(message = null)
        }
        val items = filterSearchCoinsUseCase(
            searchTerm = searchTerm,
            coins = coins,
        ).map { coin ->
            DisplayItem(
                symbol = coin.symbol,
                imageUrl = coinImageResolver.getImageUrl(coin.symbol),
            )
        }

        return if (items.isEmpty()) {
            State.ViewState.Empty(message = "There's no result")
        } else {
            State.ViewState.Content(items = items)
        }
    }

    private fun navigateToCoinDetail(coinName: String) {
        emitEffect(Effect.NavigateToCoinDetail(coinName))
    }

    private fun navigateBack() = emitEffect(Effect.NavigateBack)
}
