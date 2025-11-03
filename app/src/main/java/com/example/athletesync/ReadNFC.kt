package com.example.athletesync

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.wilprototype.databinding.ActivityReadNfcBinding

class ReadNFC : AppCompatActivity() {

    private lateinit var binding: ActivityReadNfcBinding
    private val nfcAdapter: NfcAdapter? by lazy { NfcAdapter.getDefaultAdapter(this) }
    private var pendingIntent: PendingIntent? = null
    private var intentFiltersArray: Array<IntentFilter>? = null
    private val techListsArray = arrayOf(arrayOf(Ndef::class.java.name))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReadNfcBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupNFC()
    }

    private fun setupUI() {
        binding.btnScanAnother.setOnClickListener {
            resetUI()
            Toast.makeText(this, "Ready to scan another NFC tag", Toast.LENGTH_SHORT).show()
        }

        binding.btnEmergencyCall.setOnClickListener {
            // Open the EnhancedContactsActivity when emergency button is clicked
            val intent = Intent(this, EnhancedContactsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupNFC() {
        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC not supported on this device", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        if (!nfcAdapter!!.isEnabled) {
            Toast.makeText(this, "Please enable NFC to scan athlete tags", Toast.LENGTH_LONG).show()
        }

        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)
        } else {
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        }

        val ndefFilter = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
            try {
                addDataType("application/vnd.obselite.athlete+json")
            } catch (e: IntentFilter.MalformedMimeTypeException) {
                e.printStackTrace()
            }
        }
        intentFiltersArray = arrayOf(ndefFilter)
    }

    private fun resetUI() {
        binding.athleteInfoGroup.isVisible = false
        binding.scanPromptGroup.isVisible = true
        binding.progressBar.isVisible = false
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter?.enableForegroundDispatch(
            this,
            pendingIntent,
            intentFiltersArray,
            techListsArray
        )
    }

    override fun onPause() {
        nfcAdapter?.disableForegroundDispatch(this)
        super.onPause()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action ||
            NfcAdapter.ACTION_TECH_DISCOVERED == intent.action) {

            binding.progressBar.isVisible = true
            binding.scanPromptGroup.isVisible = false

            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG) ?: return
            readAthleteDataFromTag(tag)
        }
    }

    private fun readAthleteDataFromTag(tag: Tag) {
        try {
            val ndef = Ndef.get(tag) ?: run {
                showError("Tag is not NDEF formatted")
                return
            }

            ndef.connect()
            val ndefMessage = ndef.ndefMessage ?: run {
                showError("No athlete data found on tag")
                ndef.close()
                return
            }

            val records = ndefMessage.records
            if (records.isNotEmpty()) {
                val payload = records[0].payload
                val jsonString = String(payload, Charsets.UTF_8)
                val athleteInfo = AthleteInfo.fromJson(jsonString)

                if (athleteInfo != null) {
                    displayAthleteInfo(athleteInfo)
                } else {
                    showError("Invalid athlete data format")
                }
            }

            ndef.close()
        } catch (e: Exception) {
            showError("Error reading tag: ${e.message}")
        } finally {
            binding.progressBar.isVisible = false
        }
    }

    private fun displayAthleteInfo(athleteInfo: AthleteInfo) {
        runOnUiThread {
            binding.athleteInfoGroup.isVisible = true

            // Basic Info
            binding.tvAthleteName.text = athleteInfo.fullName
            binding.tvAthleteId.text = "ID: ${athleteInfo.athleteId}"
            binding.tvSportPosition.text = "${athleteInfo.sport} • ${athleteInfo.position}"

            // Medical Info
            binding.tvBloodTypeValue.text = athleteInfo.bloodType.ifEmpty { "Not specified" }
            binding.tvAllergiesValue.text = athleteInfo.allergies.ifEmpty { "None reported" }
            binding.tvMedicalConditionsValue.text = athleteInfo.medicalConditions.ifEmpty { "None reported" }

            // Physical Stats
            binding.tvHeightValue.text = if (athleteInfo.height.isNotEmpty()) "${athleteInfo.height} cm" else "Not specified"
            binding.tvWeightValue.text = if (athleteInfo.weight.isNotEmpty()) "${athleteInfo.weight} kg" else "Not specified"

            // Emergency Contact
            binding.tvEmergencyContact.text = athleteInfo.emergencyContact.ifEmpty { "Not specified" }
            binding.tvDateOfBirthValue.text = athleteInfo.dateOfBirth.ifEmpty { "Not specified" }

            binding.tvCoachNotesValue.text = athleteInfo.coachNotes.ifEmpty { "No notes" }
            binding.tvLastUpdatedValue.text = athleteInfo.lastUpdated

            // Update emergency button to use the individual athlete's emergency contact
            binding.btnEmergencyCall.setOnClickListener {
                val emergencyContact = athleteInfo.emergencyContact
                if (emergencyContact.isNotEmpty() && emergencyContact != "Not specified") {
                    // Call the specific athlete's emergency contact
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = android.net.Uri.parse("tel:$emergencyContact")
                    }
                    startActivity(intent)
                } else {
                    // If no specific contact, open the general emergency contacts
                    val intent = Intent(this, EnhancedContactsActivity::class.java)
                    startActivity(intent)
                }
            }

            Toast.makeText(this, " Loaded data for ${athleteInfo.fullName}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showError(message: String) {
        runOnUiThread {
            Toast.makeText(this, " $message", Toast.LENGTH_LONG).show()
            binding.progressBar.isVisible = false
            binding.scanPromptGroup.isVisible = true
        }
    }
}