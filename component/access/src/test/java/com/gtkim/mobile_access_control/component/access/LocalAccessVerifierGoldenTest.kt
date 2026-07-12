package com.gtkim.mobile_access_control.component.access

import com.gtkim.mobile_access_control.component.access.data.LocalAccessVerifierImpl
import com.gtkim.mobile_access_control.component.access.domain.model.AccessDecision
import com.gtkim.mobile_access_control.component.master.domain.model.CachedCard
import com.gtkim.mobile_access_control.component.master.domain.model.CachedPermission
import com.gtkim.mobile_access_control.component.master.domain.model.CachedUser
import com.gtkim.mobile_access_control.component.master.domain.model.CachedZone
import com.gtkim.mobile_access_control.component.master.domain.model.MasterError
import com.gtkim.mobile_access_control.component.master.domain.repository.MasterDataRepository
import com.gtkim.mobile_access_control.core.common.result.Outcome
import com.gtkim.mobile_access_control.core.common.time.TimeProvider
import com.gtkim.mobile_access_control.core.model.CardUid
import com.gtkim.mobile_access_control.core.model.EmployeeCode
import com.gtkim.mobile_access_control.core.model.Zone
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

/**
 * Drift detection — client [LocalAccessVerifierImpl] 가 같은 JSON 매트릭스를 server
 * `PermissionEvaluator` 와 동일하게 평가하는지 검증한다. (hybrid-offline.md §2.3 / api-spec §8.1 raw
 * 필드 정책.)
 *
 * 같은 cases.json 이 access-terminal-server repo 의 `src/test/resources/golden/access-evaluation/`
 * 에도 복사되어 server 측 `PermissionEvaluatorGoldenTest` 가 동일 매트릭스를 평가한다.
 * 한 쪽이 spec drift 면 양쪽 테스트 중 하나가 실패한다.
 */
class LocalAccessVerifierGoldenTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Test
    fun `evaluator matches golden matrix`() = runTest {
        val raw = checkNotNull(
            javaClass.classLoader?.getResourceAsStream("golden/access-evaluation/cases.json"),
        ) { "cases.json not on classpath" }.bufferedReader().use { it.readText() }
        val matrix = json.decodeFromString<Matrix>(raw)

        matrix.cases.forEach { case ->
            val verifier = LocalAccessVerifierImpl(
                masterData = FakeMasterData(case.input),
                time = FixedTimeProvider(Instant.parse(case.input.atIso)),
            )
            val outcome = verifier.verify(CardUid(CARD_UID), Zone(case.input.zone))
            val actualDecision = when (outcome) {
                is Outcome.Failure -> outcome.error.javaClass.simpleName
                is Outcome.Success -> outcome.data.decision.name
            }
            assertEquals("case=${case.name}", case.expected, actualDecision)
            // expectedValidUntil 이 명시된 케이스만 비교 (대부분은 검증 enum 만 보면 충분).
            if (outcome is Outcome.Success && case.expectedValidUntil != null && actualDecision == AccessDecision.ALLOWED.name) {
                assertEquals(
                    "validUntil mismatch (case=${case.name})",
                    case.expectedValidUntil.takeIf { it != "null" }?.let(Instant::parse),
                    outcome.data.validUntil,
                )
            }
        }
    }

    private class FakeMasterData(private val input: Input) : MasterDataRepository {
        override suspend fun sync(): Outcome<Unit, MasterError> = error("unused")
        override suspend fun currentVersion(): String? = null
        override suspend fun cardByUid(uid: CardUid): CachedCard? = CachedCard(
            id = 1L,
            uid = uid,
            cardType = "FELICA",
            userId = 1L,
            isActive = input.card.isActive,
        )
        override suspend fun userById(id: Long): CachedUser? = CachedUser(
            id = id,
            employeeCode = EmployeeCode("EMP-GOLDEN"),
            isActive = input.user.isActive,
        )
        override suspend fun permissionsForUserAndZone(userId: Long, zone: Zone): List<CachedPermission> =
            input.permissions
                .filter { it.zone == zone.value }
                .mapIndexed { idx, p ->
                    CachedPermission(
                        id = idx + 1L,
                        userId = userId,
                        zone = Zone(p.zone),
                        validUntil = p.validUntil?.let(Instant::parse),
                        allowedHoursStart = p.allowedHoursStart?.let(LocalTime::parse),
                        allowedHoursEnd = p.allowedHoursEnd?.let(LocalTime::parse),
                    )
                }

        override fun observeAvailableZones() = kotlinx.coroutines.flow.flowOf(emptyList<CachedZone>())
    }

    private class FixedTimeProvider(private val fixed: Instant) : TimeProvider {
        override fun now(): Instant = fixed
        override fun zoneId(): ZoneId = ZoneId.of("Asia/Tokyo")
    }

    @Serializable
    private data class Matrix(val cases: List<Case>)

    @Serializable
    private data class Case(
        val name: String,
        val input: Input,
        val expected: String,
        val expectedValidUntil: String? = null,
    )

    @Serializable
    private data class Input(
        val atIso: String,
        val zone: String,
        val card: CardSpec,
        val user: UserSpec,
        val permissions: List<PermissionSpec>,
    )

    @Serializable
    private data class CardSpec(val isActive: Boolean)

    @Serializable
    private data class UserSpec(val isActive: Boolean)

    @Serializable
    private data class PermissionSpec(
        val zone: String,
        val validUntil: String? = null,
        val allowedHoursStart: String? = null,
        val allowedHoursEnd: String? = null,
    )

    private companion object {
        const val CARD_UID = "GOLDEN-CARD-UID"
    }
}
