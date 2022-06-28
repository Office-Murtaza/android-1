package com.belcobtm.data.disk.database.service

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceDao {

    @Query("SELECT * FROM service")
    fun observeAvailable(): Flow<List<ServiceEntity>>

    @Query("DELETE FROM service")
    fun clear()

    @Insert
    fun insert(services: List<ServiceEntity>)

    @Transaction
    fun updateServices(services: List<ServiceEntity>) {
        clear()
        insert(services)
    }
}
