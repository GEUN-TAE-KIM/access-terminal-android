package com.gtkim.mobile_access_control.core.network.interceptor

import com.gtkim.mobile_access_control.core.common.time.TimeProvider
import com.gtkim.mobile_access_control.core.network.auth.TokenProvider
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.delay
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneId
import java.util.Collections
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class AuthInterceptorTest {

    @Test
    fun `no access token - no Authorization header attached`() {
        val chain = fakeChain { req -> response(req, 200) }

        AuthInterceptor(FakeTokenProvider(accessToken = null), FakeTime).intercept(chain.asChain())

        assertNull(chain.proceeded.single().header("Authorization"))
    }

    @Test
    fun `attaches bearer access token`() {
        val chain = fakeChain { req -> response(req, 200) }

        AuthInterceptor(FakeTokenProvider(accessToken = "ACCESS"), FakeTime).intercept(chain.asChain())

        assertEquals("Bearer ACCESS", chain.proceeded.single().header("Authorization"))
    }

    @Test
    fun `401 then successful refresh retries with new token`() {
        val tokenProvider = FakeTokenProvider(
            accessToken = "OLD",
            refreshSucceeds = true,
            tokenAfterRefresh = "NEW",
        )
        val chain = fakeChain { req -> response(req, codeFor(req)) }

        val result = AuthInterceptor(tokenProvider, FakeTime).intercept(chain.asChain())

        assertEquals(200, result.code)
        assertEquals(listOf("Bearer OLD", "Bearer NEW"), chain.proceeded.map { it.header("Authorization") })
        assertEquals(1, tokenProvider.refreshCount.get())
    }

    @Test
    fun `401 then failed refresh returns original 401 without retry`() {
        val tokenProvider = FakeTokenProvider(accessToken = "OLD", refreshSucceeds = false)
        val chain = fakeChain { req -> response(req, codeFor(req)) }

        val result = AuthInterceptor(tokenProvider, FakeTime).intercept(chain.asChain())

        assertEquals(401, result.code)
        assertEquals(1, chain.proceeded.size)
        assertEquals(1, tokenProvider.refreshCount.get())
    }

    @Test
    fun `concurrent 401s trigger refresh only once - single-flight`() {
        val tokenProvider = FakeTokenProvider(
            accessToken = "OLD",
            refreshSucceeds = true,
            tokenAfterRefresh = "NEW",
            refreshDelayMs = 50,
        )
        val interceptor = AuthInterceptor(tokenProvider, FakeTime)

        val threads = 8
        val pool = Executors.newFixedThreadPool(threads)
        val startGate = CountDownLatch(1)
        val codes = Collections.synchronizedList(mutableListOf<Int>())

        val futures = (1..threads).map {
            pool.submit {
                startGate.await()
                val chain = fakeChain { req -> response(req, codeFor(req)) }
                codes.add(interceptor.intercept(chain.asChain()).code)
            }
        }
        startGate.countDown()
        futures.forEach { it.get(5, TimeUnit.SECONDS) }
        pool.shutdown()

        assertEquals(threads, codes.size)
        assertTrue("모든 요청이 최종 200 이어야 함", codes.all { it == 200 })
        assertEquals("refresh 는 정확히 1회만 호출되어야 함", 1, tokenProvider.refreshCount.get())
    }

    // --- helpers --------------------------------------------------------------

    /** "Bearer NEW" 로 재시도된 요청만 200, 그 외(만료된 "OLD")는 401. */
    private fun codeFor(request: Request): Int =
        if (request.header("Authorization") == "Bearer NEW") 200 else 401

    private fun response(request: Request, code: Int): Response = Response.Builder()
        .request(request)
        .protocol(Protocol.HTTP_1_1)
        .code(code)
        .message(if (code == 200) "OK" else "Unauthorized")
        .body("".toResponseBody(null))
        .build()

    private fun fakeChain(responder: (Request) -> Response): RecordingChain =
        RecordingChain(responder)

    /** [Interceptor.Chain] 의 가짜 구현 — `request()`/`proceed()` 만 쓰므로 그것만 채운다. */
    private class RecordingChain(private val responder: (Request) -> Response) {
        val proceeded = mutableListOf<Request>()
        private val request = Request.Builder().url("http://localhost/verify").build()

        fun asChain(): Interceptor.Chain {
            val chain = mockk<Interceptor.Chain>()
            every { chain.request() } returns request
            every { chain.proceed(any()) } answers {
                val req = firstArg<Request>()
                synchronized(proceeded) { proceeded.add(req) }
                responder(req)
            }
            return chain
        }
    }

    /** 만료 임박 판정에 안 쓰이는 fixed time — `accessTokenExpiresAt = null` 이라 비교 자체 발생 안 함. */
    private object FakeTime : TimeProvider {
        override fun now(): Instant = Instant.EPOCH
        override fun zoneId(): ZoneId = ZoneId.of("Asia/Tokyo")
    }

    private class FakeTokenProvider(
        accessToken: String?,
        private val refreshSucceeds: Boolean = false,
        private val tokenAfterRefresh: String? = null,
        private val refreshDelayMs: Long = 0,
    ) : TokenProvider {

        @Volatile
        private var token: String? = accessToken
        val refreshCount = AtomicInteger(0)

        override fun currentAccessToken(): String? = token

        /** 사전 갱신 경로를 비활성화 — 본 테스트 셋은 401 사후 갱신만 검증한다. */
        override fun accessTokenExpiresAt(): Instant? = null

        override suspend fun refreshTokens(): Boolean {
            refreshCount.incrementAndGet()
            if (refreshDelayMs > 0) delay(refreshDelayMs)
            return if (refreshSucceeds) {
                token = tokenAfterRefresh
                true
            } else {
                false
            }
        }
    }
}
