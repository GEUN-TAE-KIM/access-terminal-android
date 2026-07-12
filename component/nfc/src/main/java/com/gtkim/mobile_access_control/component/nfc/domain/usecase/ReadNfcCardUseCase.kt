package com.gtkim.mobile_access_control.component.nfc.domain.usecase

import android.nfc.Tag
import com.gtkim.mobile_access_control.component.nfc.domain.model.CardData
import com.gtkim.mobile_access_control.component.nfc.domain.model.NfcError
import com.gtkim.mobile_access_control.component.nfc.domain.service.NfcCardService
import com.gtkim.mobile_access_control.core.common.result.Outcome
import javax.inject.Inject

interface ReadNfcCardUseCase {
    suspend operator fun invoke(tag: Tag): Outcome<CardData, NfcError>
}

internal class ReadNfcCardUseCaseImpl @Inject constructor(
    private val service: NfcCardService,
) : ReadNfcCardUseCase {
    override suspend operator fun invoke(tag: Tag): Outcome<CardData, NfcError> = service.read(tag)
}
