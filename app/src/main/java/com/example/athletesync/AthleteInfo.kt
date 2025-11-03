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

data class QuickIncidentReport(
    val reportId: String = "",
    val athleteId: String = "",
    val athleteName: String = "",
    val incidentType: String = "",
    val severity: String = "Medium",
    val description: String = "",
    val immediateAction: String = "",
    val timestamp: String = "",
    val reporterName: String = "",
    val status: String = "Active"
) : Serializable
data class EmergencyContact(
    val id: String,
    val name: String,
    val phone: String,
    val role: String,
    val isDefault: Boolean = false,
    val isPlayerContact: Boolean = false,
    val playerId: String? = null,
    val isActive: Boolean = true
)
object SouthAfricanEmergencyContacts {
    val defaultContacts = listOf(
        EmergencyContact("1", "Ambulance", "10177", "Emergency Medical", true),
        EmergencyContact("2", "Police", "10111", "Emergency Police", true),
        EmergencyContact("3", "Emergency", "112", "General Emergency", true),
        EmergencyContact("4", "Crime Stop", "0860010111", "Crime Reporting", true),
        EmergencyContact("5", "Child Emergency", "0800123123", "Child Protection", true)
    )
}

data class EmergencyContactCascade(
    val primaryContact: String,
    val secondaryContact: String,
    val emergencyServices: String = "10111",
    val teamPhysician: String,
    val athleticDirector: String,
    val lastUpdated: String

) {
    fun toNdefMessage(): NdefMessage {
        val jsonString = Gson().toJson(this)
        return NdefMessage(
            arrayOf(
                NdefRecord.createMime(
                    "application/vnd.obselite.emergency+json",
                    jsonString.toByteArray(StandardCharsets.UTF_8)
                )
            )
        )
    }
}
