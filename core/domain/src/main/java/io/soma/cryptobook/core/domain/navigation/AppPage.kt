package io.soma.cryptobook.core.domain.navigation

sealed class AppPage {
    data object Home : AppPage()
    data class CoinDetail(val coinName: String) : AppPage()
    data object Settings : AppPage()
    data object Search : AppPage()
}
