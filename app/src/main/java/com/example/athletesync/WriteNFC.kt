package com.example.athletesync

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class WriteNFC : AppCompatActivity() {

    private lateinit var binding: ActivityWriteNfcBinding
    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null
    private var intentFiltersArray: Array<IntentFilter>? = null
    private val techListsArray = arrayOf(
        arrayOf(Ndef::class.java.name),
        arrayOf(NdefFormatable::class.java.name)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWriteNfcBinding.inflate(layoutInflater)
        setContentView(binding.root)

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        // Initialize NFC foreground dispatch
        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)
        } else {
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        }

        val ndefFilter = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
        try {
            ndefFilter.addDataType("application/vnd.obselite.athlete+json")
        } catch (e: IntentFilter.MalformedMimeTypeException) {
            throw RuntimeException("Failed to add MIME type", e)
        }
        intentFiltersArray = arrayOf(ndefFilter)

        // NFC availability check
        if (nfcAdapter == null) {
            Toast.makeText(this, " NFC not supported on this device", Toast.LENGTH_SHORT).show()
            finish()
        } else if (!nfcAdapter!!.isEnabled) {
            Toast.makeText(this, " Please turn on NFC to write athlete data", Toast.LENGTH_LONG).show()
        }

        setupUI()
    }

    private fun setupUI() {
        binding.btnSaveData.setOnClickListener {
            if (validateInput()) {
                Toast.makeText(this, " Data ready! Tap an NFC tag to write athlete information", Toast.LENGTH_LONG).show()
            }
        }

        binding.btnClear.setOnClickListener {
            clearAllFields()
        }

        binding.btnGenerateId.setOnClickListener {
            binding.athleteId.setText(generateAthleteId())
        }
    }

    private fun generateAthleteId(): String {
        val prefix = "OBA" // Obselite Athlete
        val random = Random().nextInt(9000) + 1000
        return "$prefix$random"
    }

    private fun validateInput(): Boolean {
        if (binding.athleteId.text.toString().trim().isEmpty()) {
            Toast.makeText(this, " Please generate an Athlete ID", Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.fullName.text.toString().trim().isEmpty()) {
            Toast.makeText(this, " Please enter athlete's full name", Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.emergencyContact.text.toString().trim().isEmpty()) {
            Toast.makeText(this, " Please enter emergency contact", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun clearAllFields() {
        binding.athleteId.setText("")
        binding.fullName.setText("")
        binding.dateOfBirth.setText("")
        binding.emergencyContact.setText("")
        binding.bloodType.setText("")
        binding.allergies.setText("")
        binding.medicalConditions.setText("")
        binding.height.setText("")
        binding.weight.setText("")
        binding.sport.setText("")
        binding.position.setText("")
        binding.coachNotes.setText("")
    }

    private fun createAthleteInfo(): AthleteInfo {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val emergencyCascade = EmergencyContactCascade(
            primaryContact = binding.emergencyContact.text.toString().trim(),
            secondaryContact = "+1234567890", // Default secondary
            teamPhysician = "+1234567891", // Default physician
            athleticDirector = "+1234567892", // Default AD
            lastUpdated = dateFormat.format(Date())
        )
        return AthleteInfo(
            athleteId = binding.athleteId.text.toString().trim(),
            fullName = binding.fullName.text.toString().trim(),
            dateOfBirth = binding.dateOfBirth.text.toString().trim(),
            emergencyContact = binding.emergencyContact.text.toString().trim(),
            bloodType = binding.bloodType.text.toString().trim(),
            allergies = binding.allergies.text.toString().trim(),
            medicalConditions = binding.medicalConditions.text.toString().trim(),
            height = binding.height.text.toString().trim(),
            weight = binding.weight.text.toString().trim(),
            sport = binding.sport.text.toString().trim(),
            position = binding.position.text.toString().trim(),
            coachNotes = binding.coachNotes.text.toString().trim(),
            lastUpdated = dateFormat.format(Date()),
            emergencyCascade = emergencyCascade
        )
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray)
    }

    override fun onPause() {
        nfcAdapter?.disableForegroundDispatch(this)
        super.onPause()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        if (!validateInput()) {
            return
        }

        val athleteInfo = createAthleteInfo()
        val ndefMessage = athleteInfo.toNdefMessage()

        try {
            if (NfcAdapter.ACTION_TECH_DISCOVERED == intent.action ||
                NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action
            ) {
                val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG) ?: return
                val ndef = Ndef.get(tag)

                if (ndef != null) {
                    ndef.connect()
                    if (ndef.isWritable) {
                        ndef.writeNdefMessage(ndefMessage)
                        showSuccessDialog(athleteInfo)
                    } else {
                        Toast.makeText(this, " Tag is read-only! Cannot write data.", Toast.LENGTH_LONG).show()
                    }
                    ndef.close()
                } else {
                    val format = NdefFormatable.get(tag)
                    if (format != null) {
                        format.connect()
                        format.format(ndefMessage)
                        format.close()
                        showSuccessDialog(athleteInfo)
                    } else {
                        Toast.makeText(this, " Tag does not support NDEF format", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, " Error writing to NFC: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showSuccessDialog(athleteInfo: AthleteInfo) {
        runOnUiThread {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(" Athlete Data Written Successfully!")
                .setMessage(
                    "Athlete: ${athleteInfo.fullName}\n" +
                            "ID: ${athleteInfo.athleteId}\n" +
                            "Sport: ${athleteInfo.sport}\n\n" +
                            "The NFC tag now contains this athlete's information and can be scanned by coaches."
                )
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                    clearAllFields()
                }
                .setCancelable(false)
                .show()
        }
    }
}