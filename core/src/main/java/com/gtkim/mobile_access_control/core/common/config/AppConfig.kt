package com.gtkim.mobile_access_control.core.common.config

/** BuildConfig 의존을 추상화. 각 build flavor 에서 주입한다. */
interface AppConfig {
    val baseUrl: String
    val isDebug: Boolean
    val terminalId: String
}
