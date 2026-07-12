package com.gtkim.mobile_access_control.feature.login.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.gtkim.mobile_access_control.feature.common.ui.theme.AppTheme
import com.gtkim.mobile_access_control.feature.common.ui.component.AppButton
import com.gtkim.mobile_access_control.feature.common.ui.component.AppLoadingIndicator
import com.gtkim.mobile_access_control.feature.common.ui.dialog.AppDialog
import com.gtkim.mobile_access_control.feature.login.R
import com.gtkim.mobile_access_control.feature.login.ui.component.OfflineBanner
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun LoginScreen(onLoggedIn: () -> Unit) {
    val viewModel: LoginViewModel = hiltViewModel()
    val state by viewModel.collectAsState()

    viewModel.collectSideEffect { effect ->
        when (effect) {
            is LoginSideEffect.NavigateToScan -> onLoggedIn()
        }
    }

    LoginScaffold(state = state, onIntent = viewModel::onIntent)
}

@Composable
internal fun LoginScaffold(
    state: LoginUiState,
    onIntent: (LoginIntent) -> Unit,
) {
    // 로그인은 셸(AppShell) 밖 화면이라 감싸주는 Scaffold 가 없다. 자체 Scaffold 로 시스템 바
    // 인셋 패딩과 Material 배경을 확보한다 — edge-to-edge 에서 콘텐츠가 상태바와 겹치지 않도록.
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val idFocusRequester = remember { FocusRequester() }
    // 비밀번호 표시 토글은 화면 한정 일시 상태. rememberSaveable 로 회전 등에 유지.
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    // 입력 중 텍스트는 로컬 버퍼로 동기 소유한다 — Orbit reduce 가 비동기라 value 를 state 에 직접
    // 물리면 빠른 입력 시 커서가 튄다. 확정 로직(canSubmit/Submit)은 onIntent 포워딩으로 VM 이 유지.
    // 평문 비밀번호를 savedInstanceState 에 남기지 않도록 rememberSaveable 가 아닌 remember —
    // 회전 등 구성 변경엔 retained ViewModel 의 state 로 재초기화돼 입력이 보존된다.
    var idInput by remember { mutableStateOf(state.loginId) }
    var passwordInput by remember { mutableStateOf(state.password) }

    // TODO: 빈-입력 검증이 현재 View 의 canSubmit 가드에만 있다 (IME Done 경로 포함).
    //       단일-소스를 위해 LoginViewModel.submit() 에 재가드를 추가하고 이 가드는 UX 최적화로 격하.
    // Submit 발사를 키보드/버튼 양쪽에서 동일하게 처리. loading 중에는 무시.
    val submit: () -> Unit = {
        if (state.canSubmit) {
            keyboardController?.hide()
            onIntent(LoginIntent.Submit)
        }
    }

    Scaffold { padding ->
        // 폼과 로딩 인디케이터를 같은 Box 에 쌓아 history/stats 와 동일한 풀스크린 인디케이터 패턴을
        // 적용한다 — 로딩 중에는 폼이 disabled 인 채로 가운데 동그라미만 보이게.
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(stringResource(R.string.login_title))
                OutlinedTextField(
                    value = idInput,
                    onValueChange = {
                        idInput = it
                        onIntent(LoginIntent.IdChanged(it))
                    },
                    label = { Text(stringResource(R.string.login_field_id)) },
                    enabled = !state.loading,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(idFocusRequester),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next,
                        autoCorrectEnabled = false,
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) },
                    ),
                )
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = {
                        passwordInput = it
                        onIntent(LoginIntent.PasswordChanged(it))
                    },
                    label = { Text(stringResource(R.string.login_field_password)) },
                    enabled = !state.loading,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        val (icon, desc) = if (passwordVisible) {
                            Icons.Filled.VisibilityOff to stringResource(R.string.login_password_hide)
                        } else {
                            Icons.Filled.Visibility to stringResource(R.string.login_password_show)
                        }
                        IconButton(
                            onClick = { passwordVisible = !passwordVisible },
                            enabled = !state.loading,
                        ) {
                            Icon(imageVector = icon, contentDescription = desc)
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                        autoCorrectEnabled = false,
                    ),
                    keyboardActions = KeyboardActions(onDone = { submit() }),
                )
                AppButton(
                    text = stringResource(R.string.login_button_login),
                    onClick = submit,
                    enabled = state.canSubmit,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (!state.isOnline) {
                    OfflineBanner()
                }
            }

            if (state.loading) {
                AppLoadingIndicator(modifier = Modifier.fillMaxSize())
            }
        }
    }

    state.dialog?.let { dialog ->
        AppDialog(
            dialog = dialog,
            onDismiss = { onIntent(LoginIntent.DialogDismissed) },
            onConfirm = { onIntent(LoginIntent.DialogConfirmed) },
        )
    }
}

@Preview
@Composable
private fun LoginScaffoldPreview() {
    AppTheme {
        LoginScaffold(state = LoginUiState(), onIntent = {})
    }
}

