package com.example.autoavp.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("autoavp_settings", Context.MODE_PRIVATE)

    private val _calibrationX = MutableStateFlow(prefs.getFloat(KEY_CALIB_X, 0f))
    val calibrationX: StateFlow<Float> = _calibrationX.asStateFlow()

    private val _calibrationY = MutableStateFlow(prefs.getFloat(KEY_CALIB_Y, 0f))
    val calibrationY: StateFlow<Float> = _calibrationY.asStateFlow()

    private val _autoDetection = MutableStateFlow(prefs.getBoolean(KEY_AUTO_DETECTION, true))
    val autoDetection: StateFlow<Boolean> = _autoDetection.asStateFlow()

    private val _selectedOfficeId = MutableStateFlow(prefs.getLong(KEY_SELECTED_OFFICE, -1L))
    val selectedOfficeId: StateFlow<Long> = _selectedOfficeId.asStateFlow()

    private val _hasSeenWelcome = MutableStateFlow(prefs.getBoolean(KEY_HAS_SEEN_WELCOME, false))
    val hasSeenWelcome: StateFlow<Boolean> = _hasSeenWelcome.asStateFlow()

    suspend fun setCalibrationX(value: Float) = withContext(Dispatchers.IO) {
        prefs.edit { putFloat(KEY_CALIB_X, value) }
        _calibrationX.value = value
    }

    suspend fun setCalibrationY(value: Float) = withContext(Dispatchers.IO) {
        prefs.edit { putFloat(KEY_CALIB_Y, value) }
        _calibrationY.value = value
    }

    suspend fun setAutoDetection(value: Boolean) = withContext(Dispatchers.IO) {
        prefs.edit { putBoolean(KEY_AUTO_DETECTION, value) }
        _autoDetection.value = value
    }

    suspend fun setSelectedOfficeId(value: Long) = withContext(Dispatchers.IO) {
        prefs.edit { putLong(KEY_SELECTED_OFFICE, value) }
        _selectedOfficeId.value = value
    }

    suspend fun setHasSeenWelcome(value: Boolean) = withContext(Dispatchers.IO) {
        prefs.edit { putBoolean(KEY_HAS_SEEN_WELCOME, value) }
        _hasSeenWelcome.value = value
    }

    companion object {
        private const val KEY_CALIB_X = "calibration_x"
        private const val KEY_CALIB_Y = "calibration_y"
        private const val KEY_AUTO_DETECTION = "auto_detection"
        private const val KEY_SELECTED_OFFICE = "selected_office_id"
        private const val KEY_HAS_SEEN_WELCOME = "has_seen_welcome"
    }
}
