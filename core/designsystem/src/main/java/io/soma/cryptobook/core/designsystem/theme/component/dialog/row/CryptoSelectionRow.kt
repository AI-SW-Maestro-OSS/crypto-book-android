package io.soma.cryptobook.core.designsystem.theme.component.dialog.row

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import io.soma.cryptobook.core.designsystem.theme.component.radio.CryptoRadioButton
import io.soma.cryptobook.core.designsystem.theme.theme.CbTheme
import io.soma.cryptobook.core.designsystem.util.Text

/**
 * Single-choice option row used inside [io.soma.cryptobook.core.designsystem.theme.component.dialog.CryptoSelectionDialog].
 *
 * A radio button on the left and the option label on the right. The whole row is
 * selectable; the [RadioButton] itself is not separately clickable.
 *
 * @param text Option label.
 * @param isSelected Whether this option is the currently selected one.
 * @param onClick Invoked when the row is tapped.
 * @param modifier Optional modifier for the row.
 */
@Composable
fun CryptoSelectionRow(
    text: Text,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CryptoRadioButton(
            modifier = Modifier.padding(16.dp),
            onClick = null,
            isSelected = isSelected
        )
        Text(
            text = text(),
            color = CbTheme.colorScheme.text.primary,
            style = CbTheme.typography.bodyLarge,
            modifier = Modifier
        )
    }
}
