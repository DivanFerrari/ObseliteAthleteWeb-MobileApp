package com.example.athletesync

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.athletesync.databinding.ActivityContactsEnhancedBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText

class EnhancedContactsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContactsEnhancedBinding
    private lateinit var contactsAdapter: EnhancedContactsAdapter
    private val defaultContacts = mutableListOf<EmergencyContact>()
    private val playerContacts = mutableListOf<EmergencyContact>()
    private var currentIncident: QuickIncidentReport? = null
    private var emergencyMode = false

    // Track current display mode
    private enum class DisplayMode { EMERGENCY, PLAYER }
    private var currentDisplayMode = DisplayMode.EMERGENCY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactsEnhancedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get incident data if in emergency mode
        currentIncident = intent.getSerializableExtra("incident_report") as? QuickIncidentReport
        emergencyMode = intent.getBooleanExtra("emergency_mode", false)

        setupUI()
        loadContacts()

        // Trigger emergency alerts if in emergency mode
        if (emergencyMode && currentIncident != null) {
            triggerEmergencyAlerts()
        }
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener { finish() }

        // Show emergency alert banner if in emergency mode
        if (emergencyMode && currentIncident != null) {
            binding.tvEmergencyAlert.text = "🚨 EMERGENCY: ${currentIncident!!.incidentType} - ${currentIncident!!.athleteName}"
            binding.tvEmergencyAlert.visibility = android.view.View.VISIBLE
        }

        contactsAdapter = EnhancedContactsAdapter(
            onCallClick = { contact -> makePhoneCall(contact.phone) },
            onMessageClick = { contact ->
                if (contact.isPlayerContact) {
                    sendMessage(contact.phone)
                } else {
                    makePhoneCall(contact.phone) // Default contacts only call
                }
            },
            onEditClick = { contact -> editContact(contact) }
        )

        binding.contactsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@EnhancedContactsActivity)
            adapter = contactsAdapter
        }

        binding.fabAddContact.setOnClickListener {
            showAddContactDialog()
        }

        binding.btnDefaultContacts.setOnClickListener {
            showEmergencyContacts()
        }

        binding.btnPlayerContacts.setOnClickListener {
            showPlayerContacts()
        }

        // Set initial button states - Emergency contacts selected by default
        updateButtonStates()
    }

    private fun loadContacts() {
        // Load default South African emergency contacts
        defaultContacts.clear()
        defaultContacts.addAll(EmergencyContactManager.getDefaultSouthAfricanContacts())

        // Load player contacts from storage
        playerContacts.clear()
        playerContacts.addAll(EmergencyContactManager.getPlayerContacts(this))

        // Show emergency contacts by default when activity starts
        showEmergencyContacts()
    }

    private fun showEmergencyContacts() {
        currentDisplayMode = DisplayMode.EMERGENCY
        contactsAdapter.submitList(defaultContacts)
        updateButtonStates()
    }

    private fun showPlayerContacts() {
        currentDisplayMode = DisplayMode.PLAYER
        contactsAdapter.submitList(playerContacts)
        updateButtonStates()
    }

    private fun updateButtonStates() {
        when (currentDisplayMode) {
            DisplayMode.EMERGENCY -> {
                // Emergency contacts button - selected state (red background, white text)
                binding.btnDefaultContacts.apply {
                    setBackgroundColor(ContextCompat.getColor(this@EnhancedContactsActivity, R.color.emergency_red))
                    setTextColor(ContextCompat.getColor(this@EnhancedContactsActivity, android.R.color.white))
                    strokeWidth = 0
                }
                // Player contacts button - unselected state (transparent background, red text, red border)
                binding.btnPlayerContacts.apply {
                    setBackgroundColor(ContextCompat.getColor(this@EnhancedContactsActivity, android.R.color.transparent))
                    setTextColor(ContextCompat.getColor(this@EnhancedContactsActivity, R.color.emergency_red))
                    strokeWidth = 2
                }
            }
            DisplayMode.PLAYER -> {
                // Player contacts button - selected state (red background, white text)
                binding.btnPlayerContacts.apply {
                    setBackgroundColor(ContextCompat.getColor(this@EnhancedContactsActivity, R.color.emergency_red))
                    setTextColor(ContextCompat.getColor(this@EnhancedContactsActivity, android.R.color.white))
                    strokeWidth = 0
                }
                // Emergency contacts button - unselected state (transparent background, red text, red border)
                binding.btnDefaultContacts.apply {
                    setBackgroundColor(ContextCompat.getColor(this@EnhancedContactsActivity, android.R.color.transparent))
                    setTextColor(ContextCompat.getColor(this@EnhancedContactsActivity, R.color.emergency_red))
                    strokeWidth = 2
                }
            }
        }
    }

    private fun triggerEmergencyAlerts() {
        currentIncident?.let { incident ->
            val emergencyMessage = createEmergencyMessage(incident)
            val allContacts = defaultContacts + playerContacts

            var successCount = 0
            allContacts.forEach { contact ->
                if (hasSmsPermission() && sendDirectSMS(contact.phone, emergencyMessage)) {
                    successCount++
                }
            }

            Toast.makeText(
                this,
                "Emergency alerts sent to $successCount of ${allContacts.size} contacts",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun createEmergencyMessage(incident: QuickIncidentReport): String {
        return """
            🚨 SPORTS EMERGENCY 🚨
            
            Athlete: ${incident.athleteName}
            Incident: ${incident.incidentType}
            Severity: ${incident.severity}
            Time: ${incident.timestamp}
            
            Description:
            ${incident.description}
            
            ${if (incident.immediateAction.isNotEmpty()) "Immediate Action: ${incident.immediateAction}" else "No immediate action specified"}
            
            URGENT RESPONSE REQUIRED!
            
            Sent via Coach Emergency App
        """.trimIndent()
    }

    private fun makePhoneCall(phoneNumber: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Cannot make phone call", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendMessage(phoneNumber: String) {
        val message = if (emergencyMode && currentIncident != null) {
            createEmergencyMessage(currentIncident!!)
        } else {
            "Urgent: Please check the Coach Emergency App for details."
        }

        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("sms:$phoneNumber")
                putExtra("sms_body", message)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Cannot open messaging app", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hasSmsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.SEND_SMS
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    private fun sendDirectSMS(phoneNumber: String, message: String): Boolean {
        return try {
            val smsManager: SmsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun showAddContactDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_contact_simple, null)
        val etName = dialogView.findViewById<TextInputEditText>(R.id.etName)
        val etPhone = dialogView.findViewById<TextInputEditText>(R.id.etPhone)
        val etRole = dialogView.findViewById<TextInputEditText>(R.id.etRole)

        MaterialAlertDialogBuilder(this)
            .setTitle("Add Player Contact")
            .setView(dialogView)
            .setPositiveButton("Add") { dialog, _ ->
                val name = etName.text.toString().trim()
                val phone = etPhone.text.toString().trim()
                val role = etRole.text.toString().trim()

                if (name.isEmpty() || phone.isEmpty()) {
                    Toast.makeText(this, "Please enter name and phone number", Toast.LENGTH_SHORT).show()
                } else {
                    val contactRole = if (role.isEmpty()) "Parent" else role
                    val newContact = EmergencyContact(
                        id = System.currentTimeMillis().toString(),
                        name = name,
                        phone = phone,
                        role = contactRole,
                        isPlayerContact = true
                    )
                    savePlayerContact(newContact)
                    dialog.dismiss()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun editContact(contact: EmergencyContact) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_contact_simple, null)
        val etName = dialogView.findViewById<TextInputEditText>(R.id.etName)
        val etPhone = dialogView.findViewById<TextInputEditText>(R.id.etPhone)
        val etRole = dialogView.findViewById<TextInputEditText>(R.id.etRole)

        // Pre-fill the fields with current contact data
        etName.setText(contact.name)
        etPhone.setText(contact.phone)
        etRole.setText(contact.role)

        MaterialAlertDialogBuilder(this)
            .setTitle("Edit Contact")
            .setView(dialogView)
            .setPositiveButton("Save") { dialog, _ ->
                val name = etName.text.toString().trim()
                val phone = etPhone.text.toString().trim()
                val role = etRole.text.toString().trim()

                if (name.isEmpty() || phone.isEmpty()) {
                    Toast.makeText(this, "Please enter name and phone number", Toast.LENGTH_SHORT).show()
                } else {
                    val contactRole = if (role.isEmpty()) "Parent" else role
                    val updatedContact = contact.copy(
                        name = name,
                        phone = phone,
                        role = contactRole
                    )
                    updatePlayerContact(updatedContact)
                    dialog.dismiss()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setNeutralButton("Delete") { dialog, _ ->
                showDeleteConfirmation(contact)
                dialog.dismiss()
            }
            .show()
    }

    private fun showDeleteConfirmation(contact: EmergencyContact) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Contact")
            .setMessage("Are you sure you want to delete ${contact.name}?")
            .setPositiveButton("Delete") { dialog, _ ->
                deletePlayerContact(contact)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun deletePlayerContact(contact: EmergencyContact) {
        val updatedContacts = playerContacts.filter { it.id != contact.id }.toMutableList()
        EmergencyContactManager.savePlayerContacts(this, updatedContacts)
        playerContacts.clear()
        playerContacts.addAll(updatedContacts)
        // Refresh the current view
        when (currentDisplayMode) {
            DisplayMode.EMERGENCY -> showEmergencyContacts()
            DisplayMode.PLAYER -> showPlayerContacts()
        }
        Toast.makeText(this, "Contact deleted successfully", Toast.LENGTH_SHORT).show()
    }

    private fun savePlayerContact(contact: EmergencyContact) {
        val updatedContacts = playerContacts.toMutableList().apply {
            add(contact)
        }
        EmergencyContactManager.savePlayerContacts(this, updatedContacts)
        playerContacts.clear()
        playerContacts.addAll(updatedContacts)
        // Switch to player contacts view to show the newly added contact
        showPlayerContacts()
        Toast.makeText(this, "Contact added successfully", Toast.LENGTH_SHORT).show()
    }

    private fun updatePlayerContact(contact: EmergencyContact) {
        val updatedContacts = playerContacts.map {
            if (it.id == contact.id) contact else it
        }.toMutableList()
        EmergencyContactManager.savePlayerContacts(this, updatedContacts)
        playerContacts.clear()
        playerContacts.addAll(updatedContacts)
        // Refresh the current view
        when (currentDisplayMode) {
            DisplayMode.EMERGENCY -> showEmergencyContacts()
            DisplayMode.PLAYER -> showPlayerContacts()
        }
        Toast.makeText(this, "Contact updated successfully", Toast.LENGTH_SHORT).show()
    }
}