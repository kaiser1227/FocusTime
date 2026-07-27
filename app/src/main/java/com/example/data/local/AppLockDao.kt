package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entities.FocusStreakEntity
import com.example.data.local.entities.LockedAppEntity
import com.example.data.local.entities.MissionSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppLockDao {

    // Locked Apps
    @Query("SELECT * FROM locked_apps ORDER BY appName ASC")
    fun getAllLockedApps(): Flow<List<LockedAppEntity>>

    @Query("SELECT * FROM locked_apps WHERE isLocked = 1")
    fun getLockedAppsSync(): List<LockedAppEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateApp(app: LockedAppEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateApps(apps: List<LockedAppEntity>)

    // Mission & Focus Settings
    @Query("SELECT * FROM mission_settings WHERE id = 1")
    fun getMissionSettings(): Flow<MissionSettingsEntity?>

    @Query("SELECT * FROM mission_settings WHERE id = 1")
    suspend fun getMissionSettingsSync(): MissionSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateMissionSettings(settings: MissionSettingsEntity)

    // Focus Streaks
    @Query("SELECT * FROM focus_streaks ORDER BY date DESC")
    fun getFocusStreaks(): Flow<List<FocusStreakEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStreak(streak: FocusStreakEntity)
}
