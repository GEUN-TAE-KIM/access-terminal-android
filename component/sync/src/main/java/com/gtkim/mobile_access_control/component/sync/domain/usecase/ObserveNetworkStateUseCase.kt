package com.gtkim.mobile_access_control.component.sync.domain.usecase

import com.gtkim.mobile_access_control.component.sync.domain.provider.NetworkStateProvider
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 단말의 온라인/오프라인 상태를 관찰한다. UI(로그인 차단 가드·오프라인 배지) 표시용.
 *
 * ViewModel 이 [NetworkStateProvider] 를 직접 주입받지 않고 UseCase 를 경유하기 위한 얇은 래퍼
 * (architecture.md §4 — ViewModel → UseCase → provider 단방향). 동기 `isOnline()` 은 검문 핫패스의
 * [VerifyAccessUseCase] 가 직접 쓰므로 본 UseCase 는 관찰(observe) 만 노출한다.
 */
interface ObserveNetworkStateUseCase {
    operator fun invoke(): Flow<Boolean>
}

internal class ObserveNetworkStateUseCaseImpl @Inject constructor(
    private val provider: NetworkStateProvider,
) : ObserveNetworkStateUseCase {
    override operator fun invoke(): Flow<Boolean> = provider.observe()
}
