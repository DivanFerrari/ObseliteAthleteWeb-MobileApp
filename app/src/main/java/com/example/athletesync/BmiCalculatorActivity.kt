package com.example.athletesync

import android.graphics.Color
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.athletesync.databinding.ActivityBmiCalculatorBinding
import kotlin.math.pow

class BmiCalculatorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBmiCalculatorBinding

    // Modern back-handler (replaces onBackPressed())
    private val backCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBmiCalculatorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Add the new dispatcher callback
        onBackPressedDispatcher.addCallback(this, backCallback)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.calculateButton.setOnClickListener { calculateBMI() }
        binding.resetButton.setOnClickListener { resetCalculator() }

        // Back button in UI
        binding.backButton.setOnClickListener {
            backCallback.handleOnBackPressed() // reuse same logic + animation
        }
    }

    private fun calculateBMI() {
        val heightText = binding.heightInput.text.toString().trim()
        val weightText = binding.weightInput.text.toString().trim()

        if (heightText.isEmpty() || weightText.isEmpty()) {
            Toast.makeText(this, "⚠️ Please enter both height and weight", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val height = heightText.toFloat() / 100 // cm → m
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
        binding.bmiResult.text = "Your BMI: ${String.format("%.1f", bmi)}"

        val (category, description, color) = getBMICategory(bmi)
        binding.bmiCategory.text = "Category: $category"
        binding.bmiCategory.setTextColor(color)
        binding.bmiDescription.text = description

        binding.resultsCard.visibility = android.view.View.VISIBLE

        // Smooth scroll to results
        binding.resultsCard.post {
            binding.resultsCard.requestFocus()
        }
    }

    private fun getBMICategory(bmi: Float): Triple<String, String, Int> = when {
        bmi < 18.5 -> Triple(
            "Underweight",
            "You may need to gain weight. Consider consulting a nutritionist for a healthy weight gain plan.",
            Color.parseColor("#FF9800")
        )
        bmi < 25 -> Triple(
            "Normal Weight",
            "Great! You're in the healthy weight range. Maintain your current lifestyle with balanced nutrition and regular exercise.",
            Color.parseColor("#4CAF50")
        )
        bmi < 30 -> Triple(
            "Overweight",
            "Consider adopting a healthier lifestyle with balanced diet and regular physical activity.",
            Color.parseColor("#FFC107")
        )
        else -> Triple(
            "Obese",
            "It's recommended to consult with a healthcare professional for guidance on weight management and healthy lifestyle changes.",
            Color.parseColor("#F44336")
        )
    }

    private fun resetCalculator() {
        binding.heightInput.text?.clear()
        binding.weightInput.text?.clear()
        binding.resultsCard.visibility = android.view.View.GONE

        binding.heightInput.clearFocus()
        binding.weightInput.clearFocus()

        (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(binding.heightInput.windowToken, 0)
    }

    override fun onDestroy() {
        backCallback.remove() // clean up
        super.onDestroy()
    }
}