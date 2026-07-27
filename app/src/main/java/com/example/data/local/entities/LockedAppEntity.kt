package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "locked_apps")
data class LockedAppEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val isLocked: Boolean,
    val isDefaultExcluded: Boolean
)
