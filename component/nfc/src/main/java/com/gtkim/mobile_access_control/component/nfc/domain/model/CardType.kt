package com.gtkim.mobile_access_control.component.nfc.domain.model

/**
 * 지원 카드 타입.
 *
 * 평문·인증 없는 NDEF 는 사원증 검증 용도로 부적합하므로 제외했다.
 * 사원증은 일반적으로 FeliCa(일본 환경) 또는 IsoDep(ISO 14443-4 with secure messaging,
 * MIFARE DESFire 등)로 발급된다.
 */
enum class CardType {
    FELICA,
    ISO_DEP,
    MOCK,
}
