package io.soma.cryptobook.search.domain.usecase

import io.soma.cryptobook.search.domain.model.SearchCoin
import javax.inject.Inject

class FilterSearchCoinsUseCase @Inject constructor() {
    operator fun invoke(searchTerm: String, coins: List<SearchCoin>): List<SearchCoin> {
        val query = searchTerm.trim()
        if (query.isBlank()) return emptyList()

        return coins
            .filter { it.symbol.contains(query, ignoreCase = true) }
            .sortedWith(
                compareBy<SearchCoin> {
                    !it.symbol.startsWith(query, ignoreCase = true)
                }.thenBy { it.symbol },
            )
    }
}
