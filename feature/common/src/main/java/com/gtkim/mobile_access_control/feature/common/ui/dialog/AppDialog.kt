package com.gtkim.mobile_access_control.feature.common.ui.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gtkim.mobile_access_control.feature.common.R
import com.gtkim.mobile_access_control.feature.common.ui.error.asString

/**
 * 앱 전역 공용 다이얼로그 컴포저블. 디자인 통일 진입점.
 *
 * 호출 측은 자기 UiState 의 [DialogState] 를 그대로 넘기기만 하면 되고,
 * 다이얼로그 종류별 외관 분기는 본 컴포저블 내부에서 처리한다.
 *
 * 플랫폼 액션 (Context.startActivity 등) 은 본 컴포저블이 아닌 호출 화면의 SideEffect 핸들러에서 처리한다 —
 * 본 컴포저블은 화면별 UiState/Intent 에 종속되지 않도록 콜백만 노출한다.
 */
@Composable
fun AppDialog(
    dialog: DialogState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    when (dialog) {
        is DialogState.Error -> {
            val uiError = dialog.uiError
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(uiError.title.asString()) },
                text = { Text(uiError.message.asString()) },
                confirmButton = {
                    TextButton(onClick = onConfirm) {
                        Text(
                            uiError.confirmText?.asString()
                                ?: stringResource(R.string.common_confirm)
                        )
                    }
                },
            )
        }

        is DialogState.Confirm -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(dialog.title.asString()) },
                text = { Text(dialog.message.asString()) },
                confirmButton = {
                    TextButton(onClick = onConfirm) {
                        Text(
                            dialog.confirmLabel?.asString()
                                ?: stringResource(R.string.common_confirm)
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.common_cancel))
                    }
                },
            )
        }

        is DialogState.Info -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(dialog.title.asString()) },
                text = { Text(dialog.message.asString()) },
                confirmButton = {
                    TextButton(onClick = onConfirm) {
                        Text(stringResource(R.string.common_confirm))
                    }
                },
            )
        }
    }
}
