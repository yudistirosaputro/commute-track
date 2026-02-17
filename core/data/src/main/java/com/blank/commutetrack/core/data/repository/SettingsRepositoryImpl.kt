package com.blank.commutetrack.core.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.blank.commutetrack.core.domain.model.DistanceUnit
import com.blank.commutetrack.core.domain.model.TransportMode
import com.blank.commutetrack.core.domain.model.UserSettings
import com.blank.commutetrack.core.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    private object Keys {
        val DEFAULT_TRANSPORT = stringPreferencesKey("default_transport")
        val DISTANCE_UNIT = stringPreferencesKey("distance_unit")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val AUTO_START = booleanPreferencesKey("auto_start")
        val HOME_ADDRESS = stringPreferencesKey("home_address")
        val WORK_ADDRESS = stringPreferencesKey("work_address")
    }

    override fun getSettings(): Flow<UserSettings> =
        context.dataStore.data.map { prefs ->
            UserSettings(
                defaultTransportMode = prefs[Keys.DEFAULT_TRANSPORT]?.let { TransportMode.valueOf(it) } ?: TransportMode.DRIVING,
                distanceUnit = prefs[Keys.DISTANCE_UNIT]?.let { DistanceUnit.valueOf(it) } ?: DistanceUnit.KILOMETERS,
                notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true,
                darkModeEnabled = prefs[Keys.DARK_MODE] ?: false,
                autoStartEnabled = prefs[Keys.AUTO_START] ?: false,
                homeAddress = prefs[Keys.HOME_ADDRESS] ?: "",
                workAddress = prefs[Keys.WORK_ADDRESS] ?: ""
            )
        }

    override suspend fun updateSettings(settings: UserSettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DEFAULT_TRANSPORT] = settings.defaultTransportMode.name
            prefs[Keys.DISTANCE_UNIT] = settings.distanceUnit.name
            prefs[Keys.NOTIFICATIONS_ENABLED] = settings.notificationsEnabled
            prefs[Keys.DARK_MODE] = settings.darkModeEnabled
            prefs[Keys.AUTO_START] = settings.autoStartEnabled
            prefs[Keys.HOME_ADDRESS] = settings.homeAddress
            prefs[Keys.WORK_ADDRESS] = settings.workAddress
        }
    }
}
