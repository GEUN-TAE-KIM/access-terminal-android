package com.gtkim.mobile_access_control.component.access.data.remote

import com.gtkim.mobile_access_control.component.access.data.remote.dto.RegisterCardRequest
import com.gtkim.mobile_access_control.component.access.data.remote.dto.RegisterCardResponse
import com.gtkim.mobile_access_control.component.access.data.remote.dto.VerifyRequest
import com.gtkim.mobile_access_control.component.access.data.remote.dto.VerifyResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

internal interface AccessApi {

    /**
     * NFC 카드 검증 — `POST /api/v1/access/verify` (API 명세 §4.1).
     *
     * 출입 거부(`DENIED_*`)도 `200 OK` 로 응답되므로 [VerifyResponse] 로 받는다.
     * [idempotencyKey] 는 같은 비즈니스 요청에 동일 키를 유지한다 — 재시도 시 새로 만들지 않는다
     * (architecture.md §2). 호출자(검문 화면)가 검문 세션 단위로 생성·보관한다.
     */
    @POST("api/v1/access/verify")
    suspend fun verify(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body req: VerifyRequest,
    ): VerifyResponse

    /**
     * NFC 카드 등록 — `POST /api/v1/access/cards` (API 명세 §4.2). ADMIN role 한정.
     *
     * 멱등 처리 없음 — 같은 카드 재등록은 `409 CARD_ALREADY_REGISTERED` 로 명시 거부된다.
     * 인증·role 검증은 서버가 JWT 로 수행하며, 클라이언트는 `errorCode` 로 분기한다.
     */
    @POST("api/v1/access/cards")
    suspend fun registerCard(@Body req: RegisterCardRequest): RegisterCardResponse
}
