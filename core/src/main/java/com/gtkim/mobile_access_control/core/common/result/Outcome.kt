package com.gtkim.mobile_access_control.core.common.result

/**
 * 도메인 연산의 성공/실패 결과를 표현하는 sealed 결과 타입.
 *
 * - [Success] 는 성공값 [T] 를 담고, [Failure] 는 도메인 에러 [E] ([AppError] 하위) 를 담는다.
 * - 호출 측은 `when (result) { is Success -> ...; is Failure -> ... }` 로 분기한다.
 *   exhaustive 분기를 강제하기 위해 `else` 를 쓰지 않는다.
 * - 에러 타입을 파라미터화한 이유: 모듈 내부의 sealed 도메인 에러 (예: [com.gtkim.mobile_access_control.core.common.result.AppError]
 *   를 상속한 AuthError/AccessError 등) 의 exhaustiveness 를 호출 시점까지 보존하기 위함.
 *   상위 [AppError] 는 멀티 모듈 경계 제약으로 plain interface 이므로, 단일 `Outcome<T>` +
 *   `Failure(AppError)` 로 두면 도메인별 when 분기가 exhaustive 하지 않게 된다.
 *
 * NFC 같은 비-API 결과도 같은 타입으로 감싸므로 이름은 도메인 중립적인 [Outcome] 으로 둔다.
 */
sealed interface Outcome<out T, out E : AppError> {
    data class Success<out T>(val data: T) : Outcome<T, Nothing>
    data class Failure<out E : AppError>(val error: E) : Outcome<Nothing, E>
}

/**
 * 예외 던지는 블록을 [Outcome] 으로 변환한다. data 레이어의 외부 호출 (Retrofit, Room 등) 진입점에서
 * Throwable 을 도메인 에러로 매핑할 때 사용. 직접 try/catch + Success/Failure 작성 보일러플레이트 제거.
 *
 * [kotlinx.coroutines.CancellationException] 은 catch 하지 않고 재던진다 — 코루틴 취소 시그널을
 * 비즈니스 실패로 둔갑시키지 않기 위함.
 */
inline fun <T, E : AppError> safeCall(
    onError: (Throwable) -> E,
    block: () -> T,
): Outcome<T, E> = try {
    Outcome.Success(block())
} catch (e: Throwable) {
    if (e is kotlinx.coroutines.CancellationException) throw e
    Outcome.Failure(onError(e))
}
