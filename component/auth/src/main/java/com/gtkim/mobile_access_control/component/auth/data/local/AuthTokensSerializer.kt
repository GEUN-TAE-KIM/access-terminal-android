package com.gtkim.mobile_access_control.component.auth.data.local

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import java.security.GeneralSecurityException
import javax.inject.Inject

/**
 * DataStore<AuthTokens> 의 read/write 경계에서 JSON 직렬화 + AndroidKeyStore AES-GCM 암복호화.
 *
 * 디스크에는 항상 암호문만 저장.
 *
 * 두 실패 경로 모두 `CorruptionException` 으로 통일 → 모듈의 `ReplaceFileCorruptionHandler` 가
 * 단일 진입점에서 `AuthTokens.EMPTY` 로 복구. 호출부는 NPE/crash 걱정 없음.
 *
 *  1) 복호화 실패 — `GeneralSecurityException` (키 손상, IV 손상, GCM tag mismatch 등)
 *  2) 역직렬화 실패 — `SerializationException` (스키마 변경, JSON 손상 등)
 */
internal class AuthTokensSerializer @Inject constructor(
    private val crypto: KeystoreCrypto,
) : Serializer<AuthTokens> {

    override val defaultValue: AuthTokens = AuthTokens.EMPTY

    override suspend fun readFrom(input: InputStream): AuthTokens {
        val encryptedBytes = input.readBytes()
        if (encryptedBytes.isEmpty()) return defaultValue

        val decryptedJson = try {
            crypto.decrypt(encryptedBytes)
        } catch (e: GeneralSecurityException) {
            throw CorruptionException("AuthTokens decryption failed", e)
        }

        return try {
            Json.decodeFromString(AuthTokens.serializer(), decryptedJson)
        } catch (e: SerializationException) {
            throw CorruptionException("AuthTokens deserialization failed", e)
        }
    }

    override suspend fun writeTo(t: AuthTokens, output: OutputStream) {
        val json = Json.encodeToString(AuthTokens.serializer(), t)
        val encrypted = crypto.encrypt(json)
        output.write(encrypted)
    }
}
