package com.jonaylor.saintjohn.data.local.dao

import androidx.room.*
import com.jonaylor.saintjohn.data.local.entity.AppPreferenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppPreferenceDao {
    @Query("SELECT * FROM app_preferences")
    fun getAllPreferences(): Flow<List<AppPreferenceEntity>>

    @Query("SELECT * FROM app_preferences WHERE packageName = :packageName")
    suspend fun getPreference(packageName: String): AppPreferenceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreference(preference: AppPreferenceEntity)

    @Update
    suspend fun updatePreference(preference: AppPreferenceEntity)

    @Delete
    suspend fun deletePreference(preference: AppPreferenceEntity)

    @Query("SELECT * FROM app_preferences WHERE isHidden = 1")
    fun getHiddenApps(): Flow<List<AppPreferenceEntity>>

    @Query("SELECT * FROM app_preferences WHERE isLocked = 1")
    fun getLockedApps(): Flow<List<AppPreferenceEntity>>
}
