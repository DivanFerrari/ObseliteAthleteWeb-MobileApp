package com.example.athletesync

import android.content.Context

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

object EmergencyContactManager {
    private const val PREFS_NAME = "emergency_contacts"
    private const val PLAYER_CONTACTS_KEY = "player_contacts"

    fun getDefaultSouthAfricanContacts(): List<EmergencyContact> {
        return SouthAfricanEmergencyContacts.defaultContacts
    }

    fun getPlayerContacts(context: Context): List<EmergencyContact> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(PLAYER_CONTACTS_KEY, null)
        return if (json != null) {
            Gson().fromJson(json, object : TypeToken<List<EmergencyContact>>() {}.type)
        } else {
            emptyList()
        }
    }

    fun savePlayerContacts(context: Context, contacts: List<EmergencyContact>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = Gson().toJson(contacts)
        prefs.edit().putString(PLAYER_CONTACTS_KEY, json).apply()
    }

    fun getAllContacts(context: Context): List<EmergencyContact> {
        val defaultContacts = getDefaultSouthAfricanContacts()
        val playerContacts = getPlayerContacts(context)
        return defaultContacts + playerContacts
    }

    fun getEmergencyContacts(context: Context): List<EmergencyContact> {
        return getAllContacts(context)
    }

    fun saveEmergencyContacts(context: Context, contacts: List<EmergencyContact>) {

        val playerContacts = contacts.filter { it.isPlayerContact }
        savePlayerContacts(context, playerContacts)
    }

    fun getDefaultCascade(context: Context): EmergencyContactCascade {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val contacts = getEmergencyContacts(context)

        val primary = contacts.find { it.role == "Primary" }
        val secondary = contacts.find { it.role == "Secondary" }
        val physician = contacts.find { it.role == "Physician" }
        val director = contacts.find { it.role == "Director" }

        return EmergencyContactCascade(
            primaryContact = primary?.phone ?: "10111",
            secondaryContact = secondary?.phone ?: "10177",
            emergencyServices = "112",
            teamPhysician = physician?.phone ?: "+27415031111",
            athleticDirector = director?.phone ?: "+27415031111",
            lastUpdated = dateFormat.format(Date())
        )
    }
}