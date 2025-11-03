package com.example.athletesync

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class EmergencyDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmergencyDashboardBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var incidentAdapter: ActiveIncidentsAdapter
    private var incidentListener: ListenerRegistration? = null
    private var activeIncidents = mutableListOf<QuickIncidentReport>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmergencyDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        setupUI()
        setupRealTimeIncidentListener()
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener { finish() }

        incidentAdapter = ActiveIncidentsAdapter(
            onIncidentClick = { incident ->
                showIncidentActions(incident)
            }
        )

        binding.incidentsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@EmergencyDashboardActivity)
            adapter = incidentAdapter
            isNestedScrollingEnabled = true
        }

        binding.btnSubmitEmergency.setOnClickListener {
            startQuickIncidentReport()
        }
    }

    private fun setupRealTimeIncidentListener() {
        incidentListener = db.collection("incident_reports")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Toast.makeText(this, "Error loading incidents", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                activeIncidents.clear()
                snapshots?.forEach { document ->
                    val incident = document.toObject(QuickIncidentReport::class.java)
                    activeIncidents.add(incident)
                }
                incidentAdapter.submitList(activeIncidents.toList())
                updateIncidentSummary()
            }
    }

    private fun updateIncidentSummary() {
        val activeCount = activeIncidents.count { it.status == "Active" }
        val emergencyCount = activeIncidents.count { it.status == "Emergency" }
        val resolvedCount = activeIncidents.count { it.status == "Resolved" }
        val totalCount = activeIncidents.size