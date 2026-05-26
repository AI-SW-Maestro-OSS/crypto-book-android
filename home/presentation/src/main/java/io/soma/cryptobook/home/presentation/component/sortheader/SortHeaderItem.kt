package io.soma.cryptobook.home.presentation.component.sortheader

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.soma.cryptobook.core.designsystem.resource.CryptoString
import io.soma.cryptobook.core.designsystem.theme.resource.CbDrawable
import io.soma.cryptobook.core.designsystem.theme.theme.CbTheme

/**
 * Sort direction state
 */
enum class SortDirection {
    None,
    Asc,
    Desc,
}

@Composable
fun SortHeaderItem(
    label: String,
    sortDirection: SortDirection,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = CbTheme.typography.labelMedium,
            color = CbTheme.colorScheme.text.primary,
        )
        Box(modifier = Modifier.size(16.dp)) {
            // 베이스: 양쪽 화살표, 항상 표시 (흐리게)
            Icon(
                painter = painterResource(CbDrawable.ic_sort_none),
                contentDescription = stringResource(
                    CryptoString.cb_home_sort_icon_cd_format,
                    label,
                ),
                tint = CbTheme.colorScheme.background.selected, // dimmed 기본색
                modifier = Modifier.matchParentSize(),
            )
            // 오버레이: 활성 방향만 강조
            val overlayRes = when (sortDirection) {
                SortDirection.Asc -> CbDrawable.ic_sort_asc
                SortDirection.Desc -> CbDrawable.ic_sort_desc
                SortDirection.None -> null
            }
            overlayRes?.let {
                Icon(
                    painter = painterResource(it),
                    contentDescription = null,
                    tint = CbTheme.colorScheme.text.primary,
                    modifier = Modifier.matchParentSize(),
                )
            }
        }
    }
}

@Preview
@Composable
private fun SortHeaderItemAscPreview() {
    SortHeaderItem(
        label = stringResource(CryptoString.cb_home_sort_symbol),
        sortDirection = SortDirection.Asc,
        onClick = {},
    )
}

@Preview
@Composable
private fun SortHeaderItemDescPreview() {
    SortHeaderItem(
        label = stringResource(CryptoString.cb_home_sort_symbol),
        sortDirection = SortDirection.Desc,
        onClick = {},
    )
}

@Preview
@Composable
private fun SortHeaderItemNonePreview() {
    SortHeaderItem(
        label = stringResource(CryptoString.cb_home_sort_symbol),
        sortDirection = SortDirection.None,
        onClick = {},
    )
}
