package com.example.athletesync
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import com.example.athletesync.databinding.DialogAddContactBinding

class EditContactDialog(
    private val contact: EmergencyContact,
    private val onContactUpdated: (EmergencyContact) -> Unit
) : DialogFragment() {

    private lateinit var binding: DialogAddContactBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogAddContactBinding.inflate(layoutInflater)

        setupRoleSpinner()
        populateFields()

        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .setTitle("Edit Contact")
            .setPositiveButton("Update") { _, _ ->
                if (validateForm()) {
                    updateContact()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
    }

    private fun setupRoleSpinner() {
        val roles = arrayOf(
            "Parent",
            "Guardian",
            "Family Member",
            "Personal Doctor",
            "Other"
        )

        ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, roles).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerRole.adapter = adapter
        }
    }

    private fun populateFields() {
        binding.etName.setText(contact.name)
        binding.etPhone.setText(contact.phone)

        // Fix the type casting issue
        val adapter = binding.spinnerRole.adapter as? ArrayAdapter<String>
        if (adapter != null) {
            val rolePosition = adapter.getPosition(contact.role)
            if (rolePosition >= 0) {
                binding.spinnerRole.setSelection(rolePosition)
            }
        } else {
            // Fallback: Find position manually
            val roles = arrayOf("Parent", "Guardian", "Family Member", "Personal Doctor", "Other")
            val position = roles.indexOf(contact.role)
            if (position >= 0) {
                binding.spinnerRole.setSelection(position)
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        if (binding.etName.text.toString().trim().isEmpty()) {
            binding.etName.error = "Enter contact name"
            isValid = false
        }

        if (binding.etPhone.text.toString().trim().isEmpty()) {
            binding.etPhone.error = "Enter phone number"
            isValid = false
        }

        return isValid
    }

    private fun updateContact() {
        val updatedContact = contact.copy(
            name = binding.etName.text.toString().trim(),
            phone = binding.etPhone.text.toString().trim(),
            role = binding.spinnerRole.selectedItem.toString()
        )

        onContactUpdated(updatedContact)
    }
}