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
