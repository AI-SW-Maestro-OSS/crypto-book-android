package io.soma.cryptobook.search.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.soma.cryptobook.core.designsystem.theme.resource.CryptoDrawable
import io.soma.cryptobook.core.designsystem.theme.theme.CryptoTheme

@Composable
fun SearchListItem(
    symbol: String,
    imageUrl: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .background(color = CryptoTheme.colorScheme.background.secondary)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = imageUrl,
            placeholder = painterResource(CryptoDrawable.ic_coin_placeholder),
            error = painterResource(CryptoDrawable.ic_coin_placeholder),
            fallback = painterResource(CryptoDrawable.ic_coin_placeholder),
            contentDescription = "$symbol coin icon",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(40.dp),
        )

        Text(
            text = symbol,
            style = CryptoTheme.typography.titleMedium,
            color = CryptoTheme.colorScheme.text.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchListItemPreview() {
    SearchListItem(
        symbol = "BTCUSDT",
        imageUrl = "",
        onClick = {},
    )
}
