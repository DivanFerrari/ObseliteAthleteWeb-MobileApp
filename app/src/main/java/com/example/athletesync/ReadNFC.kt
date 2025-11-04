package com.example.athletesync

import android.R.attr.button
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class ReadNFC : AppCompatActivity() {
    private lateinit var textView: TextView

    private val nfcAdapter: NfcAdapter? by lazy {
        NfcAdapter.getDefaultAdapter(this)
    }
    private var pendingIntent: PendingIntent? = null
    private var intentFiltersArray: Array<IntentFilter>? = null


    private val techListsArray = arrayOf(
        arrayOf(Ndef::class.java.name),
        arrayOf(NdefFormatable::class.java.name)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read_nfc)

        textView = findViewById(R.id.text_view)


        val back = findViewById<ImageButton>(R.id.backButton)

        back.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
        }


        // Check NFC availability
        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC not supported", Toast.LENGTH_SHORT).show()
        } else if (!nfcAdapter!!.isEnabled) {
            Toast.makeText(this, "Please turn on NFC", Toast.LENGTH_SHORT).show()
        }

        // Prepare pending intent for NFC detection
        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)
        } else {
            PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )
        }

        val ndef = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
        try {
            ndef.addDataType("text/plain")
        } catch (e: IntentFilter.MalformedMimeTypeException) {
            throw RuntimeException("fail", e)
        }
        intentFiltersArray = arrayOf(ndef)
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

        if (this.isFinishing) {
            nfcAdapter?.disableForegroundDispatch(this)
        }
        super.onPause()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action || NfcAdapter.ACTION_TECH_DISCOVERED == intent.action) {
            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG) ?: return
            val ndef = Ndef.get(tag) ?: return

            try {
                ndef.connect()
                val ndefMessage = ndef.cachedNdefMessage
                val records = ndefMessage.records

                if (records.isNotEmpty()) {

                    val messageRecord = records[0]
                    val message = String(messageRecord.payload).drop(3)
                    textView.text = message
                }

                ndef.close()
            } catch (e: Exception) {
                Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
