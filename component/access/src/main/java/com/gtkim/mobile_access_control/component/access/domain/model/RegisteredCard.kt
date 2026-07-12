package com.gtkim.mobile_access_control.component.access.domain.model

import com.gtkim.mobile_access_control.core.model.CardUid

/**
 * `POST /api/v1/access/cards` 의 카드 등록 성공 결과 (API 명세 §4.2).
 *
 * 본 UI 는 등록 완료를 사용자에게 알리는 데에 user + cardUid 만 사용한다. wire 의
 * `cardId` / `cardType` / `isActive` / `issuedAt` 은 ignoreUnknownKeys 로 무시.
 */
data class RegisteredCard(
    /** 등록된 UID (서버에서 대소문자 정규화됨). */
    val cardUid: CardUid,
    /** 카드가 매핑된 사용자. */
    val user: AccessUser,
)
