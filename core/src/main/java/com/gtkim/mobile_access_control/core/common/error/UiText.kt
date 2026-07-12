package com.gtkim.mobile_access_control.core.common.error

import androidx.annotation.StringRes

/**
 * 화면에 노출할 텍스트 추상화 — 정적 리소스 ID, 포맷 인자 있는 리소스, 동적 String 을 한 타입으로
 * 묶는다.
 *
 * `:core` 에 두는 이유: 도메인 에러([com.gtkim.mobile_access_control.core.common.result.AppError])
 * 의 `message` 필드 타입이 본 추상화여야 `:component:*` 의 sealed 에러가 strings.xml 리소스 ID 로
 * 메시지를 표현할 수 있다. 한편 `:core:network` 처럼 pure JVM 모듈은 Android 리소스를 가질 수
 * 없으므로 [Raw] 폴백을 활용한다.
 *
 * String 해석은 @Composable 진입점이 책임진다 (`:feature:common` 의 asString 확장 참조) — 본 모듈
 * 은 Compose 의존이 없다.
 */
sealed interface UiText {
    /**
     * 사전 정의된 [StringRes] — i18n 가능한 정적 메시지.
     *
     * `@param:` / `@get:` 명시는 KT-73255 — Kotlin 차기 버전부터 어노테이션이 param 뿐 아니라
     * field 까지 자동 적용되는데, 그 동작이 들어오기 전에 의도를 명확히 한다. lint 검사는 param
     * (호출 시점 검증) + getter (값 재사용 시 검증) 양쪽에서 의미가 있다.
     */
    data class Res(@param:StringRes @get:StringRes val id: Int) : UiText

    /** 포맷 인자를 받는 [StringRes] — i18n + 동적 값 결합. */
    data class FormattedRes(@param:StringRes @get:StringRes val id: Int, val args: List<Any>) : UiText {
        constructor(@StringRes id: Int, vararg args: Any) : this(id, args.toList())
    }

    /** 서버 응답 메시지 등 런타임에 결정되는 동적 문자열. */
    data class Raw(val value: String) : UiText
}
