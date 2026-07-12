package com.gtkim.mobile_access_control.component.access.domain.model

/**
 * verify / register 요청에 쓰이는 카드 타입.
 *
 * 클라가 보낼 수 있는 값은 `FELICA` / `ISO_DEP` / `MOCK` 3종이다 — NDEF 는 평문·인증 없음으로
 * 사원증 검증에 부적합해 클라가 의도적으로 지원 제외 (architecture.md §2/§5). 응답의 cardType 은
 * 도메인 모델로 매핑하지 않으므로(ignoreUnknownKeys) 본 enum 은 요청 송신 전용이다.
 *
 * `:component:nfc` 의 `CardType` 과 명칭이 같지만 모듈 경계상 `:component:access` 는
 * `:component:nfc` 에 의존하지 않으므로 별도 정의한다.
 */
enum class CardType {
    FELICA,
    ISO_DEP,
    MOCK,
}
