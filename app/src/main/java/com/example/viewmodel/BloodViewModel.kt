package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.database.DonorEntity
import com.example.database.EmergencyRequestEntity
import com.example.repository.BloodRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class BloodViewModel(private val repository: BloodRepository) : ViewModel() {

    // Authentication state
    private val _isAdminLoggedIn = MutableStateFlow(false)
    val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn.asStateFlow()

    // Search Fiters triggers
    val searchBloodGroup = MutableStateFlow("")
    val searchDistrict = MutableStateFlow("")
    val searchCity = MutableStateFlow("")
    val showOnlyAvailable = MutableStateFlow(false)

    // Reactive Combined Donor Search results flow
    val matchingDonors: StateFlow<List<DonorEntity>> = combine(
        searchBloodGroup,
        searchDistrict,
        searchCity,
        showOnlyAvailable
    ) { bg, dist, city, onlyAvail ->
        repository.searchDonors(bg, dist, city, onlyAvail)
    }.flatMapLatest { flow ->
        flow
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Master list elements (for admin)
    val allDonors: StateFlow<List<DonorEntity>> = repository.allDonors.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Emergency pending lists
    val emergencyRequests: StateFlow<List<EmergencyRequestEntity>> = repository.allEmergencyRequests.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // System Counters
    val totalDonorsCount: StateFlow<Int> = repository.totalCount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val availableDonorsCount: StateFlow<Int> = repository.availableCount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    // Operations methods
    fun loginAdmin(id: String, pass: String): Boolean {
        return if (id == "Blooddonorssystem" && pass == "blooddonor@123") {
            _isAdminLoggedIn.value = true
            true
        } else {
            false
        }
    }

    fun logoutAdmin() {
        _isAdminLoggedIn.value = false
    }

    fun registerDonor(donor: DonorEntity, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.insertDonor(donor)
            onResult(success)
        }
    }

    fun deleteDonor(id: Int) {
        viewModelScope.launch {
            repository.deleteDonorById(id)
        }
    }

    fun postEmergencyRequest(request: EmergencyRequestEntity) {
        viewModelScope.launch {
            repository.insertRequest(request)
        }
    }

    fun resolveEmergency(id: Int) {
        viewModelScope.launch {
            repository.deleteRequestById(id)
        }
    }
}
