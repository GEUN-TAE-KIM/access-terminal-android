package com.gtkim.mobile_access_control.component.auth.data.local

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AndroidKeyStore (TEE/StrongBox) 의 AES-256-GCM 키를 사용한 byte 기반 암복호화.
 *
 * - 키는 단말기 하드웨어 보안 영역에 보관 — 백업/루팅으로도 export 불가.
 * - 동일 alias 로 재호출 시 기존 키 재사용.
 * - 출력 포맷: `IV(12B) || ciphertext || GCM tag(16B)` — DataStore 파일에 raw 로 그대로 저장 (Base64 불필요).
 * - 실패 시 `GeneralSecurityException` 계열 전파 — 호출자가 `CorruptionException` 으로 wrap.
 */
@Singleton
internal class KeystoreCrypto @Inject constructor() {

    fun encrypt(plaintext: String): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        }
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        return ByteArray(iv.size + ciphertext.size).also {
            iv.copyInto(it, 0)
            ciphertext.copyInto(it, iv.size)
        }
    }

    fun decrypt(combined: ByteArray): String {
        require(combined.size > IV_LENGTH) { "ciphertext shorter than IV" }
        val iv = combined.copyOfRange(0, IV_LENGTH)
        val ciphertext = combined.copyOfRange(IV_LENGTH, combined.size)
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(TAG_LENGTH_BITS, iv))
        }
        return cipher.doFinal(ciphertext).toString(Charsets.UTF_8)
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        val keyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)
        keyGen.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(KEY_SIZE_BITS)
                .build(),
        )
        return keyGen.generateKey()
    }

    private companion object {
        const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        const val KEY_ALIAS = "access.token.aes_gcm"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val KEY_SIZE_BITS = 256
        const val IV_LENGTH = 12
        const val TAG_LENGTH_BITS = 128
    }
}
