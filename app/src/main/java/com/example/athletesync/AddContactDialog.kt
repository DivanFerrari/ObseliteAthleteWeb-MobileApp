package com.example.athletesync

class AddContactDialog(
    private val onContactAdded: (EmergencyContact) -> Unit
) : DialogFragment() {

    private lateinit var binding: DialogAddContactBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogAddContactBinding.inflate(layoutInflater)

        setupRoleSpinner()

        val dialog = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .setTitle("Add Player Contact")
            .setPositiveButton("Add") { _, _ ->
                if (validateForm()) {
                    addContact()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        // Set black background for the dialog
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.BLACK))
        dialog.setOnShowListener {
            // Style the buttons
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(Color.WHITE)
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(Color.WHITE)
        }

        return dialog
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

    private fun validateForm(): Boolean {
        var isValid = true

        if (binding.etName.text.toString().trim().isEmpty()) {
            binding.etName.error = "Enter contact name"
            isValid = false
        } else {
            binding.etName.error = null
        }

        if (binding.etPhone.text.toString().trim().isEmpty()) {
            binding.etPhone.error = "Enter phone number"
            isValid = false
        } else {
            binding.etPhone.error = null
        }

        return isValid
    }

    private fun addContact() {
        val contact = EmergencyContact(
            id = System.currentTimeMillis().toString(),
            name = binding.etName.text.toString().trim(),
            phone = binding.etPhone.text.toString().trim(),
            role = binding.spinnerRole.selectedItem.toString(),
            isPlayerContact = true
        )

        onContactAdded(contact)
    }
}