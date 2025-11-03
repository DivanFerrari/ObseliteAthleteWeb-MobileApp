package com.example.athletesync


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class DashboardActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var weatherManager: WeatherManager

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (!allGranted) {
            android.widget.Toast.makeText(
                this,
                "Some emergency features may not work without permissions",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
        // Setup weather regardless of permissions since we use hardcoded coordinates
        setupWeatherCard()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)

        auth = FirebaseAuth.getInstance()
        weatherManager = WeatherManager(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize weather display
        setupWeatherCard()

        // Request permissions (weather will work regardless)
        requestEmergencyPermissions()

        // Setup click listeners
        setupClickListeners()

        // Display user email in app name
        displayUserInfo()
    }

    private fun setupWeatherCard() {
        // Show loading state with Port Elizabeth as default location
        showWeatherLoading()

        weatherManager.getCurrentWeather(object : WeatherManager.WeatherCallback {
            override fun onWeatherReceived(weatherData: WeatherData) {
                runOnUiThread {
                    Log.d("Dashboard", "Weather received: ${weatherData.temperature}°C in ${weatherData.city}")
                    updateWeatherUI(weatherData)
                }
            }

            override fun onError(message: String) {
                runOnUiThread {
                    Log.e("Dashboard", "Weather error: $message")
                    showWeatherError(message)
                }
            }
        })
    }

    private fun showWeatherLoading() {
        findViewById<TextView>(R.id.weatherTemperature).text = "--°C"
        findViewById<TextView>(R.id.weatherCondition).text = "Loading weather..."
        findViewById<TextView>(R.id.weatherLocation).text = "Port Elizabeth" // Default to PE
        findViewById<TextView>(R.id.weatherHumidity).text = "💧 --%"
        findViewById<TextView>(R.id.weatherWind).text = "💨 -- km/h"
    }

    private fun updateWeatherUI(weatherData: WeatherData) {
        try {
            findViewById<TextView>(R.id.weatherTemperature).text = "${weatherData.temperature}°C"
            findViewById<TextView>(R.id.weatherCondition).text = weatherData.description.replaceFirstChar { it.uppercase() }

            // Always show Port Elizabeth as location to avoid wrong city names
            findViewById<TextView>(R.id.weatherLocation).text = "Port Elizabeth, ZA"

            findViewById<TextView>(R.id.weatherHumidity).text = "💧 ${weatherData.humidity}%"
            findViewById<TextView>(R.id.weatherWind).text = "💨 ${weatherData.windSpeed} km/h"

            // Set weather icon based on condition
            setWeatherIcon(weatherData.iconCode, weatherData.condition)

            Log.d("Dashboard", "Weather UI updated: ${weatherData.temperature}°C")

        } catch (e: Exception) {
            Log.e("Dashboard", "Error updating weather UI: ${e.message}")
        }
    }

    private fun setWeatherIcon(iconCode: String, condition: String) {
        val weatherIcon = findViewById<ImageView>(R.id.weatherIcon)
        val iconResource = when {
            iconCode.contains("01d") -> R.drawable.ic_sunny
            iconCode.contains("01n") -> R.drawable.ic_clear_night
            iconCode.contains("02") -> R.drawable.ic_partly_cloudy
            iconCode.contains("03") || iconCode.contains("04") -> R.drawable.ic_cloudy
            iconCode.contains("09") || iconCode.contains("10") -> R.drawable.ic_rainy
            iconCode.contains("11") -> R.drawable.ic_storm
            iconCode.contains("13") -> R.drawable.ic_snow
            iconCode.contains("50") -> R.drawable.ic_fog
            else -> when {
                condition.contains("rain", true) -> R.drawable.ic_rainy
                condition.contains("storm", true) || condition.contains("thunder", true) -> R.drawable.ic_storm
                condition.contains("snow", true) -> R.drawable.ic_snow
                condition.contains("cloud", true) -> R.drawable.ic_cloudy
                condition.contains("clear", true) -> R.drawable.ic_sunny
                else -> R.drawable.ic_weather_default
            }
        }
        weatherIcon.setImageResource(iconResource)
    }

    private fun setupWeatherCardRefresh() {
        val weatherCard = findViewById<com.google.android.material.card.MaterialCardView>(R.id.weatherCard)
        weatherCard.setOnClickListener {
            // Show refreshing state
            findViewById<TextView>(R.id.weatherCondition).text = "Refreshing..."
            setupWeatherCard()

            // Show refresh confirmation
            Snackbar.make(weatherCard, "Refreshing weather data...", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun showWeatherError(message: String) {
        findViewById<TextView>(R.id.weatherCondition).text = "Weather unavailable"
        findViewById<TextView>(R.id.weatherTemperature).text = "--°C"
        findViewById<TextView>(R.id.weatherLocation).text = "Port Elizabeth" // Default to PE on error


        Snackbar.make(findViewById(R.id.weatherCard), "Weather: $message", Snackbar.LENGTH_LONG).show()
    }

    private fun setupClickListeners() {

        val chatbot: CardView = findViewById(R.id.FitnessBuddyChat)
        chatbot.setOnClickListener {
            val intent = Intent(this, ChatBotActivity::class.java)
            startActivity(intent)
        }

        val write: CardView = findViewById(R.id.writenfc)
        write.setOnClickListener {
            val intent = Intent(this, WriteNFC::class.java)
            startActivity(intent)
        }

        val read: CardView = findViewById(R.id.readnfc)
        read.setOnClickListener {
            val intent = Intent(this, ReadNFC::class.java)
            startActivity(intent)
        }

        val emergencyProtocols: CardView = findViewById(R.id.emergencyProtocolsCard)
        emergencyProtocols.setOnClickListener {
            val intent = Intent(this, EmergencyDashboardActivity::class.java)
            startActivity(intent)
        }

        val emergencyContacts: CardView = findViewById(R.id.emergencyContactsCard)
        emergencyContacts.setOnClickListener {
            val intent = Intent(this, EnhancedContactsActivity::class.java)
            startActivity(intent)
        }

        // BMI Calculator - NEW FEATURE
        val bmiCalculator: CardView = findViewById(R.id.bmiCalculatorCard)
        bmiCalculator.setOnClickListener {
            val intent = Intent(this, BmiCalculatorActivity::class.java)
            startActivity(intent)
        }

        val logoutButton: androidx.appcompat.widget.AppCompatButton = findViewById(R.id.logoutButton)
        logoutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        val practiceSchedule: CardView = findViewById(R.id.practiceScheduleCard)
        practiceSchedule.setOnClickListener {
            val intent = Intent(this, PracticeScheduleActivity::class.java)
            startActivity(intent)
        }
    }

    private fun displayUserInfo() {
        val currentUser = auth.currentUser
        currentUser?.email?.let { email ->
            val appNameTextView: TextView = findViewById(R.id.AppName)
            appNameTextView.text = "AthleteSync - ${email.substringBefore("@")}"
        }
    }

    private fun requestEmergencyPermissions() {
        val permissions = arrayOf(
            Manifest.permission.SEND_SMS,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.SEND_SMS) ||
                shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {

                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("App Features Need Permissions")
                    .setMessage("AthleteSync needs permissions for:\n• SMS/Call - Emergency contacts\n• Location - Other app features\n• NFC - Tag reading/writing")
                    .setPositiveButton("Grant Permissions") { _, _ ->
                        requestPermissionLauncher.launch(missingPermissions.toTypedArray())
                    }
                    .setNegativeButton("Cancel") { _, _ ->
                        // Weather will work even without location permissions
                        setupWeatherCard()
                    }
                    .show()
            } else {
                requestPermissionLauncher.launch(missingPermissions.toTypedArray())
            }
        } else {
            // Permissions already granted
            setupWeatherCard()
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        setupWeatherCard()
    }
}