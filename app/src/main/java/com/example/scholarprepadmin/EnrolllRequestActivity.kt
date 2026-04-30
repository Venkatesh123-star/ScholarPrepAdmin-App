package com.example.scholarprepadmin

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.scholarprepadmin.databinding.ActivityEnrolllRequestBinding
import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.firestore.FirebaseFirestore

class EnrolllRequestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEnrolllRequestBinding

    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEnrolllRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        loadRequests()

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Enroll Requests"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun loadRequests() {

        val db = FirebaseFirestore.getInstance()

        db.collection("enrollRequests")
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { snapshot ->

                binding.containerLayout.removeAllViews()

                for (doc in snapshot.documents) {

                    val requestId = doc.id
                    val name = doc.getString("studentName") ?: ""
                    val subject = doc.getString("subject") ?: ""
                    val receipt = doc.getString("receiptNumber") ?: ""
                    val studentClass = doc.getString("class") ?: "No class"


                    val boxLayout = LinearLayout(this)
                    boxLayout.orientation = LinearLayout.VERTICAL
                    boxLayout.setPadding(30, 30, 30, 30)
                    boxLayout.setBackgroundColor(android.graphics.Color.WHITE)

                    val boxParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    boxParams.setMargins(0, 0, 0, 40)
                    boxLayout.layoutParams = boxParams
                    boxLayout.elevation = 8f

                    val tvName = TextView(this)
                    tvName.text = "Name: $name"
                    tvName.textSize = 20f
                    tvName.setTextColor(android.graphics.Color.BLACK)

                    val tvSubject = TextView(this)
                    tvSubject.text = "Subject: $subject"
                    tvSubject.setTextColor(android.graphics.Color.BLACK)

                    val tvClass = TextView(this)
                    tvClass.text = "Class: $studentClass"
                    tvClass.setTextColor(android.graphics.Color.BLACK)

                    val tvReceipt = TextView(this)
                    tvReceipt.text = "Receipt: $receipt"
                    tvReceipt.setTextColor(android.graphics.Color.BLACK)

                    val btnLayout = LinearLayout(this)
                    btnLayout.orientation = LinearLayout.HORIZONTAL

                    val btnParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                    btnParams.setMargins(10, 30, 10, 0)

                    val btnVerify = Button(this)
                    btnVerify.text = "Verify"
                    btnVerify.setTextColor(android.graphics.Color.WHITE)
                    btnVerify.setBackgroundColor(
                        android.graphics.Color.parseColor("#2ECC71")
                    )
                    btnVerify.layoutParams = btnParams

                    val btnReject = Button(this)
                    btnReject.text = "Reject"
                    btnReject.setTextColor(android.graphics.Color.WHITE)
                    btnReject.setBackgroundColor(
                        android.graphics.Color.parseColor("#FF0000")
                    )
                    btnReject.layoutParams = btnParams

                    btnVerify.setOnClickListener {

                        db.collection("enrollRequests")
                            .document(requestId)
                            .update("status", "verified")
                            .addOnSuccessListener {
                                Toast.makeText(this, "Verified", Toast.LENGTH_SHORT).show()
                                loadRequests() // refresh
                            }
                    }

                    btnReject.setOnClickListener {

                        db.collection("enrollRequests")
                            .document(requestId)
                            .update("status", "rejected")
                            .addOnSuccessListener {
                                Toast.makeText(this, "Rejected", Toast.LENGTH_SHORT).show()
                                loadRequests()
                            }
                    }

                    btnLayout.addView(btnVerify)
                    btnLayout.addView(btnReject)

                    boxLayout.addView(tvName)
                    boxLayout.addView(tvClass)
                    boxLayout.addView(tvSubject)
                    boxLayout.addView(tvReceipt)
                    boxLayout.addView(btnLayout)

                    binding.containerLayout.addView(boxLayout)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load data", Toast.LENGTH_SHORT).show()
            }
    }
}