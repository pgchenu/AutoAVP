package com.example.autoavp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autoavp.data.local.entities.InstanceOfficeEntity
import com.example.autoavp.data.local.entities.MailItemEntity
import com.example.autoavp.data.repository.OfficeRepository
import com.example.autoavp.data.repository.ScanRepository
import com.example.autoavp.data.repository.SettingsRepository
import com.example.autoavp.domain.model.ScannedData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val scanRepository: ScanRepository,
    private val settingsRepository: SettingsRepository,
    officeRepository: OfficeRepository
) : ViewModel() {

    private val _latestSession = scanRepository.getLatestSession()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val latestSession = _latestSession

    @OptIn(ExperimentalCoroutinesApi::class)
    val mailItems: StateFlow<List<MailItemEntity>> = _latestSession.flatMapLatest { session ->
        if (session == null) flowOf(emptyList())
        else scanRepository.getItemsForSession(session.sessionId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val offices: StateFlow<List<InstanceOfficeEntity>> = officeRepository.getAllOffices()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedOffice = MutableStateFlow<InstanceOfficeEntity?>(null)
    val selectedOffice = _selectedOffice.asStateFlow()

    init {
        // Restaurer le bureau sélectionné depuis les préférences
        viewModelScope.launch {
            val savedId = settingsRepository.selectedOfficeId.value
            if (savedId > 0) {
                // Attendre max 3s que la liste des bureaux soit chargée
                withTimeoutOrNull(3000) {
                    offices.first { it.isNotEmpty() }
                }?.find { it.officeId == savedId }
                    ?.let { _selectedOffice.value = it }
            }
        }
    }

    fun clearOffice() {
        _selectedOffice.value = null
        viewModelScope.launch {
            settingsRepository.setSelectedOfficeId(-1L)
        }
    }

    fun selectOffice(office: InstanceOfficeEntity) {
        _selectedOffice.value = office
        viewModelScope.launch {
            settingsRepository.setSelectedOfficeId(office.officeId)
        }
    }

    fun updateItem(item: MailItemEntity) {
        viewModelScope.launch {
            scanRepository.updateMailItem(item)
        }
    }

    fun deleteItem(item: MailItemEntity) {
        viewModelScope.launch {
            scanRepository.deleteMailItem(item)
            item.imagePath?.let { path ->
                try {
                    val file = java.io.File(path)
                    if (file.exists()) {
                        file.delete()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun addManualItem(trackingNumber: String, address: String) {
        viewModelScope.launch {
            val session = _latestSession.value
            val sessionId = session?.sessionId ?: scanRepository.createSession()
            
            scanRepository.saveScannedItem(
                sessionId,
                ScannedData(trackingNumber = trackingNumber, rawText = address)
            )
        }
    }

    fun createNewSession() {
        viewModelScope.launch {
            scanRepository.createSession()
        }
    }
}
