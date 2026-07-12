package com.gtkim.mobile_access_control.component.master.domain.usecase

import com.gtkim.mobile_access_control.component.master.domain.model.MasterError
import com.gtkim.mobile_access_control.component.master.domain.repository.MasterDataRepository
import com.gtkim.mobile_access_control.core.common.result.Outcome
import javax.inject.Inject

interface SyncMasterDataUseCase {
    suspend operator fun invoke(): Outcome<Unit, MasterError>
}

internal class SyncMasterDataUseCaseImpl @Inject constructor(
    private val repository: MasterDataRepository,
) : SyncMasterDataUseCase {
    override suspend operator fun invoke(): Outcome<Unit, MasterError> = repository.sync()
}
