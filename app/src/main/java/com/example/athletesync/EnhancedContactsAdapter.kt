package com.example.athletesync

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.athletesync.databinding.ItemEnhancedContactBinding

class EnhancedContactsAdapter(
    private val onCallClick: (EmergencyContact) -> Unit,
    private val onMessageClick: (EmergencyContact) -> Unit,
    private val onEditClick: (EmergencyContact) -> Unit
) : ListAdapter<EmergencyContact, EnhancedContactsAdapter.ContactViewHolder>(ContactDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val binding = ItemEnhancedContactBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ContactViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ContactViewHolder(private val binding: ItemEnhancedContactBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(contact: EmergencyContact) {
            // Text
            binding.tvContactName.text = contact.name
            binding.tvContactRole.text = contact.role
            binding.tvContactPhone.text = contact.phone

            // Badge for SA Emergency
            binding.tvDefaultBadge.visibility = if (contact.isDefault) {
                android.view.View.VISIBLE
            } else {
                android.view.View.GONE
            }

            // Edit Button (icon)
            binding.btnEdit.visibility = if (contact.isPlayerContact) {
                android.view.View.VISIBLE
            } else {
                android.view.View.GONE
            }

            // Message button only enabled for player contacts
            binding.btnMessage.isEnabled = contact.isPlayerContact

            // === CALL ===
            binding.btnCall.setOnClickListener {
                onCallClick(contact)
            }

            // === MESSAGE ===
            binding.btnMessage.setOnClickListener {
                if (contact.isPlayerContact) {
                    onMessageClick(contact)
                }
            }

            // === EDIT ICON ===
            binding.btnEdit.setOnClickListener {
                onEditClick(contact)
            }

            // === ENTIRE CARD CLICK (only for player contacts) ===
            binding.cardRoot.setOnClickListener {
                if (contact.isPlayerContact) {
                    onEditClick(contact)
                }
            }

            // Optional: Add visual feedback on card tap
            binding.cardRoot.isClickable = contact.isPlayerContact
            binding.cardRoot.isFocusable = contact.isPlayerContact
        }
    }

    class ContactDiffCallback : DiffUtil.ItemCallback<EmergencyContact>() {
        override fun areItemsTheSame(oldItem: EmergencyContact, newItem: EmergencyContact): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: EmergencyContact, newItem: EmergencyContact): Boolean {
            return oldItem == newItem
        }
    }
}