package com.gtkim.mobile_access_control.component.sync.data

import com.gtkim.mobile_access_control.component.sync.domain.model.PendingLog
import com.gtkim.mobile_access_control.core.database.sync.dao.PendingLogDao
import com.gtkim.mobile_access_control.core.database.sync.entity.PendingLogEntity
import com.gtkim.mobile_access_control.core.model.CardUid
import com.gtkim.mobile_access_control.core.model.Zone
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class OfflineQueueRepositoryImplTest {

    private val dao = mockk<PendingLogDao>()
    private val repo = OfflineQueueRepositoryImpl(dao)

    @Test
    fun `enqueue under the cap upserts without discarding or signaling overflow`() = runTest {
        coEvery { dao.countNow() } returns 5
        coEvery { dao.upsert(any()) } just Runs

        repo.enqueue(sampleLog())

        coVerify(exactly = 0) { dao.deleteOldest(any()) }
        coVerify(exactly = 1) { dao.upsert(any()) }
    }

    @Test
    fun `enqueue at the cap discards the oldest and emits an overflow event`() = runTest {
        coEvery { dao.countNow() } returns 100
        coEvery { dao.deleteOldest(1) } just Runs
        coEvery { dao.upsert(any()) } just Runs

        val events = mutableListOf<Unit>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            repo.overflowEvents.collect { events += it }
        }

        repo.enqueue(sampleLog())

        coVerify(exactly = 1) { dao.deleteOldest(1) }
        coVerify(exactly = 1) { dao.upsert(any()) }
        assertEquals(1, events.size)
    }

    @Test
    fun `enqueue preserves the decidedAt timestamp and starts attempts at zero`() = runTest {
        coEvery { dao.countNow() } returns 0
        val captured = slot<PendingLogEntity>()
        coEvery { dao.upsert(capture(captured)) } just Runs

        val log = sampleLog()
        repo.enqueue(log)

        assertEquals(log.decidedAt.toEpochMilli(), captured.captured.decidedAtEpochMs)
        assertEquals(0, captured.captured.attempts)
        assertEquals(log.id.toString(), captured.captured.id)
    }

    @Test
    fun `markFailure below the attempt cap keeps the row`() = runTest {
        val id = UUID.randomUUID()
        coEvery { dao.incrementAndGetAttempts(id.toString()) } returns 3

        repo.markFailure(id)

        coVerify(exactly = 0) { dao.deleteById(any()) }
    }

    @Test
    fun `markFailure at the attempt cap dead-letters the row and emits an event`() = runTest {
        val id = UUID.randomUUID()
        coEvery { dao.incrementAndGetAttempts(id.toString()) } returns 10
        coEvery { dao.deleteById(id.toString()) } just Runs

        val events = mutableListOf<Unit>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            repo.deadLetterEvents.collect { events += it }
        }

        repo.markFailure(id)

        coVerify(exactly = 1) { dao.deleteById(id.toString()) }
        assertEquals(1, events.size)
    }

    @Test
    fun `markFailure on a missing row is a no-op`() = runTest {
        val id = UUID.randomUUID()
        coEvery { dao.incrementAndGetAttempts(id.toString()) } returns null

        repo.markFailure(id)

        coVerify(exactly = 0) { dao.deleteById(any()) }
    }

    @Test
    fun `markUploaded deletes the row by id`() = runTest {
        val id = UUID.randomUUID()
        coEvery { dao.deleteById(id.toString()) } just Runs

        repo.markUploaded(id)

        coVerify(exactly = 1) { dao.deleteById(id.toString()) }
    }

    private fun sampleLog() = PendingLog(
        id = UUID.randomUUID(),
        cardUid = CardUid("EMP001"),
        cardType = "MOCK",
        terminalId = "MOBILE-001",
        zone = Zone("GATE-A"),
        decidedAt = Instant.ofEpochMilli(1_716_508_800_000L),
        result = "ALLOWED",
        denyReason = null,
        verifierVersion = "v3-1716508800000-abc123",
    )
}
