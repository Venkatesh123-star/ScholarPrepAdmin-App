package com.example.scholarprepadmin


import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.scholarprepadmin.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance().reference

        binding.back.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.register.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val email = binding.Email.text.toString().trim()
        val pass = binding.Password.text.toString().trim()

        if (email.isEmpty()) {
            Toast.makeText(this, "Enter email", Toast.LENGTH_SHORT).show()
            binding.Email.requestFocus()
            return
        }

        if (pass.isEmpty()) {
            Toast.makeText(this, "Enter password", Toast.LENGTH_SHORT).show()
            binding.Password.requestFocus()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Enter valid mail", Toast.LENGTH_SHORT).show()
            binding.Email.requestFocus()
            return
        }
        if (pass.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            binding.Password.requestFocus()
            return
        }



        db.child("admins")
            .get()
            .addOnSuccessListener { snapshot ->

                var alreadyExists = false

                for (snap in snapshot.children) {
                    val dbEmail = snap.child("email").value.toString()

                    if (dbEmail.equals(email, ignoreCase = true)) {
                        alreadyExists = true
                        break
                    }
                }

                if (alreadyExists) {
                    Toast.makeText(this, "Already registered. Please login.", Toast.LENGTH_LONG).show()
                } else {

                    auth.createUserWithEmailAndPassword(email, pass)
                        .addOnSuccessListener {

                            val uid = auth.currentUser!!.uid

                            val map = HashMap<String, Any>()
                            map["email"] = email
                            map["password"] = pass
                            map["status"] = "pending"

                            db.child("admins").child(uid).setValue(map)

                            binding.status.text = "Status: Pending"

                            Toast.makeText(this, "Request sent for approval", Toast.LENGTH_SHORT).show()

                            auth.signOut()
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Registration Failed", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Database error", Toast.LENGTH_SHORT).show()
            }
    }
}