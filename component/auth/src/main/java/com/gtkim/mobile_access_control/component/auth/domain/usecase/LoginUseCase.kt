package com.gtkim.mobile_access_control.component.auth.domain.usecase

import com.gtkim.mobile_access_control.component.auth.domain.model.Admin
import com.gtkim.mobile_access_control.component.auth.domain.model.AuthError
import com.gtkim.mobile_access_control.component.auth.domain.repository.AuthRepository
import com.gtkim.mobile_access_control.core.common.result.Outcome
import javax.inject.Inject

interface LoginUseCase {
    suspend operator fun invoke(loginId: String, password: String): Outcome<Admin, AuthError>
}

internal class LoginUseCaseImpl @Inject constructor(
    private val repository: AuthRepository,
) : LoginUseCase {
    override suspend operator fun invoke(
        loginId: String,
        password: String
    ): Outcome<Admin, AuthError> =
        repository.login(loginId, password)
}
