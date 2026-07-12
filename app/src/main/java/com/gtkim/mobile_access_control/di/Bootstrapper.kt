package com.gtkim.mobile_access_control.di

import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.gtkim.mobile_access_control.component.auth.domain.model.AuthState
import com.gtkim.mobile_access_control.component.auth.domain.repository.AuthRepository
import com.gtkim.mobile_access_control.component.master.domain.repository.TerminalSettings
import com.gtkim.mobile_access_control.component.sync.data.worker.OfflineFlushWorker
import com.gtkim.mobile_access_control.component.sync.domain.provider.NetworkStateProvider
import com.gtkim.mobile_access_control.core.di.qualifier.AppScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 앱 시작 시점에 한 번 호출되어 백그라운드 작업을 결선한다.
 *
 * 현재 책임:
 *   - 토큰 캐시 선반영 (콜드스타트 race 방지)
 *   - 오프라인 큐 플러시: 네트워크 복구 순간 1회
 *   - 로그아웃 시 단말 선택 zone clear (architecture.md §17-A — 새 세션 = 미선택 디폴트)
 *
 * master 동기화 트리거는 본 클래스에서 하지 않는다:
 *   - 로그인 직후 1회 sync — `LoginViewModel` 이 `SyncMasterDataUseCase` 를 직접 await
 *     (검문 가용성 게이팅 — 캐시가 채워지기 전엔 검문 화면 진입하지 않음).
 *   - 운영자가 검문 화면에서 명시적으로 "동기화" 를 누른 경우 — `ScanViewModel` 이
 *     `MasterSyncScheduler.syncNow()` 와 `RequestOfflineFlushUseCase` 를 묶어서 호출.
 *
 * 주기적 백그라운드 sync 는 본 단말의 사용 패턴 (운영자 출퇴근 단위) 에 맞지 않아 채택하지 않았다.
 */
@Singleton
class Bootstrapper @Inject constructor(
    private val workManager: WorkManager,
    private val networkState: NetworkStateProvider,
    private val authRepository: AuthRepository,
    private val terminalSettings: TerminalSettings,
    @param:AppScope private val appScope: CoroutineScope,
) {
    fun start() {
        // 토큰 캐시 선반영은 백그라운드로 — Application.onCreate 를 막지 않는다.
        // 첫 인증 요청 전에 캐시가 채워지도록 부팅 시점에 트리거만 걸어둔다.
        appScope.launch { authRepository.ensureSessionLoaded() }
        observeNetworkAndFlush()
        observeLogoutAndClearZone()
    }

    private fun observeNetworkAndFlush() {
        appScope.launch {
            networkState.observe()
                .distinctUntilChanged()
                .filter { online -> online }
                .onEach {
                    Timber.i("Network restored — enqueuing OfflineFlushWorker")
                    workManager.enqueueUniqueWork(
                        OfflineFlushWorker.UNIQUE_NAME,
                        ExistingWorkPolicy.KEEP,
                        OfflineFlushWorker.buildRequest(),
                    )
                }
                .collect()
        }
    }

    /**
     * LoggedIn → LoggedOut 전이를 잡아 단말의 zone 선택값을 clear 한다. 콜드스타트 시 initial
     * LoggedOut emission 으로 의도치 않은 clear 가 일어나지 않도록 wasLoggedIn 가드.
     * authState 는 StateFlow 라 자체 distinct — 별도 distinctUntilChanged 불필요.
     */
    private fun observeLogoutAndClearZone() {
        appScope.launch {
            var wasLoggedIn = false
            authRepository.authState.collect { state ->
                when (state) {
                    is AuthState.LoggedIn -> wasLoggedIn = true
                    is AuthState.LoggedOut -> if (wasLoggedIn) {
                        wasLoggedIn = false
                        terminalSettings.clearZone()
                        Timber.i("Logout detected — cleared selected zone")
                    }
                }
            }
        }
    }
}
