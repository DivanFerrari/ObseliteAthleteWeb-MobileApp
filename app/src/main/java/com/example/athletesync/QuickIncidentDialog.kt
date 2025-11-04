package com.example.athletesync

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.athletesync.databinding.DialogQuickIncidentBinding
import java.text.SimpleDateFormat
import java.util.*

class QuickIncidentDialog(
    private val onReportSubmit: (QuickIncidentReport) -> Unit
) : DialogFragment() {

    private lateinit var binding: DialogQuickIncidentBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogQuickIncidentBinding.inflate(layoutInflater)

        setupIncidentTypes()
        setupSeverityLevels()

        val dialog = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .setTitle("Quick Incident Report")
            .setPositiveButton("Report") { _, _ ->
                if (validateForm()) {
                    submitIncidentReport()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.BLACK))
        dialog.setOnShowListener {
            // Style the buttons
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(Color.WHITE)
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(Color.WHITE)
        }

        return dialog
    }

    private fun setupIncidentTypes() {
        val incidentTypes = arrayOf(
            "Minor Injury",
            "Head Injury/Concussion",
            "Sprain/Strain",
            "Fracture",
            "Heat Exhaustion",
            "Allergic Reaction",
            "Equipment Issue",
            "Behavioral Issue",
            "Other"
        )

        ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, incidentTypes).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerIncidentType.adapter = adapter
        }
    }

    private fun setupSeverityLevels() {
        val severityLevels = arrayOf("Low", "Medium", "High", "Critical")

        ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, severityLevels).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerSeverity.adapter = adapter
        }

        binding.spinnerSeverity.setSelection(1)
    }

    private fun validateForm(): Boolean {
        var isValid = true

        if (binding.etAthleteName.text.toString().trim().isEmpty()) {
            binding.etAthleteName.error = "Enter athlete name"
            isValid = false
        } else {
            binding.etAthleteName.error = null
        }

        if (binding.etDescription.text.toString().trim().isEmpty()) {
            binding.etDescription.error = "Enter incident description"
            isValid = false
        } else {
            binding.etDescription.error = null
        }

        return isValid
    }

    private fun submitIncidentReport() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val currentDateTime = dateFormat.format(Date())

        val report = QuickIncidentReport(
            reportId = generateReportId(),
            athleteName = binding.etAthleteName.text.toString().trim(),
            incidentType = binding.spinnerIncidentType.selectedItem.toString(),
            severity = binding.spinnerSeverity.selectedItem.toString(),
            description = binding.etDescription.text.toString().trim(),
            immediateAction = binding.etImmediateAction.text.toString().trim(),
            timestamp = currentDateTime,
            reporterName = "Coach",
            status = "Active"
        )

        onReportSubmit(report)
    }

    private fun generateReportId(): String {
        val timestamp = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
        return "IR$timestamp"
    }
}