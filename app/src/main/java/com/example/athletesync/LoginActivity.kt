package com.example.athletesync

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.athletesync.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlin.toString

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.loginButton.setOnClickListener {
            val email = binding.txtEmail.editText?.text.toString().trim()
            val password = binding.editTextTextPassword.editText?.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "⚠️ Please fill in all fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "✅ Login successful!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, DashboardActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        val message = when (task.exception) {
                            is FirebaseAuthInvalidUserException -> "❌ No account found with this email."
                            is FirebaseAuthInvalidCredentialsException -> "❌ Incorrect password. Try again."
                            else -> "❌ Login failed. Please try again later."
                        }
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    }
                }
        }

        binding.forgotPassword.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val view = layoutInflater.inflate(R.layout.dialog_forgot, null)
            val userEmail = view.findViewById<EditText>(R.id.editBox)

            builder.setView(view)
            val dialog = builder.create()

            view.findViewById<Button>(R.id.btnReset).setOnClickListener {
                val emailText = userEmail.text.toString().trim()
                if (emailText.isEmpty()) {
                    Toast.makeText(this, "⚠️ Please enter your email.", Toast.LENGTH_SHORT).show()
                } else if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
                    Toast.makeText(this, "⚠️ Please enter a valid email address.", Toast.LENGTH_SHORT).show()
                } else {
                    firebaseAuth.sendPasswordResetEmail(emailText)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this, "✅ Check your email to reset your password.", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this, "❌ Failed to send reset email. Try again later.", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
                dialog.dismiss()
            }

            view.findViewById<Button>(R.id.btnCancel).setOnClickListener {
                dialog.dismiss()
            }

            dialog.window?.setBackgroundDrawable(ColorDrawable(0))
            dialog.show()
        }

        binding.linkSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }
}