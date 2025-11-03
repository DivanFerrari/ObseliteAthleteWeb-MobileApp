package com.example.athletesync

data class AthleteInfo(
    val athleteId: String,
    val fullName: String,
    val dateOfBirth: String,
    val emergencyContact: String,
    val bloodType: String,
    val allergies: String,
    val medicalConditions: String,
    val height: String,
    val weight: String,
    val sport: String,
    val position: String,
    val coachNotes: String,
    val lastUpdated: String,
    val emergencyCascade: EmergencyContactCascade? = null
)