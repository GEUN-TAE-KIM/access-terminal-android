package com.gtkim.mobile_access_control.feature.register.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gtkim.mobile_access_control.feature.common.ui.nfc.NfcCardPromptIcon
import com.gtkim.mobile_access_control.feature.register.R
import com.gtkim.mobile_access_control.feature.common.R as CommonR

@Composable
internal fun ScanProgressDialog(
    registering: Boolean,
    onCancel: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(
                if (registering) {
                    stringResource(R.string.register_dialog_title_registering)
                } else {
                    stringResource(R.string.register_dialog_title_scanning)
                },
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (registering) {
                    CircularProgressIndicator()
                } else {
                    NfcCardPromptIcon()
                }
                Text(
                    text = if (registering) {
                        stringResource(R.string.register_dialog_body_registering)
                    } else {
                        stringResource(R.string.register_dialog_body_scanning)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            if (!registering) {
                TextButton(onClick = onCancel) { Text(stringResource(CommonR.string.common_cancel)) }
            }
        },
    )
}
