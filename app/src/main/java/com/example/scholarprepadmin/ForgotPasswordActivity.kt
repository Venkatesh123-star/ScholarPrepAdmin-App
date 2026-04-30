package com.example.scholarprepadmin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.scholarprepadmin.databinding.ActivityForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance().reference

        binding.back.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.reset.setOnClickListener {
            resetPassword()
        }
    }
    private fun resetPassword() {

        val email = binding.eEmail.text.toString().trim().lowercase()

        if (email.isEmpty()) {
            Toast.makeText(this, "Enter email", Toast.LENGTH_SHORT).show()
            binding.eEmail.requestFocus()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Enter valid email", Toast.LENGTH_SHORT).show()
            binding.eEmail.requestFocus()
            return
        }

        db.child("admins")
            .get()
            .addOnSuccessListener { snapshot ->

                var emailFound = false
                var isApproved = false

                for (snap in snapshot.children) {

                    val dbEmail = snap.child("email").value.toString().trim().lowercase()
                    val status = snap.child("status").value.toString()

                    if (dbEmail == email) {
                        emailFound = true

                        if (status == "approved") {
                            isApproved = true
                        }
                        break
                    }
                }

                if (!emailFound) {
                    Toast.makeText(this, "Email not found. Please register first.", Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }

                if (!isApproved) {
                    Toast.makeText(this, "Admin not approved yet", Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }
                auth.sendPasswordResetEmail(email)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Reset link sent to email", Toast.LENGTH_LONG).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Auth error: ${e.message}", Toast.LENGTH_LONG).show()
                    }

            }
            .addOnFailureListener {
                Toast.makeText(this, "Database error", Toast.LENGTH_SHORT).show()
            }
    }
}