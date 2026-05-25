package com.example.repository

import com.example.database.BloodDonorDao
import com.example.database.DonorEntity
import com.example.database.EmergencyRequestDao
import com.example.database.EmergencyRequestEntity
import kotlinx.coroutines.flow.Flow

class BloodRepository(
    private val donorDao: BloodDonorDao,
    private val emergencyDao: EmergencyRequestDao
) {
    val allDonors: Flow<List<DonorEntity>> = donorDao.getAllDonors()
    val totalCount: Flow<Int> = donorDao.getCount()
    val availableCount: Flow<Int> = donorDao.getAvailableCount()
    
    val allEmergencyRequests: Flow<List<EmergencyRequestEntity>> = emergencyDao.getAllRequests()

    fun searchDonors(bloodGroup: String, district: String, city: String, onlyAvailable: Boolean): Flow<List<DonorEntity>> {
        return donorDao.searchDonors(
            bloodGroup = bloodGroup,
            district = district,
            city = city,
            onlyAvailable = if (onlyAvailable) 1 else 0
        )
    }

    suspend fun insertDonor(donor: DonorEntity): Boolean {
        // Validation check for unique phone numbers
        val existing = donorDao.getDonorByPhone(donor.phone)
        if (existing != null && existing.id != donor.id) {
            return false // Phone number must be unique!
        }
        donorDao.insertDonor(donor)
        return true
    }

    suspend fun deleteDonorById(id: Int) {
        donorDao.deleteDonorById(id)
    }

    suspend fun insertRequest(request: EmergencyRequestEntity) {
        emergencyDao.insertRequest(request)
    }

    suspend fun deleteRequestById(id: Int) {
        emergencyDao.deleteRequestById(id)
    }
}
