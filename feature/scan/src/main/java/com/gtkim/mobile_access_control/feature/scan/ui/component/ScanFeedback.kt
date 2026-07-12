package com.gtkim.mobile_access_control.feature.scan.ui.component

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

private const val GRANTED_TONE_MS = 200
private const val DENIED_TONE_MS = 400
private const val GRANTED_VIBRATE_MS = 80L
private val DENIED_VIBRATE_PATTERN = longArrayOf(0L, 150L, 100L, 150L)

/**
 * 허가/거부 결과의 audio + haptic 피드백을 묶은 컨트롤러.
 * ToneGenerator/Vibrator 의 생성·dispose·SDK 분기는 [rememberScanFeedback] 이 책임진다.
 */
internal class ScanFeedback(
    private val toneGenerator: ToneGenerator,
    private val vibrator: Vibrator,
) {
    fun playGranted() {
        toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK, GRANTED_TONE_MS)
        vibrator.vibrateGranted()
    }

    fun playDenied() {
        toneGenerator.startTone(ToneGenerator.TONE_PROP_NACK, DENIED_TONE_MS)
        vibrator.vibrateDenied()
    }
}

@Composable
internal fun rememberScanFeedback(): ScanFeedback {
    val context = LocalContext.current
    val toneGenerator = remember {
        ToneGenerator(AudioManager.STREAM_NOTIFICATION, ToneGenerator.MAX_VOLUME)
    }

    DisposableEffect(toneGenerator) {
        onDispose { toneGenerator.release() }
    }

    val vibrator = remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    return remember(toneGenerator, vibrator) { ScanFeedback(toneGenerator, vibrator) }
}

// VIBRATE 는 normal permission 이라 Manifest 선언만으로 install 시 자동 부여된다. 런타임 권한 체크가
// 불필요하므로 lint 의 MissingPermission 만 한 곳에서 suppress 하고 호출부는 깔끔하게 둔다.
@SuppressLint("MissingPermission")
private fun Vibrator.vibrateGranted() {
    vibrate(VibrationEffect.createOneShot(GRANTED_VIBRATE_MS, VibrationEffect.DEFAULT_AMPLITUDE))
}

@SuppressLint("MissingPermission")
private fun Vibrator.vibrateDenied() {
    vibrate(VibrationEffect.createWaveform(DENIED_VIBRATE_PATTERN, -1))
}
