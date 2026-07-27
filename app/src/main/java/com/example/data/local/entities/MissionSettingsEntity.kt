package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mission_settings")
data class MissionSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val meditationEnabled: Boolean = true,
    val resolutionEnabled: Boolean = true,
    val friendApprovalEnabled: Boolean = false,
    val customResolution: String = "",
    val friendCode: String = "1234",
    val isFocusing: Boolean = false,
    val focusStartTime: Long = 0L,
    val todayEnduredSeconds: Long = 0L,
    val lastUpdatedDate: String = ""
)
