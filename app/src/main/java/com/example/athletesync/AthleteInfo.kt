package com.example.athletesync
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import com.google.gson.Gson
import java.io.Serializable
import java.nio.charset.StandardCharsets
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
) {
    // ✅ Add this function
    fun toNdefMessage(): NdefMessage {
        val jsonString = Gson().toJson(this)
        return NdefMessage(
            arrayOf(
                NdefRecord.createMime(
                    "application/vnd.obselite.athlete+json",
                    jsonString.toByteArray(StandardCharsets.UTF_8)
                )
            )
        )
    }
    companion object {
        fun fromJson(json: String): AthleteInfo? {
            return try {
                Gson().fromJson(json, AthleteInfo::class.java)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}

data class EmergencyProtocol(
    val protocolId: String,
    val title: String,
    val description: String,
    val steps: List<String>,
    val lastUpdated: String
)