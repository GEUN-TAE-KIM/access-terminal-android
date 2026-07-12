package com.gtkim.mobile_access_control.feature.scan

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * viewModelScope (Dispatchers.Main.immediate) 위에서 도는 코루틴을 runTest 가상시간으로
 * 제어 가능하게 만드는 JUnit4 TestRule.
 *
 * ScanViewModel 의 dwellJob 이 viewModelScope.launch 로 분리되어 있어서 — Orbit 인텐트 큐를
 * 점유하지 않고 다음 태깅을 즉시 받기 위함 — 테스트에선 Main 디스패처를 TestDispatcher 로
 * 갈아끼워야 delay(RESULT_DWELL_MS) 가 가상시간 단위로 처리된다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
