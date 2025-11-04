package com.example.athletesync

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.athletesync.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        setupListeners()
    }

    private fun setupListeners() {
        binding.signupButton.setOnClickListener {
            val username = binding.txtUsername.editText?.text.toString().trim()
            val email = binding.txtEmail.editText?.text.toString().trim()
            val password = binding.editTextTextPassword.editText?.text.toString().trim()

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please complete all fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Registration successful!", Toast.LENGTH_LONG).show()
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        val message = when (task.exception) {
                            is FirebaseAuthInvalidCredentialsException -> "Please enter a valid email address."
                            is FirebaseAuthUserCollisionException -> "An account with this email already exists."
                            is FirebaseAuthWeakPasswordException -> "Password is too weak. Use at least 6 characters."
                            else -> "Registration failed. Please try again later."
                        }
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                    }
                }
        }

        binding.LinkLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}
