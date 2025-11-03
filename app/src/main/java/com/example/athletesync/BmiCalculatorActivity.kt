package com.example.athletesync

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.pow


class BmiCalculatorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBmiCalculatorBinding

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