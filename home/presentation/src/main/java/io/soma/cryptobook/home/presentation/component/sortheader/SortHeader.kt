package io.soma.cryptobook.home.presentation.component.sortheader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.soma.cryptobook.core.designsystem.resource.CryptoString
import io.soma.cryptobook.core.designsystem.theme.theme.CbTheme

/**
 * Sort header container component
 *
 * Figma element name: Container
 * Figma element type: Frame
 * Figma node-id: 1:3
 *
 * Displays:
 * - Symbol sort header
 * - Price ($) sort header
 * - 24h Change % sort header
 *
 * Dependencies:
 * - [SortHeaderItem]
 *
 * Layout:
 * - Height: 45dp
 * - Full width with centered content
 * - Gap between items: 44dp
 *
 * TODO: 정렬 상태는 HomeViewModel에서 관리 예정
 *
 * @param symbolSort Symbol column sort direction
 * @param priceSort Price column sort direction
 * @param changeSort 24h Change column sort direction
 * @param onSymbolClick Callback when Symbol header is clicked
 * @param onPriceClick Callback when Price header is clicked
 * @param onChangeClick Callback when Change header is clicked
 * @param modifier Optional modifier
 */
@Composable
fun SortHeader(
    symbolSort: SortDirection,
    priceSort: SortDirection,
    changeSort: SortDirection,
    onSymbolClick: () -> Unit,
    onPriceClick: () -> Unit,
    onChangeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(45.dp)
            .background(CbTheme.colorScheme.background.secondary),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(44.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SortHeaderItem(
                label = stringResource(CryptoString.cb_home_sort_symbol),
                sortDirection = symbolSort,
                onClick = onSymbolClick,
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
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun SortHeaderPreview() {
    SortHeader(
        symbolSort = SortDirection.None,
        priceSort = SortDirection.Desc,
        changeSort = SortDirection.None,
        onSymbolClick = {},
        onPriceClick = {},
        onChangeClick = {},
    )
}
