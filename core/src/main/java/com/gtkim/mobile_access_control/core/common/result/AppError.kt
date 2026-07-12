package com.gtkim.mobile_access_control.core.common.result

import com.gtkim.mobile_access_control.core.common.error.UiText

/**
 * 모든 도메인 에러의 최상위 인터페이스.
 *
 * 원래 sealed 로 두고 싶었으나 멀티 모듈 경계 너머의 sealed 상속은 Kotlin 에서 금지되어 있어
 * 일반 interface 로 둔다. 각 component / core 모듈 내부의 하위 에러는 자체적으로
 * `sealed interface FooError : AppError` 로 유지하여 모듈 안에서는 when 분기가 exhaustive 하게 검증된다.
 *
 * [message] 가 [UiText] 인 이유: 도메인 에러도 사용자 노출 텍스트의 출처(strings.xml 리소스 vs
 * 서버 응답 raw vs 인터폴레이션 포맷)를 자체적으로 표현해야 한다. presentation 레이어의
 * ErrorMapper 는 이 값을 그대로 전달하거나 (대부분 케이스) 자기만의 톤으로 덮어쓸 수 있다.
 */
interface AppError {
    val message: UiText
}
