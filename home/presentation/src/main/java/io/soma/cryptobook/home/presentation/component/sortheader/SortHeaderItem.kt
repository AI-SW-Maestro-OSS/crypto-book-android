package io.soma.cryptobook.home.presentation.component.sortheader

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.soma.cryptobook.core.designsystem.R
import io.soma.cryptobook.core.designsystem.theme.IconPrimary
import io.soma.cryptobook.core.designsystem.theme.Selectedicon
import io.soma.cryptobook.core.designsystem.theme.fontFamily

/**
 * Sort direction state
 */
enum class SortDirection {
    None,
    Asc,
    Desc,
}

/**
 * Sort header item component
 *
 * Figma element name: Table (SortHeader variant)
 * Figma element type: Instance
 * Figma node-id: 177:385
 *
 * Displays:
 * - Column label text (Symbol, Price ($), 24h Change %)
 * - Sort direction icon (Asc, Desc, None)
 *
 * Dependencies: None (leaf component)
 *
 * Layout: Row with text and sort icon
 * - Text: 14px, Bold, color varies by sort state
 * - Icon: 20x20dp sort indicator
 *
 * States:
 * - None: Default color (#F3F4F6), neutral sort icon
 * - Asc: Selected color (#0D59F2), ascending icon
 * - Desc: Selected color (#0D59F2), descending icon
 *
 * @param label Column label text
 * @param sortDirection Current sort direction
 * @param onClick Callback when header is clicked
 * @param modifier Optional modifier
 */
@Composable
fun SortHeaderItem(
    label: String,
    sortDirection: SortDirection,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val textColor = when (sortDirection) {
        SortDirection.None -> IconPrimary
        SortDirection.Asc, SortDirection.Desc -> Selectedicon
    }

    val iconRes = when (sortDirection) {
        SortDirection.None -> R.drawable.ic_sort_none
        SortDirection.Asc -> R.drawable.ic_sort_asc
        SortDirection.Desc -> R.drawable.ic_sort_desc
    }

    Row(
        modifier = modifier.clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            lineHeight = 20.sp,
            color = textColor,
        )
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = "Sort $label",
            tint = textColor,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun SortHeaderItemAscPreview() {
    SortHeaderItem(
        label = "Symbol",
        sortDirection = SortDirection.Asc,
        onClick = {},
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun SortHeaderItemDescPreview() {
    SortHeaderItem(
        label = "Symbol",
        sortDirection = SortDirection.Desc,
        onClick = {},
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun SortHeaderItemNonePreview() {
    SortHeaderItem(
        label = "Symbol",
        sortDirection = SortDirection.None,
        onClick = {},
    )
}
