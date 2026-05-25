package com.example.database

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "donors")
data class DonorEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val district: String,
    val city: String,
    val bloodGroup: String,
    val gender: String,
    val dob: String,
    val age: Int,
    val phone: String,
    val email: String,
    val availability: String = "Available", // "Available" or "Unavailable"
    val lastDonationDate: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "emergency_requests")
data class EmergencyRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val patientName: String,
    val bloodGroup: String,
    val hospitalName: String,
    val location: String, // City/District
    val urgencyLevel: String, // "Critical", "Urgent", "Normal"
    val contactPhone: String,
    val message: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface BloodDonorDao {
    @Query("SELECT * FROM donors ORDER BY name ASC")
    fun getAllDonors(): Flow<List<DonorEntity>>

    @Query("SELECT * FROM donors WHERE (:bloodGroup = '' OR bloodGroup = :bloodGroup) AND (:district = '' OR district = :district) AND (:city = '' OR city LIKE '%' || :city || '%') AND (:onlyAvailable = 0 OR availability = 'Available') ORDER BY name ASC")
    fun searchDonors(bloodGroup: String, district: String, city: String, onlyAvailable: Int): Flow<List<DonorEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDonor(donor: DonorEntity)

    @Query("DELETE FROM donors WHERE id = :id")
    suspend fun deleteDonorById(id: Int)

    @Query("SELECT COUNT(*) FROM donors")
    fun getCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM donors WHERE availability = 'Available'")
    fun getAvailableCount(): Flow<Int>
    
    @Query("SELECT * FROM donors WHERE phone = :phone LIMIT 1")
    suspend fun getDonorByPhone(phone: String): DonorEntity?
}

@Dao
interface EmergencyRequestDao {
    @Query("SELECT * FROM emergency_requests ORDER BY createdAt DESC")
    fun getAllRequests(): Flow<List<EmergencyRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: EmergencyRequestEntity)

    @Query("DELETE FROM emergency_requests WHERE id = :id")
    suspend fun deleteRequestById(id: Int)
}

@Database(entities = [DonorEntity::class, EmergencyRequestEntity::class], version = 1, exportSchema = false)
abstract class BloodDatabase : RoomDatabase() {
    abstract fun donorDao(): BloodDonorDao
    abstract fun emergencyDao(): EmergencyRequestDao
}
