package com.example.athletesync

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.athletesync.databinding.ActivityPracticeScheduleBinding
import com.example.athletesync.databinding.ItemPracticeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.*

class PracticeScheduleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPracticeScheduleBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private val reminderOptions = arrayOf(
        "No reminder",
        "15 minutes before",
        "30 minutes before",
        "1 hour before",
        "2 hours before",
        "1 day before"
    )

    private val reminderMinutes = mapOf(
        "No reminder" to 0,
        "15 minutes before" to 15,
        "30 minutes before" to 30,
        "1 hour before" to 60,
        "2 hours before" to 120,
        "1 day before" to 1440
    )

    private var selectedDate: Calendar = Calendar.getInstance()
    private var selectedTime: Calendar = Calendar.getInstance()
    private var practicesListener: ListenerRegistration? = null
    private val practices = mutableListOf<PracticeSchedule>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPracticeScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupToolbar()
        setupUI()
        setupClickListeners()
        loadPractices()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        binding.toolbar.navigationIcon?.setTint(ContextCompat.getColor(this, android.R.color.white))

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupUI() {
        val adapter = ArrayAdapter(this, R.layout.spinner_selected_item, reminderOptions).apply {
            setDropDownViewResource(R.layout.spinner_dropdown_item)
        }
        binding.reminderSpinner.adapter = adapter

        binding.reminderSpinner.setSelection(0)

        selectedTime.add(Calendar.HOUR, 1)
        updateTimeDisplay()

        updateDateDisplay()


        binding.practicesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.practicesRecyclerView.adapter = PracticeAdapter(practices)
    }
    private fun setupClickListeners() {
        binding.btnSchedulePractice.setOnClickListener { showScheduleForm() }
        binding.btnViewPractices.setOnClickListener { showPracticesList() }
        binding.dateInput.setOnClickListener { showDatePicker() }
        binding.timeInput.setOnClickListener { showTimePicker() }
        binding.scheduleButton.setOnClickListener { schedulePractice() }
    }

    private fun showScheduleForm() {
        binding.scheduleFormSection.visibility = View.VISIBLE
        binding.practicesListSection.visibility = View.GONE

        binding.btnSchedulePractice.apply {
            backgroundTintList = ContextCompat.getColorStateList(this@PracticeScheduleActivity, android.R.color.holo_red_dark)
            setTextColor(ContextCompat.getColor(this@PracticeScheduleActivity, android.R.color.white))
        }

        binding.btnViewPractices.apply {
            backgroundTintList = ContextCompat.getColorStateList(this@PracticeScheduleActivity, android.R.color.transparent)
            setTextColor(ContextCompat.getColor(this@PracticeScheduleActivity, android.R.color.darker_gray))
            setStrokeColorResource(android.R.color.darker_gray)
        }
    }

    private fun showPracticesList() {
        binding.scheduleFormSection.visibility = View.GONE
        binding.practicesListSection.visibility = View.VISIBLE

        binding.btnViewPractices.apply {
            backgroundTintList = ContextCompat.getColorStateList(this@PracticeScheduleActivity, android.R.color.holo_red_dark)
            setTextColor(ContextCompat.getColor(this@PracticeScheduleActivity, android.R.color.white))
        }

        binding.btnSchedulePractice.apply {
            backgroundTintList = ContextCompat.getColorStateList(this@PracticeScheduleActivity, android.R.color.transparent)
            setTextColor(ContextCompat.getColor(this@PracticeScheduleActivity, android.R.color.darker_gray))
            setStrokeColorResource(android.R.color.darker_gray)
        }
    }

    private fun showDatePicker() {
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                selectedDate.set(year, month, dayOfMonth)
                updateDateDisplay()
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = System.currentTimeMillis() - 1000
            show()
        }
    }

    private fun showTimePicker() {
        TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
                selectedTime.set(Calendar.MINUTE, minute)
                updateTimeDisplay()
            },
            selectedTime.get(Calendar.HOUR_OF_DAY),
            selectedTime.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun updateDateDisplay() {
        val dateFormat = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
        binding.dateInput.setText(dateFormat.format(selectedDate.time))
    }

    private fun updateTimeDisplay() {
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        binding.timeInput.setText(timeFormat.format(selectedTime.time))
    }

    private fun loadPractices() {
        val currentUser = auth.currentUser ?: run {
            Toast.makeText(this, "Please log in to view practices", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        practicesListener = db.collection("practices")
            .whereEqualTo("coachId", currentUser.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                practices.clear()
                snapshot?.documents?.forEach { doc ->
                    doc.toObject(PracticeSchedule::class.java)?.let { practices.add(it) }
                }

                practices.sortWith(compareBy({ it.date }, { it.time }))
                binding.practicesRecyclerView.adapter?.notifyDataSetChanged()

                binding.emptyState.visibility = if (practices.isEmpty()) View.VISIBLE else View.GONE
                binding.practicesRecyclerView.visibility = if (practices.isEmpty()) View.GONE else View.VISIBLE
            }
    }

    private fun schedulePractice() {
        val title = binding.titleInput.text.toString().trim()
        val location = binding.locationInput.text.toString().trim()
        val notes = binding.notesInput.text.toString().trim()
        val reminderOption = binding.reminderSpinner.selectedItem.toString()
        val reminderMinutes = reminderMinutes[reminderOption] ?: 0

        if (title.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        val formattedDate = dateFormat.format(selectedDate.time)
        val formattedTime = timeFormat.format(selectedTime.time)

        val currentUser = auth.currentUser ?: run {
            Toast.makeText(this, "Please log in to schedule practices", Toast.LENGTH_SHORT).show()
            return
        }

        val practice = PracticeSchedule(
            id = db.collection("practices").document().id,
            title = title,
            date = formattedDate,
            time = formattedTime,
            location = location,
            notes = notes,
            reminderMinutes = reminderMinutes,
            coachId = currentUser.uid,
            createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        )

        binding.scheduleButton.text = "SCHEDULING..."
        binding.scheduleButton.isEnabled = false

        db.collection("practices").document(practice.id).set(practice)
            .addOnSuccessListener {
                Toast.makeText(this, "Practice scheduled successfully!", Toast.LENGTH_SHORT).show()
                resetForm()
                showPracticesList()
                if (reminderMinutes > 0) setReminder(practice)
            }
            .addOnFailureListener {
                binding.scheduleButton.text = "SCHEDULE PRACTICE"
                binding.scheduleButton.isEnabled = true
                Toast.makeText(this, "Failed to schedule practice", Toast.LENGTH_SHORT).show()
            }
    }

    private fun resetForm() {
        binding.titleInput.text?.clear()
        binding.locationInput.text?.clear()
        binding.notesInput.text?.clear()
        selectedDate = Calendar.getInstance()
        selectedTime = Calendar.getInstance().apply { add(Calendar.HOUR, 1) }
        updateDateDisplay()
        updateTimeDisplay()
        binding.reminderSpinner.setSelection(0)
        binding.scheduleButton.text = "SCHEDULE PRACTICE"
        binding.scheduleButton.isEnabled = true
    }

    private fun setReminder(practice: PracticeSchedule) {
        try {
            val practiceCalendar = Calendar.getInstance().apply {
                time = selectedDate.time
                set(Calendar.HOUR_OF_DAY, selectedTime.get(Calendar.HOUR_OF_DAY))
                set(Calendar.MINUTE, selectedTime.get(Calendar.MINUTE))
                set(Calendar.SECOND, 0)
            }

            val reminderTime = practiceCalendar.timeInMillis - (practice.reminderMinutes * 60 * 1000L)
            if (reminderTime > System.currentTimeMillis()) {
                val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val intent = Intent(this, PracticeReminderReceiver::class.java).apply {
                    putExtra("practice_title", practice.title)
                    putExtra("practice_location", practice.location)
                    putExtra("practice_time", practice.time)
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    this,
                    practice.id.hashCode(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent)
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent)
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        practicesListener?.remove()
    }

    @SuppressLint("GestureBackNavigation")
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private inner class PracticeAdapter(private val practices: List<PracticeSchedule>) :
        RecyclerView.Adapter<PracticeAdapter.PracticeViewHolder>() {

        inner class PracticeViewHolder(private val binding: ItemPracticeBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(practice: PracticeSchedule) {
                val date = try {
                    val input = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(practice.date)
                    SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(input!!)
                } catch (e: Exception) { practice.date }

                val time = try {
                    val input = SimpleDateFormat("HH:mm", Locale.getDefault()).parse(practice.time)
                    SimpleDateFormat("h:mm a", Locale.getDefault()).format(input!!)
                } catch (e: Exception) { practice.time }

                binding.practiceTitle.text = practice.title
                binding.practiceDate.text = date
                binding.practiceTime.text = time
                binding.practiceLocation.text = practice.location

                if (practice.notes.isNotEmpty()) {
                    binding.practiceNotes.text = practice.notes
                    binding.practiceNotes.visibility = View.VISIBLE
                } else {
                    binding.practiceNotes.visibility = View.GONE
                }

                if (practice.reminderMinutes > 0) {
                    val reminderText = when (practice.reminderMinutes) {
                        15 -> "15 min before"
                        30 -> "30 min before"
                        60 -> "1 hour before"
                        120 -> "2 hours before"
                        1440 -> "1 day before"
                        else -> "Reminder set"
                    }
                    binding.reminderText.text = "Reminder: $reminderText"
                    binding.reminderInfo.visibility = View.VISIBLE
                } else {
                    binding.reminderInfo.visibility = View.GONE
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PracticeViewHolder {
            val binding = ItemPracticeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return PracticeViewHolder(binding)
        }

        override fun onBindViewHolder(holder: PracticeViewHolder, position: Int) {
            holder.bind(practices[position])
        }

        override fun getItemCount() = practices.size
    }
}