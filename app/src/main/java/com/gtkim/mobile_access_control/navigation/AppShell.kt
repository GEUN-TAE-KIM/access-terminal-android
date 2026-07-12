package com.gtkim.mobile_access_control.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Contactless
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gtkim.mobile_access_control.R
import com.gtkim.mobile_access_control.feature.common.ui.dialog.AppDialog
import com.gtkim.mobile_access_control.feature.common.ui.scaffold.AppDrawer
import com.gtkim.mobile_access_control.feature.common.ui.scaffold.AppDrawerDestination
import com.gtkim.mobile_access_control.feature.common.ui.scaffold.AppTopBar
import com.gtkim.mobile_access_control.feature.common.ui.scaffold.OfflineBadge
import com.gtkim.mobile_access_control.feature.common.ui.scaffold.SyncAction
import com.gtkim.mobile_access_control.feature.history.ui.HistoryScreen
import com.gtkim.mobile_access_control.feature.register.ui.RegisterScreen
import com.gtkim.mobile_access_control.feature.scan.ui.ScanScreen
import com.gtkim.mobile_access_control.feature.stats.ui.StatsScreen
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

private const val KEY_SCAN = "scan"
private const val KEY_REGISTER = "register"
private const val KEY_HISTORY = "history"
private const val KEY_STATS = "stats"

/**
 * 메인 셸 — Drawer + TopAppBar + inner NavHost 를 조립한다.
 *
 * [MainRoute] 목적지에서만 렌더되므로 로그인 화면을 신경 쓸 필요가 없다(셸 표시 분기 없음).
 * inner NavHost 가 검문/카드 등록/히스토리/통계 4탭을 호스팅하고, 셸의 Scaffold/Drawer 가 그 inner
 * NavHost 만 감싼다.
 *
 * 라우팅 객체는 :app 모듈 소유 — :feature:common 의 [AppDrawer]/[AppTopBar] 는 라우트를 모르는
 * stateless 컴포저블이라 destination ↔ 라우트 매핑은 본 셸이 책임진다.
 *
 * 인증 라우팅(세션 만료 → 로그인) 콜백은 받지 않는다 — [AccessNavGraph] 가
 * `AuthRepository.authState` 를 직접 관찰하므로 본 셸과 inner NavHost 화면들은 인증 흐름과 분리된다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppShell(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination
    val shellViewModel: AppShellViewModel = hiltViewModel()
    val shellState by shellViewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val flushSucceededMsg = stringResource(R.string.shell_snackbar_flush_succeeded)
    val flushFailedMsg = stringResource(R.string.shell_snackbar_flush_failed)
    val queueOverflowMsg = stringResource(R.string.shell_snackbar_queue_overflow)
    val queueDeadLetterMsg = stringResource(R.string.shell_snackbar_queue_dead_letter)

    shellViewModel.collectSideEffect { effect ->
        when (effect) {
            is AppShellSideEffect.ShowFlushSucceeded -> scope.launch {
                snackbarHostState.showSnackbar(flushSucceededMsg)
            }

            is AppShellSideEffect.ShowFlushFailed -> scope.launch {
                snackbarHostState.showSnackbar(flushFailedMsg)
            }

            is AppShellSideEffect.ShowQueueOverflow -> scope.launch {
                snackbarHostState.showSnackbar(queueOverflowMsg)
            }

            is AppShellSideEffect.ShowQueueDeadLetter -> scope.launch {
                snackbarHostState.showSnackbar(queueDeadLetterMsg)
            }
        }
    }

    // 라벨은 stringResource 로 해석해 destinations 를 매 recomposition 마다 새로 구성한다 —
    // remember 로 묶지 않는 이유: stringResource 는 @Composable 이므로 remember 블록 안에서 호출
    // 불가. recomposition 비용은 4-요소 list 한 번이라 무시 가능.
    val destinations = listOf(
        AppDrawerDestination(
            KEY_SCAN,
            stringResource(R.string.shell_destination_scan),
            Icons.Filled.Contactless
        ),
        AppDrawerDestination(
            KEY_REGISTER,
            stringResource(R.string.shell_destination_register),
            Icons.Filled.AddCard
        ),
        AppDrawerDestination(
            KEY_HISTORY,
            stringResource(R.string.shell_destination_history),
            Icons.Filled.History
        ),
        AppDrawerDestination(
            KEY_STATS,
            stringResource(R.string.shell_destination_stats),
            Icons.Filled.BarChart
        ),
    )
    // 백스택이 아직 비어 있는 첫 프레임에는 inner NavHost 의 시작 목적지(검문)를 기본값으로.
    val selectedKey = destination?.toShellKey() ?: KEY_SCAN
    val title = destinations.firstOrNull { it.key == selectedKey }?.label.orEmpty()

    val drawerState = rememberDrawerState(DrawerValue.Closed)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                destinations = destinations,
                selectedKey = selectedKey,
                onSelect = { dest ->
                    scope.launch { drawerState.close() }
                    navigateToShellDestination(navController, dest.key)
                },
                onLogout = {
                    scope.launch { drawerState.close() }
                    shellViewModel.onIntent(AppShellIntent.Logout)
                },
            )
        },
        modifier = modifier,
    ) {
        Scaffold(
            topBar = {
                AppTopBar(
                    title = title,
                    onMenuClick = { scope.launch { drawerState.open() } },
                    actions = {
                        if (!shellState.isOnline) {
                            OfflineBadge()
                        }
                        if (shellState.pendingCount > 0) {
                            SyncAction(
                                pendingCount = shellState.pendingCount,
                                onClick = { shellViewModel.onIntent(AppShellIntent.RequestSync) },
                            )
                        }
                    },
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = ScanRoute,
                modifier = Modifier.padding(padding),
                enterTransition = { fadeIn(tween(durationMillis = 220, delayMillis = 90)) },
                exitTransition = { fadeOut(tween(durationMillis = 90)) },
                popEnterTransition = { fadeIn(tween(durationMillis = 220, delayMillis = 90)) },
                popExitTransition = { fadeOut(tween(durationMillis = 90)) },
            ) {
                composable<ScanRoute> { ScanScreen() }
                composable<RegisterRoute> { RegisterScreen() }
                composable<HistoryRoute> { HistoryScreen() }
                composable<StatsRoute> { StatsScreen() }
            }
        }
    }

    shellState.dialog?.let { dialog ->
        AppDialog(
            dialog = dialog,
            onDismiss = { shellViewModel.onIntent(AppShellIntent.DismissDialog) },
            onConfirm = { shellViewModel.onIntent(AppShellIntent.ConfirmSync) },
        )
    }
}

private fun NavDestination.toShellKey(): String? = when {
    hasRoute(ScanRoute::class) -> KEY_SCAN
    hasRoute(RegisterRoute::class) -> KEY_REGISTER
    hasRoute(HistoryRoute::class) -> KEY_HISTORY
    hasRoute(StatsRoute::class) -> KEY_STATS
    else -> null
}

/**
 * 검문이 메인 화면이므로 [ScanRoute] 를 anchor 로 두고 popUpTo + saveState 로 백스택을 정돈한다.
 * launchSingleTop/restoreState 로 같은 destination 재진입 시 새 인스턴스가 쌓이지 않게 한다.
 */
private fun navigateToShellDestination(navController: NavHostController, key: String) {
    val route: Any = when (key) {
        KEY_SCAN -> ScanRoute
        KEY_REGISTER -> RegisterRoute
        KEY_HISTORY -> HistoryRoute
        KEY_STATS -> StatsRoute
        else -> return
    }
    navController.navigate(route) {
        popUpTo(ScanRoute) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
