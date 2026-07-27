package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "focus_streaks")
data class FocusStreakEntity(
    @PrimaryKey val date: String, // YYYY-MM-DD
    val dayOfWeek: Int, // 1=Mon, 2=Tue, 3=Wed, 4=Thu, 5=Fri, 6=Sat, 7=Sun
    val durationSeconds: Long,
    val isSuccess: Boolean
)
