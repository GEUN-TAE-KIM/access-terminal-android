package com.gtkim.mobile_access_control.component.history.domain.model

/**
 * 출입 기록 한 페이지 (API 명세 §5.1 응답).
 */
data class LogPage(
    val items: List<AccessLog>,
    /** 다음 페이지 cursor. 마지막 페이지면 `null`. 클라는 디코딩하지 않고 그대로 다음 요청에 첨부한다. */
    val nextCursor: LogCursor?,
    /** 다음 페이지 존재 여부. */
    val hasMore: Boolean = false,
)
