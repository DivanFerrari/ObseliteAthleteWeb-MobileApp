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