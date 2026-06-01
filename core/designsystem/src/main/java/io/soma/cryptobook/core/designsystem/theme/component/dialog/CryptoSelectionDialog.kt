package io.soma.cryptobook.core.designsystem.theme.component.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import io.soma.cryptobook.core.designsystem.resource.CryptoString
import io.soma.cryptobook.core.designsystem.theme.component.button.CryptoTextButton
import io.soma.cryptobook.core.designsystem.theme.theme.CbTheme

@Composable
fun CryptoSelectionDialog(
    title: String,
    onDismissRequest: () -> Unit,
    selectionItems: @Composable ColumnScope.() -> Unit = {},
) {
    Dialog(
        onDismissRequest = onDismissRequest,
    ) {
        Column(
            modifier = Modifier
                .background(
                    color = CbTheme.colorScheme.background.primary,
                    shape = RoundedCornerShape(size = 28.dp)
                ),
            horizontalAlignment = Alignment.End,
        ) {
            Text(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                text = title,
                color = CbTheme.colorScheme.text.primary,
                style = CbTheme.typography.headlineSmall
            )
            Column(
                modifier = Modifier
                    .weight(1f, fill = false),
                content = selectionItems,
            )
            CryptoTextButton(
                modifier = Modifier
                    .padding(24.dp),
                label = stringResource(id = CryptoString.cancel),
                onClick = onDismissRequest,
            )
        }

    }
}
