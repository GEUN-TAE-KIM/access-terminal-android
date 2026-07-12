package com.gtkim.mobile_access_control.component.scan.data

import com.gtkim.mobile_access_control.component.access.domain.model.CardType as AccessCardType
import com.gtkim.mobile_access_control.component.nfc.domain.model.CardType as NfcCardType

/**
 * `:component:nfc` 의 카드 타입을 verify/register 요청용 `:component:access` 카드 타입으로 변환.
 *
 * 두 도메인의 `CardType` 은 모듈 경계상 의도적으로 분리돼 있다 (access 는 nfc 에 의존하지 않음,
 * access/CardType.kt 주석). 그 경계를 잇는 anti-corruption 매핑을 본 orchestration 모듈에 단 한 곳
 * 두어, 이전처럼 각 feature ViewModel 에 중복 작성되지 않게 한다.
 */
internal fun NfcCardType.toAccessCardType(): AccessCardType = when (this) {
    NfcCardType.FELICA -> AccessCardType.FELICA
    NfcCardType.ISO_DEP -> AccessCardType.ISO_DEP
    NfcCardType.MOCK -> AccessCardType.MOCK
}
