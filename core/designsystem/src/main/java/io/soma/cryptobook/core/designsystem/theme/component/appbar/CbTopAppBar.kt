package io.soma.cryptobook.core.designsystem.theme.component.appbar

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.union
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import io.soma.cryptobook.core.designsystem.R
import io.soma.cryptobook.core.designsystem.theme.component.appbar.color.cbTopAppBarColors
import io.soma.cryptobook.core.designsystem.theme.component.button.CbStandardIconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CbMediumTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets
        .union(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal)),
    navigationIcon: NavigationIcon? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    CenterAlignedTopAppBar(
        windowInsets = windowInsets,
        colors = cbTopAppBarColors(),
        navigationIcon = {
            navigationIcon?.let {
                CbStandardIconButton(
                    painter = it.navigationIcon,
                    contentDescription = it.navigationIconContentDescription,
                    onClick = it.onNavigationIconClick,
                    modifier = Modifier,
                )
            }
        },
        title = {
            Text(
                text = title,
                style = TextStyle(
                    fontSize = 19.sp,
                    lineHeight = 28.sp,
                    fontFamily = FontFamily(Font(R.font.pretendard_semibold)),
                    fontWeight = FontWeight.W600,
                    letterSpacing = 0.sp,
                    lineHeightStyle = LineHeightStyle(
                        alignment = LineHeightStyle.Alignment.Center,
                        trim = LineHeightStyle.Trim.None,
                    ),
                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                )
            )
        },
        modifier = modifier,
        actions = actions,
    )
}

data class NavigationIcon(
    val navigationIcon: Painter,
    val navigationIconContentDescription: String,
    val onNavigationIconClick: () -> Unit,
)