package io.soma.cryptobook.core.designsystem.theme.component.radio

import android.widget.RadioButton
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.soma.cryptobook.core.designsystem.theme.component.radio.color.cryptoRadioButtonColors

@Composable
fun CryptoRadioButton(
    isSelected: Boolean,
    onClick: (() -> Unit)?,
    modifier : Modifier = Modifier
) {
    RadioButton(
        modifier = modifier,
        selected = isSelected,
        onClick = onClick,
        colors = cryptoRadioButtonColors(),
    )
}