package io.soma.cryptobook.core.designsystem.theme.component.coinlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.soma.cryptobook.core.designsystem.resource.CryptoString

@Composable
fun SortHeader(
    symbolSort: SortDirection,
    priceSort: SortDirection,
    changeSort: SortDirection,
    volumeSort: SortDirection,
    onSymbolClick: () -> Unit,
    onPriceClick: () -> Unit,
    onChangeClick: () -> Unit,
    onVolumeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SortHeaderItem(
            label = stringResource(CryptoString.cb_home_sort_symbol),
            sortDirection = symbolSort,
            onClick = onSymbolClick,
        )
        SortHeaderItem(
            label = stringResource(CryptoString.cb_home_sort_volume),
            sortDirection = volumeSort,
            onClick = onVolumeClick,
        )
        SortHeaderItem(
            label = stringResource(CryptoString.cb_home_sort_price),
            sortDirection = priceSort,
            onClick = onPriceClick,
        )
        SortHeaderItem(
            label = stringResource(CryptoString.cb_home_sort_change_24h),
            sortDirection = changeSort,
            onClick = onChangeClick,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun SortHeaderPreview() {
    SortHeader(
        symbolSort = SortDirection.None,
        priceSort = SortDirection.Desc,
        changeSort = SortDirection.None,
        volumeSort = SortDirection.None,
        onSymbolClick = {},
        onPriceClick = {},
        onChangeClick = {},
        onVolumeClick = {},
    )
}
