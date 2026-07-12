package com.gtkim.mobile_access_control.component.auth.domain.usecase

import com.gtkim.mobile_access_control.component.auth.domain.repository.AuthRepository
import javax.inject.Inject

interface LogoutUseCase {
    suspend operator fun invoke()
}

internal class LogoutUseCaseImpl @Inject constructor(
    private val repository: AuthRepository,
) : LogoutUseCase {
    override suspend operator fun invoke() = repository.logout()
}
