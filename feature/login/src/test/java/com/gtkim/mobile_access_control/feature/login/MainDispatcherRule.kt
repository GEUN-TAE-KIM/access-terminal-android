package com.gtkim.mobile_access_control.feature.login

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * viewModelScope(Dispatchers.Main) 위에서 도는 Orbit container 코루틴을 runTest 가상시간으로
 * 제어 가능하게 만드는 JUnit4 TestRule. Orbit ContainerHost 가 ViewModel 이라 container 가
 * viewModelScope 를 쓰므로, 단위 테스트에선 Main 디스패처를 TestDispatcher 로 갈아끼워야 한다.
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
