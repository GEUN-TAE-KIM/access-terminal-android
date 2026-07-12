package com.gtkim.mobile_access_control.component.master.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.gtkim.mobile_access_control.component.master.domain.repository.TerminalSettings
import com.gtkim.mobile_access_control.core.model.Zone
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 단말 설정 영속 — Preferences DataStore (Phase 12, 2026-05-27 SharedPreferences → DataStore 이전).
 *
 * SharedPreferences 패턴이 코루틴/Flow 친화성 떨어지고 본 프로젝트가 이미 typed DataStore
 * ([com.gtkim.mobile_access_control.component.auth.data.local.DataStoreTokenStorage]) 를 쓰니까
 * 일관성 차원에서 후속 API 로 통일. 단일 String key 라 typed DataStore 의 Serializer 보일러는
 * 과하니 Preferences DataStore 로 선택.
 *
 * 다른 프로세스가 같은 datastore 를 건드리는 시나리오 없음. delegate ([preferencesDataStore]) 가
 * Context 당 단일 인스턴스를 보장하므로 별도 Hilt @Provides 불필요.
 */
private val Context.terminalSettingsDataStore: DataStore<Preferences>
        by preferencesDataStore(name = TERMINAL_SETTINGS_DATASTORE_NAME)

private const val TERMINAL_SETTINGS_DATASTORE_NAME = "terminal_settings"

@Singleton
internal class TerminalSettingsImpl @Inject constructor(
    @ApplicationContext context: Context,
) : TerminalSettings {

    private val store: DataStore<Preferences> = context.terminalSettingsDataStore

    override fun observeSelectedZone(): Flow<Zone?> = store.data
        .map { prefs -> prefs[KEY_SELECTED_ZONE]?.takeIf { it.isNotBlank() }?.let(::Zone) }
        .distinctUntilChanged()

    override suspend fun selectZone(zone: Zone) {
        store.edit { prefs -> prefs[KEY_SELECTED_ZONE] = zone.value }
    }

    override suspend fun clearZone() {
        store.edit { prefs -> prefs.remove(KEY_SELECTED_ZONE) }
    }

    private companion object {
        val KEY_SELECTED_ZONE = stringPreferencesKey("selected_zone_code")
    }
}
