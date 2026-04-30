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
import com.example.scholarprepadmin.databinding.ActivityAdminRequestBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase



class
AdminRequestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminRequestBinding
    private lateinit var db: DatabaseReference
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAdminRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance().reference

        loadRequests()
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Admin Requests"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun loadRequests() {

        db.child("admins").get()
            .addOnSuccessListener { snapshot ->

                binding.containerLayout.removeAllViews()

                for (snap in snapshot.children) {

                    val uid = snap.key!!
                    val email = snap.child("email").value.toString()
                    val status = snap.child("status").value.toString()

                    if (status == "pending") {

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

                        val tvEmail = TextView(this)
                        tvEmail.text = email
                        tvEmail.textSize = 18f
                        tvEmail.setTextColor(android.graphics.Color.BLACK)

                        val btnLayout = LinearLayout(this)
                        btnLayout.orientation = LinearLayout.HORIZONTAL

                        val btnParams = LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1f
                        )
                        btnParams.setMargins(10, 30, 10, 0)
                        val btnApprove = Button(this)
                        btnApprove.text = "Approve"
                        btnApprove.setTextColor(android.graphics.Color.WHITE)
                        btnApprove.setBackgroundColor(android.graphics.Color.parseColor("#2ECC71"))
                        btnApprove.layoutParams = btnParams

                        val btnReject = Button(this)
                        btnReject.text = "Reject"
                        btnReject.setTextColor(android.graphics.Color.WHITE)
                        btnReject.setBackgroundColor(android.graphics.Color.parseColor("#FF0000"))
                        btnReject.layoutParams = btnParams

                        btnApprove.setOnClickListener {
                            db.child("admins").child(uid).child("status").setValue("approved")
                            Toast.makeText(this, "Approved", Toast.LENGTH_SHORT).show()
                            loadRequests()
                        }

                        btnReject.setOnClickListener {
                            db.child("admins").child(uid).child("status").setValue("rejected")
                            Toast.makeText(this, "Rejected", Toast.LENGTH_SHORT).show()
                            loadRequests()
                        }

                        btnLayout.addView(btnApprove)
                        btnLayout.addView(btnReject)

                        boxLayout.addView(tvEmail)
                        boxLayout.addView(btnLayout)

                        binding.containerLayout.addView(boxLayout)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load data", Toast.LENGTH_SHORT).show()
            }
    }
}