package com.example.athletesync

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.pow

class BmiCalculatorActivity : AppCompatActivity() {

    private lateinit var binding:

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBmiCalculatorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.calculateButton.setOnClickListener {
            calculateBMI()
        }

        binding.resetButton.setOnClickListener {
            resetCalculator()
        }

        // Add back button click listener
        binding.backButton.setOnClickListener {
            onBackPressed()
        }
    }

    private fun calculateBMI() {
        val heightText = binding.heightInput.text.toString()
        val weightText = binding.weightInput.text.toString()

        if (heightText.isEmpty() || weightText.isEmpty()) {
            Toast.makeText(this, "⚠️ Please enter both height and weight", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val height = heightText.toFloat() / 100 // Convert cm to meters
            val weight = weightText.toFloat()

            if (height <= 0 || weight <= 0) {
                Toast.makeText(this, "⚠️ Please enter valid positive numbers", Toast.LENGTH_SHORT).show()
                return
            }

            val bmi = weight / (height.pow(2))
            displayResults(bmi)

        } catch (e: NumberFormatException) {
            Toast.makeText(this, "⚠️ Please enter valid numbers", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayResults(bmi: Float) {
        val formattedBMI = String.format("%.1f", bmi)
        binding.bmiResult.text = "Your BMI: $formattedBMI"

        val (category, description, color) = getBMICategory(bmi)
        binding.bmiCategory.text = "Category: $category"
        binding.bmiCategory.setTextColor(color)
        binding.bmiDescription.text = description

        // Show results card
        binding.resultsCard.visibility = android.view.View.VISIBLE

        // Scroll to results
        binding.resultsCard.post {
            binding.resultsCard.requestFocus()
        }
    }

    private fun getBMICategory(bmi: Float): Triple<String, String, Int> {
        return when {
            bmi < 18.5 -> Triple(
                "Underweight",
                "You may need to gain weight. Consider consulting a nutritionist for a healthy weight gain plan.",
                android.graphics.Color.parseColor("#FF9800") // Orange
            )
            bmi < 25 -> Triple(
                "Normal Weight",
                "Great! You're in the healthy weight range. Maintain your current lifestyle with balanced nutrition and regular exercise.",
                android.graphics.Color.parseColor("#4CAF50") // Green
            )
            bmi < 30 -> Triple(
                "Overweight",
                "Consider adopting a healthier lifestyle with balanced diet and regular physical activity.",
                android.graphics.Color.parseColor("#FFC107") // Amber
            )
            else -> Triple(
                "Obese",
                "It's recommended to consult with a healthcare professional for guidance on weight management and healthy lifestyle changes.",
                android.graphics.Color.parseColor("#F44336") // Red
            )
        }
    }

    private fun resetCalculator() {
        binding.heightInput.text?.clear()
        binding.weightInput.text?.clear()
        binding.resultsCard.visibility = android.view.View.GONE

        // Clear focus and hide keyboard
        binding.heightInput.clearFocus()
        binding.weightInput.clearFocus()

        val inputMethodManager = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.heightInput.windowToken, 0)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}