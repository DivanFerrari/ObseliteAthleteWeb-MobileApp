package com.example.athletesync

data class PracticeSchedule(
    val id: String = "",
    val title: String = "",
    val date: String = "",
    val time: String = "",
    val location: String = "",
    val notes: String = "",
    val reminderMinutes: Int = 30,
    val coachId: String = "",
    val createdAt: String = "",
    val isRecurring: Boolean = false,
    val recurringDays: List<String> = emptyList()
)