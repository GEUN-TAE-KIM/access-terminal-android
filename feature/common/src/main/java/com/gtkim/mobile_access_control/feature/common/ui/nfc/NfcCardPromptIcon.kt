package com.gtkim.mobile_access_control.feature.common.ui.nfc

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contactless
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * NFC 카드를 대달라고 안내하는 펄스 아이콘.
 *
 * 검문 화면과 카드 등록 화면이 "카드를 대주세요" 단계에서 공통으로 사용한다 — 사용자가 두 화면을
 * 오가도 같은 시각 언어로 같은 동작(카드 터치 대기)을 인식할 수 있도록 통일.
 */
@Composable
fun NfcCardPromptIcon(
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
) {
    val transition = rememberInfiniteTransition(label = "nfc-prompt-pulse")
    val scale by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "nfc-prompt-pulse-scale",
    )
    val alpha by transition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "nfc-prompt-pulse-alpha",
    )
    Icon(
        imageVector = Icons.Filled.Contactless,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .size(size)
            .scale(scale)
            .alpha(alpha),
    )
}
