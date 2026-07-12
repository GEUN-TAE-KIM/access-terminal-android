package com.gtkim.mobile_access_control.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gtkim.mobile_access_control.component.auth.domain.model.AuthState
import com.gtkim.mobile_access_control.feature.login.ui.LoginScreen
import kotlinx.serialization.Serializable

/**
 * Type-safe Navigation 라우트 (Compose Navigation 2.9+). 문자열 경로 직접 다루지 않음.
 *
 * 2계층 중첩 구조 — 셸(Drawer + TopBar)이 메인 영역만 감싸도록 그래프를 분리한다:
 *  - Outer ([AccessNavGraph]): [LoginRoute](셸 없음) ↔ [MainRoute](셸 있음).
 *  - Inner ([AppShell] 내부 NavHost): [ScanRoute] / [RegisterRoute] / [HistoryRoute] / [StatsRoute].
 */
@Serializable
data object LoginRoute
@Serializable
data object MainRoute
@Serializable
data object ScanRoute
@Serializable
data object RegisterRoute
@Serializable
data object HistoryRoute
@Serializable
data object StatsRoute

/**
 * 최상위(outer) NavHost. 인증 영역([LoginRoute])과 셸이 감싸는 메인 영역([MainRoute])을 가른다.
 * 메인 화면(검문/히스토리/통계) 간 전환은 [AppShell] 의 inner NavHost 가 담당한다.
 *
 * 인증 라우팅은 **반응형** — [AccessNavViewModel.authState] (AuthRepository → TokenStorage 1:1)
 * 를 관찰해 [AuthState.LoggedOut] 진입 시 자동으로 [popToLogin] 한다. UI 화면이 콜백으로
 * 로그아웃을 트리거하던 prop drilling 을 제거.
 */
@Composable
fun AccessNavGraph(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val navViewModel: AccessNavViewModel = hiltViewModel()
    val authState by navViewModel.authState.collectAsStateWithLifecycle()

    // LoggedOut 전이 시 LoginRoute 로 복귀 — 단, **현재 destination 이 이미 LoginRoute 면 navigate 자체를
    // 호출하지 않는다**. 콜드스타트 시 initial value = LoggedOut 으로 LaunchedEffect 가 trigger 되는데,
    // 이때 navigate(LoginRoute) 를 부르면 같은 destination 이라도 NavController 가 backstack 을 settle
    // 하면서 첫 frame 깜빡임이 발생한다 (NavHost recomposition).
    LaunchedEffect(authState) {
        if (authState !is AuthState.LoggedOut) return@LaunchedEffect
        val alreadyAtLogin = navController.currentBackStackEntry
            ?.destination?.hasRoute(LoginRoute::class) == true
        if (!alreadyAtLogin) popToLogin(navController)
    }

    NavHost(
        navController = navController,
        startDestination = LoginRoute,
        modifier = modifier,
    ) {
        composable<LoginRoute> {
            LoginScreen(onLoggedIn = {
                navController.navigate(MainRoute) {
                    popUpTo(LoginRoute) { inclusive = true }
                }
            })
        }
        composable<MainRoute> {
            AppShell()
        }
    }
}

/**
 * 토큰 폐기 후 로그인 화면으로 복귀. 백스택의 [MainRoute] 와 그 inner NavHost 의 화면들을 함께 정리한다.
 * 호출자는 **현재 destination 이 LoginRoute 가 아닐 때만** 부른다 ([AccessNavGraph] 의 가드 참조) —
 * 콜드스타트 시 startDestination 과 같은 LoggedOut initial emission 으로 의미 없는 navigate 가 일어나는
 * 걸 막기 위함.
 */
private fun popToLogin(navController: NavHostController) {
    navController.navigate(LoginRoute) {
        popUpTo(MainRoute) { inclusive = true }
        launchSingleTop = true
    }
}
