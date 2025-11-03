package com.example.athletesync

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.athletesync.databinding.ActivityEmergencyDashboardBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

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

        binding.tvIncidentSummary.text = "$totalCount Total • $activeCount Active • $resolvedCount Resolved"
    }

    private fun startQuickIncidentReport() {
        val dialog = QuickIncidentDialog { report ->
            saveQuickIncidentReport(report)
        }
        dialog.show(supportFragmentManager, "QuickIncidentDialog")
    }

    private fun saveQuickIncidentReport(report: QuickIncidentReport) {
        db.collection("incident_reports")
            .document(report.reportId)
            .set(report)
            .addOnSuccessListener {
                Toast.makeText(this, "Incident reported successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to report incident: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showIncidentActions(incident: QuickIncidentReport) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Incident: ${incident.incidentType}")
            .setMessage("Athlete: ${incident.athleteName}\nSeverity: ${incident.severity}\nTime: ${incident.timestamp}\n\n${incident.description}")
            .setPositiveButton("Mark Resolved") { _, _ ->
                updateIncidentStatus(incident.reportId, "Resolved")
            }
            .setNeutralButton("View Contacts") { _, _ ->
                val intent = Intent(this, EnhancedContactsActivity::class.java)
                startActivity(intent)
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun updateIncidentStatus(reportId: String, status: String) {
        db.collection("incident_reports")
            .document(reportId)
            .update("status", status)
            .addOnSuccessListener {
                Toast.makeText(this, "Incident marked as $status", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update status: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        incidentListener?.remove()
    }
}