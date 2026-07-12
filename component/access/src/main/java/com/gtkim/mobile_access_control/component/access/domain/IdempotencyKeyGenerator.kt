package com.gtkim.mobile_access_control.component.access.domain

/**
 * verify 요청용 Idempotency-Key 발급기.
 *
 * 키 수명은 호출자(검문 세션)가 보유한다 — 한 검문당 한 키, 같은 검문의 네트워크 재시도는 동일 키
 * 재사용 (architecture.md §2). 본 generator 는 "새 키 1개" 생성만 책임지고, 언제 새로 만들지(= 새 검문
 * 시작 시점)는 호출자가 결정한다. UUID 생성이라는 비결정적 동작을 도메인 추상으로 빼 ViewModel 을
 * 순수 분기 로직으로 유지하고, 테스트에서 결정적 키를 주입할 수 있게 한다.
 */
interface IdempotencyKeyGenerator {
    fun newKey(): String
}
