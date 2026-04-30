package com.example.scholarprepadmin

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.scholarprepadmin.databinding.ActivityMainBinding
import com.example.scholarprepadmin.databinding.ToolbarBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth = FirebaseAuth.getInstance()

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val toolbarBinding = ToolbarBinding.inflate(layoutInflater)
        binding.toolbar.addView(toolbarBinding.root)

        toolbarBinding.imgProfileToolbar.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        loadToolbarProfile(toolbarBinding.imgProfileToolbar)


        binding.btn5math.setOnClickListener {
            startActivity(Intent(this , FifthMathActivity::class.java))
        }

        binding.btn5eng.setOnClickListener {
            startActivity(Intent(this , FifthEnglishActivity::class.java))
        }

        binding.btn5mar.setOnClickListener {
            startActivity(Intent(this , FifthMarathiActivity::class.java))
        }

        binding.btn5hin.setOnClickListener {
            startActivity(Intent(this , FifthHindiActivity::class.java))
        }

        binding.btn5mat.setOnClickListener {
            startActivity(Intent(this , FifthMATActivity::class.java))
        }

        binding.btn8math.setOnClickListener {
            startActivity(Intent(this , EighthMathActivity::class.java))
        }

        binding.btn8eng.setOnClickListener {
            startActivity(Intent(this , EighthEnglishActivity::class.java))
        }

        binding.btn8hin.setOnClickListener {
            startActivity(Intent(this , EighthHindiActivity::class.java))
        }

        binding.btn8mar.setOnClickListener {
            startActivity(Intent(this , EighthMarathiActivity::class.java))
        }

        binding.btn8mat.setOnClickListener {
            startActivity(Intent(this , EighthMATActivity::class.java))
        }
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId) {

            R.id.menu_admin_requests -> {
                startActivity(Intent(this, AdminRequestActivity::class.java))
            }

            R.id.menu_help -> {
                startActivity(Intent(this, HelpSupportActivity::class.java))
            }

            R.id.menu_enroll_requests -> {
                startActivity(Intent(this, EnrolllRequestActivity::class.java))
            }

            R.id.menu_profile -> {
                startActivity(Intent(this, ProfileActivity::class.java))
            }

            R.id.menu_logout -> {
                FirebaseAuth.getInstance().signOut()

                val prefs = getSharedPreferences("StudentPrefs", MODE_PRIVATE)
                prefs.edit().clear().apply()

                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
        return true
    }

    private fun loadToolbarProfile(imgProfile: ImageView) {

        val uid = auth.currentUser?.uid ?: return

        FirebaseDatabase.getInstance().reference
            .child("admins")
            .child(uid)
            .get()
            .addOnSuccessListener { snapshot ->

                val imageUrl = snapshot.child("image").value?.toString()

                if (!imageUrl.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(imageUrl)
                        .circleCrop()
                        .into(imgProfile)
                }
            }
    }
}