package com.example.athletesync

class ActiveIncidentsAdapter(
    private val onIncidentClick: (QuickIncidentReport) -> Unit
) : ListAdapter<QuickIncidentReport, ActiveIncidentsAdapter.IncidentViewHolder>(IncidentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncidentViewHolder {
        val binding = ItemActiveIncidentBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return IncidentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IncidentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class IncidentViewHolder(private val binding: ItemActiveIncidentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(incident: QuickIncidentReport) {
            binding.tvIncidentType.text = incident.incidentType
            binding.tvAthleteName.text = incident.athleteName
            binding.tvTime.text = incident.timestamp
            binding.tvDescription.text = incident.description

            // Set severity with proper color mapping
            val severityColor = getSeverityColor(incident.severity)
            binding.severityIndicator.setBackgroundColor(severityColor)
            binding.tvSeverity.text = incident.severity.replaceFirstChar { it.uppercase() }
            binding.tvSeverity.setBackgroundColor(severityColor)
            binding.tvSeverity.setTextColor(android.graphics.Color.WHITE)

            // Status badge
            binding.tvStatus.text = incident.status
            binding.tvStatus.setBackgroundColor(getStatusColor(incident.status))
            binding.tvStatus.setTextColor(android.graphics.Color.WHITE)

            // Remove emergency button completely
            binding.btnEmergency.visibility = android.view.View.GONE

            itemView.setOnClickListener {
                onIncidentClick(incident)
            }
        }

        private fun getSeverityColor(severity: String): Int {
            return when (severity.lowercase()) {
                "critical" -> android.graphics.Color.parseColor("#D32F2F") // Dark Red
                "high" -> android.graphics.Color.parseColor("#F44336")     // Red
                "medium" -> android.graphics.Color.parseColor("#FF9800")   // Orange
                "low" -> android.graphics.Color.parseColor("#4CAF50")      // Green
                else -> android.graphics.Color.parseColor("#757575")       // Gray
            }
        }

        private fun getStatusColor(status: String): Int {
            return when (status.lowercase()) {
                "emergency" -> android.graphics.Color.parseColor("#D32F2F")
                "active" -> android.graphics.Color.parseColor("#2196F3")
                "resolved" -> android.graphics.Color.parseColor("#4CAF50")
                else -> android.graphics.Color.parseColor("#757575")
            }
        }
    }

    class IncidentDiffCallback : DiffUtil.ItemCallback<QuickIncidentReport>() {
        override fun areItemsTheSame(oldItem: QuickIncidentReport, newItem: QuickIncidentReport): Boolean {
            return oldItem.reportId == newItem.reportId
        }

        override fun areContentsTheSame(oldItem: QuickIncidentReport, newItem: QuickIncidentReport): Boolean {
            return oldItem == newItem
        }
    }
}