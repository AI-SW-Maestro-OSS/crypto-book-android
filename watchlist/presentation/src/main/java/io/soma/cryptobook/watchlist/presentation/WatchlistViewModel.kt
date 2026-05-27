package io.soma.cryptobook.watchlist.presentation

import dagger.hilt.android.lifecycle.HiltViewModel
import io.soma.cryptobook.core.presentation.mvi.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class WatchlistViewModel @Inject constructor() :
    BaseViewModel<WatchlistContract.State, WatchlistContract.Event, WatchlistContract.Effect>(
        WatchlistContract.State(),
    ),
    WatchlistContract.ViewModel {
    override fun event(event: WatchlistContract.Event) {
    }
}
