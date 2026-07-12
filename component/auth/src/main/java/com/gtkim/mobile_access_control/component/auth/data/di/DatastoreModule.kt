package com.gtkim.mobile_access_control.component.auth.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStoreFile
import com.gtkim.mobile_access_control.component.auth.data.local.AuthTokens
import com.gtkim.mobile_access_control.component.auth.data.local.AuthTokensSerializer
import com.gtkim.mobile_access_control.component.auth.data.local.DataStoreTokenStorage
import com.gtkim.mobile_access_control.component.auth.data.local.TokenStorage
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class TokenStorageModule {

    @Binds
    @Singleton
    abstract fun bindTokenStorage(impl: DataStoreTokenStorage): TokenStorage

    companion object {
        /**
         * @Singleton 필수 — DataStore 는 동일 파일에 대해 하나의 인스턴스만 허용.
         * 두 번 만들면 IllegalStateException ("There are multiple DataStores active for the same file").
         *
         * corruptionHandler: Serializer 가 throw 한 CorruptionException 을 받아서
         * 파일을 `AuthTokens.EMPTY` 로 덮어써 자동 복구. 미등록 시 그대로 throw → 앱 크래시.
         */
        @Provides
        @Singleton
        fun authTokensDataStore(
            @ApplicationContext context: Context,
            serializer: AuthTokensSerializer,
        ): DataStore<AuthTokens> = DataStoreFactory.create(
            serializer = serializer,
            corruptionHandler = ReplaceFileCorruptionHandler { AuthTokens.EMPTY },
            produceFile = { context.dataStoreFile(FILE_NAME) },
        )

        private const val FILE_NAME = "auth_tokens.bin"
    }
}
