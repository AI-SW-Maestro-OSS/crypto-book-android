package io.soma.cryptobook.core.designsystem.component.appbar

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
import androidx.compose.ui.text.input.ImeAction
import io.soma.cryptobook.core.designsystem.component.appbar.color.cryptoTopAppBarColors
import io.soma.cryptobook.core.designsystem.component.button.CryptoStandardIconButton
import io.soma.cryptobook.core.designsystem.component.field.color.cryptoTextFieldColors
import io.soma.cryptobook.core.designsystem.theme.resource.CryptoDrawable
import io.soma.cryptobook.core.designsystem.theme.theme.CryptoTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryptoSearchTopAppBar(
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
        colors = cryptoTopAppBarColors(),
        navigationIcon = {
            navigationIcon?.let {
                CryptoStandardIconButton(
                    painter = it.navigationIcon,
                    contentDescription = it.navigationIconContentDescription,
                    onClick = it.onNavigationIconClick,
                    modifier = Modifier
                )
            }
        },
        title = {
            TextField(
                colors = cryptoTextFieldColors(),
                textStyle = CryptoTheme.typography.bodyLarge,
                placeholder = { Text(text = placeholder) },
                value = searchTerm,
                singleLine = true,
                onValueChange = onSearchTermChange,
                trailingIcon = {
                    CryptoStandardIconButton(
                        vectorIconRes = CryptoDrawable.ic_close,
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