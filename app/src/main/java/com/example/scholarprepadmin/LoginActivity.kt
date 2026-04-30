package com.example.scholarprepadmin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.scholarprepadmin.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {


    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance().reference

        binding.login.setOnClickListener {
            loginUser()
        }

        binding.newuser.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.ForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private fun loginUser() {
        val email = binding.etEmail.text.toString().trim()
        val pass = binding.etPassword.text.toString().trim()

        if (email.isEmpty()) {
            Toast.makeText(this, "Enter email", Toast.LENGTH_SHORT).show()
            return
        }

        if (pass.isEmpty()) {
            Toast.makeText(this, "Enter password", Toast.LENGTH_SHORT).show()
            return
        }

        db.child("admins")
            .get()
            .addOnSuccessListener { snapshot ->

                var userFound = false

                for (snap in snapshot.children) {

                    val dbEmail = snap.child("email").value.toString().trim()

                    if (dbEmail.equals(email, ignoreCase = true)) {

                        userFound = true

                        val dbPassword = snap.child("password").value.toString()
                        val dbStatus = snap.child("status").value.toString()
                        val username = snap.child("username").value

                        if (pass != dbPassword) {
                            Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show()
                            return@addOnSuccessListener
                        }

                        when (dbStatus) {

                            "approved" -> {
                                auth.signInWithEmailAndPassword(email, pass)
                                    .addOnSuccessListener {
                                        if (username == null) {
                                            Toast.makeText(this, "Login successful, update profile", Toast.LENGTH_SHORT).show()
                                            startActivity(Intent(this, ProfileActivity::class.java))
                                        } else {
                                            Toast.makeText(this, "Hello $username", Toast.LENGTH_SHORT).show()
                                            startActivity(Intent(this, MainActivity::class.java))
                                        }

                                        finish()
                                    }
                            }

                            "pending" -> {
                                Toast.makeText(this, "Approval Pending", Toast.LENGTH_SHORT).show()
                            }

                            "rejected" -> {
                                Toast.makeText(this, "Request Rejected", Toast.LENGTH_SHORT).show()
                            }
                        }

                        break
                    }
                }

                if (!userFound) {
                    Toast.makeText(this, "Email not found. Please register.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Database error", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}