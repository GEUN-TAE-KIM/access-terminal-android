package com.gtkim.mobile_access_control.feature.login.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gtkim.mobile_access_control.feature.login.R

/**
 * 오프라인 안내 배너 — errorContainer 색조로 차단 상태를 시각화. canSubmit 가드와 함께
 * 운영자에게 "왜 로그인 버튼이 비활성?" 의문에 즉답.
 */
@Composable
internal fun OfflineBanner() {
    Text(
        text = stringResource(R.string.login_offline_banner),
        color = MaterialTheme.colorScheme.onErrorContainer,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(8.dp),
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
    )
}
