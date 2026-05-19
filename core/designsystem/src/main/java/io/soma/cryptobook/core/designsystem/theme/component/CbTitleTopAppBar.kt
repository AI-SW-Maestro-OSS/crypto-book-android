package io.soma.cryptobook.core.designsystem.theme.component

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import io.soma.cryptobook.core.designsystem.resource.CryptoString
import io.soma.cryptobook.core.designsystem.theme.ScreenBackground
import io.soma.cryptobook.core.designsystem.theme.fontFamily
import io.soma.cryptobook.core.designsystem.theme.textPrimary

/**
 * Settings variant TopAppBar
 *
 * Figma element name: Property 1=Settings
 * Figma element type: Frame
 * Figma node-id: 189:1313
 *
 * Displays a centered title.
 *
 * @param title The title to display (e.g., "Settings")
 * @param modifier Optional modifier
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CbTitleTopAppBar(
    title: String,
    modifier: Modifier = Modifier
) {
    TopAppBar(
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
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = ScreenBackground
        ),
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun CbTitleTopAppBarPreview() {
    CbTitleTopAppBar(title = stringResource(CryptoString.cb_settings_title))
}