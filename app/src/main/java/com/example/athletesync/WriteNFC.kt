package com.example.athletesync

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.athletesync.databinding.ActivityWriteNfcBinding
import java.nio.charset.Charset

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

        binding.backButton.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
        }


        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)
        } else {
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        }


        val ndefFilter = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
        try {
            ndefFilter.addDataType("text/plain")
        } catch (e: IntentFilter.MalformedMimeTypeException) {
            throw RuntimeException("Failed to add MIME type", e)
        }
        intentFiltersArray = arrayOf(ndefFilter)


        if (nfcAdapter == null) {
            Toast.makeText(this, "❌ NFC not supported on this device", Toast.LENGTH_SHORT).show()
        } else if (!nfcAdapter!!.isEnabled) {
            Toast.makeText(this, "⚠️ Please turn on NFC", Toast.LENGTH_SHORT).show()
        }
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

        val bloodType = binding.bloodtype.text.toString()
        val height = binding.height.text.toString()
        val weight = binding.weight.text.toString()

        if (bloodType.isBlank() || height.isBlank() || weight.isBlank()) {
            Toast.makeText(this, "⚠️ Please fill in all fields before writing to NFC", Toast.LENGTH_SHORT).show()
            return
        }

        val messageString = "Blood Type: $bloodType\nHeight: $height\nWeight: $weight"
        val ndefMessage = NdefMessage(arrayOf(NdefRecord.createTextRecord("en", messageString)))

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
                        Toast.makeText(this, "✅ NFC Tag Written Successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "❌ Tag is read-only!", Toast.LENGTH_SHORT).show()
                    }
                    ndef.close()
                } else {
                    val format = NdefFormatable.get(tag)
                    if (format != null) {
                        format.connect()
                        format.format(ndefMessage)
                        format.close()
                        Toast.makeText(this, "✅ Tag formatted and written!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "❌ Tag does not support NDEF", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "⚠️ Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
