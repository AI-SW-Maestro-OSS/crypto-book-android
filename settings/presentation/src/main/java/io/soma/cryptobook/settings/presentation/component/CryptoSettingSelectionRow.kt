package io.soma.cryptobook.settings.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import io.soma.cryptobook.core.designsystem.theme.component.dialog.CryptoSelectionDialog
import io.soma.cryptobook.core.designsystem.theme.component.dialog.row.CryptoSelectionRow
import io.soma.cryptobook.core.designsystem.util.asText

/**
 * A [CryptoSettingRow] that opens a single-choice [CryptoSelectionDialog] when tapped.
 *
 * Dialog visibility is held in local [rememberSaveable] state (the dialog is transient
 * UI, not part of the screen's ViewModel state). Selecting an option invokes
 * [onOptionSelected] and dismisses the dialog.
 *
 * @param title Setting name shown on the row.
 * @param dialogTitle Title shown at the top of the option dialog.
 * @param options Option labels, in display order.
 * @param selectedIndex Index of the currently selected option within [options].
 * @param onOptionSelected Invoked with the chosen option index.
 * @param modifier Optional modifier for the row.
 */
@Composable
fun CryptoSettingSelectionRow(
    title: String,
    dialogTitle: String,
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }

    CryptoSettingRow(
        modifier = modifier,
        title = title,
        value = options.getOrNull(selectedIndex).orEmpty(),
        onClick = { showDialog = true },
    )

    if (showDialog) {
        CryptoSelectionDialog(
            title = dialogTitle,
            onDismissRequest = { showDialog = false },
        ) {
            options.forEachIndexed { index, option ->
                CryptoSelectionRow(
                    text = option.asText(),
                    isSelected = index == selectedIndex,
                    onClick = {
                        onOptionSelected(index)
                        showDialog = false
                    },
                )
            }
        }
    }
}
