package com.gtkim.mobile_access_control.component.master.domain

/**
 * 마스터 데이터 동기화 트리거.
 *
 * 호출자는 "동기화를 걸어야 한다"는 의도만 표현하고, 실행 방식(WorkManager, 즉시 코루틴 등)은
 * 구현체에 위임한다.
 *
 * 호출 지점:
 *   - 로그인 성공 직후는 [com.gtkim.mobile_access_control.component.master.domain.usecase.SyncMasterDataUseCase]
 *     를 직접 await 한다 (검문 가용성 게이팅 — feature/login/LoginViewModel 참조). 본 scheduler 미사용.
 *   - 운영자가 검문 화면에서 명시적으로 "동기화" 를 요청한 경우 [syncNow] 로 1회 enqueue
 *     (RequestOfflineFlushUseCase 와 함께 호출).
 *
 * 주기적 백그라운드 sync 는 본 단말의 사용 패턴 (운영자 출퇴근 단위) 과 맞지 않아 채택하지 않았다.
 * staleness 는 로그인 직후 sync + 운영자 수동 트리거 + 네트워크 복구 시 OfflineFlushWorker 의 부수
 * 효과로 충분하다고 본다.
 */
interface MasterSyncScheduler {
    /**
     * 마스터 동기화를 1회 비동기 예약한다. 즉시 반환하며 실제 완료를 기다리지 않는다.
     * WorkManager 의 KEEP 정책 — 이미 진행 중인 sync 가 있으면 중복 enqueue 무시.
     */
    fun syncNow()
}
