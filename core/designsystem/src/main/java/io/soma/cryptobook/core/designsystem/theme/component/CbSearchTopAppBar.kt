package io.soma.cryptobook.core.designsystem.theme.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.soma.cryptobook.core.designsystem.R
import io.soma.cryptobook.core.designsystem.resource.CryptoString
import io.soma.cryptobook.core.designsystem.theme.fontFamily
import io.soma.cryptobook.core.designsystem.theme.theme.CbTheme

// Hardcoded: not defined in Figma variables
private val SearchBarBackground = Color(0xFF2A2A2A)
private val SearchBarBorder = Color(0xFF323743)

/**
 * Search variant TopAppBar
 *
 * Figma element name: Property 1=Search
 * Figma element type: Frame
 * Figma node-id: 189:1301
 *
 * Displays a search bar with search icon and placeholder text.
 *
 * @param onSearchClick Callback when search bar is clicked
 * @param modifier Optional modifier
 */
@Composable
fun CbSearchTopAppBar(
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = WindowInsets.statusBars
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(CbTheme.colorScheme.background.primary)
            .windowInsetsPadding(windowInsets)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .background(SearchBarBackground, CircleShape)
                .border(2.dp, SearchBarBorder, CircleShape)
                .clickable(onClick = onSearchClick)
                .padding(12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_search),
                contentDescription = stringResource(CryptoString.cb_home_search_icon_cd),
                tint = CbTheme.colorScheme.text.secondary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = stringResource(CryptoString.cb_home_search_hint),
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                color = CbTheme.colorScheme.text.secondary
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun CbSearchTopAppBarPreview() {
    CbSearchTopAppBar(onSearchClick = {})
}
