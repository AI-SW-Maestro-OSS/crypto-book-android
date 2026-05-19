package io.soma.cryptobook.core.designsystem.theme.component

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.soma.cryptobook.core.designsystem.R
import io.soma.cryptobook.core.designsystem.resource.CryptoString
import io.soma.cryptobook.core.designsystem.theme.IconPrimary
import io.soma.cryptobook.core.designsystem.theme.ScreenBackground
import io.soma.cryptobook.core.designsystem.theme.fontFamily
import io.soma.cryptobook.core.designsystem.theme.textPrimary

/**
 * CoinDetail variant TopAppBar
 *
 * Figma element name: Property 1=CoinDetail
 * Figma element type: Frame
 * Figma node-id: 189:1307
 *
 * Displays back button, title, search and favorite icons.
 *
 * @param title The title to display (e.g., "Bitcoin – BTCUSDT")
 * @param onBackClick Callback when back button is clicked
 * @param onSearchClick Callback when search icon is clicked
 * @param onFavoriteClick Callback when favorite icon is clicked
 * @param modifier Optional modifier
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CbDetailTopAppBar(
    title: String,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                style = TextStyle(
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    lineHeight = 20.sp,
                    color = textPrimary
                )
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = stringResource(CryptoString.cb_coin_detail_back_cd),
                    tint = IconPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        actions = {
            IconButton(onClick = onSearchClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_search),
                    contentDescription = stringResource(CryptoString.cb_coin_detail_search_cd),
                    tint = IconPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
            IconButton(onClick = onFavoriteClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_favorite),
                    contentDescription = stringResource(CryptoString.cb_coin_detail_favorite_cd),
                    tint = IconPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = ScreenBackground
        ),
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun CbDetailTopAppBarPreview() {
    CbDetailTopAppBar(
        title = "Bitcoin – BTCUSDT",
        onBackClick = {},
        onSearchClick = {},
        onFavoriteClick = {}
    )
}
