package io.soma.cryptobook.search.presentation

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.soma.cryptobook.search.presentation.component.SearchListItem

@Composable
fun SearchContent(
    items: List<DisplayItem>,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
    ) {
        items(
            items = items,
            key = { it.symbol }
        ) { item ->
            SearchListItem(
                symbol = item.symbol,
                imageUrl = item.imageUrl,
                onClick = { onItemClick(item.symbol) }
            )
        }
    }
}