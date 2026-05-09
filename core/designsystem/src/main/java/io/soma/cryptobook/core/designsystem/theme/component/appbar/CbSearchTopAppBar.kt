package io.soma.cryptobook.core.designsystem.theme.component.appbar

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import io.soma.cryptobook.core.designsystem.R
import io.soma.cryptobook.core.designsystem.theme.component.appbar.color.cbTopAppBarColors
import io.soma.cryptobook.core.designsystem.theme.component.button.CbStandardIconButton
import io.soma.cryptobook.core.designsystem.theme.component.field.color.cbTextFieldColors
import io.soma.cryptobook.core.designsystem.theme.resource.CbDrawable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CbSearchTopAppBar(
    searchTerm: String,
    placeholder: String,
    onSearchTermChange: (String) -> Unit,
    navigationIcon: NavigationIcon?,
    clearIconContentDescription: String,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets
        .union(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal)),
    autoFocus: Boolean = true,
) {
    val focusRequester = remember { FocusRequester() }
    TopAppBar(
        modifier = modifier,
        windowInsets = windowInsets,
        colors = cbTopAppBarColors(),
        navigationIcon = {
            navigationIcon?.let {
                CbStandardIconButton(
                    painter = it.navigationIcon,
                    contentDescription = it.navigationIconContentDescription,
                    onClick = it.onNavigationIconClick,
                    modifier = Modifier
                )
            }
        },
        title = {
            TextField(
                colors = cbTextFieldColors(),
                textStyle = TextStyle(
                    fontSize = 15.sp,
                    lineHeight = 20.sp,
                    fontFamily = FontFamily(Font(R.font.pretendard_regular)),
                    fontWeight = FontWeight.W400,
                    letterSpacing = 0.sp,
                    lineHeightStyle = LineHeightStyle(
                        alignment = LineHeightStyle.Alignment.Center,
                        trim = LineHeightStyle.Trim.None,
                    ),
                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                ),
                placeholder = { Text(text = placeholder) },
                value = searchTerm,
                singleLine = true,
                onValueChange = onSearchTermChange,
                trailingIcon = {
                    CbStandardIconButton(
                        vectorIconRes = CbDrawable.ic_close,
                        contentDescription = clearIconContentDescription,
                        onClick = { onSearchTermChange("") }
                    )
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .fillMaxWidth(),
            )
        },
    )
    if (autoFocus) {
        LaunchedEffect(Unit) { focusRequester.requestFocus() }
    }
}