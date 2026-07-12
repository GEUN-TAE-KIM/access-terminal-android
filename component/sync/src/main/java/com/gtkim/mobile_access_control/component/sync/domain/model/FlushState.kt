package com.gtkim.mobile_access_control.component.sync.domain.model

/**
 * 오프라인 큐 flush Worker 의 외부 노출 진행 상태.
 *
 * WorkManager `WorkInfo.State` 의 5종 (ENQUEUED/RUNNING/SUCCEEDED/FAILED/BLOCKED/CANCELLED) 를
 * 운영자가 의미 있게 인식하는 4종으로 단순화한다 — UI 가 Worker 상세를 알 필요는 없다.
 */
enum class FlushState {
    /** Worker 가 한 번도 실행된 적이 없거나 cancel 됨. UI 는 무관심. */
    Idle,

    /** ENQUEUED / BLOCKED / RUNNING — 진행 중 (대기 포함). */
    Running,

    /** 최근 사이클이 정상 완료. 큐는 비워졌을 수도, batch 단위 실패로 일부 남았을 수도 있다. */
    Succeeded,

    /** 최대 시도 횟수 초과로 종료. 큐가 그대로 남아 있다. */
    Failed,
}
