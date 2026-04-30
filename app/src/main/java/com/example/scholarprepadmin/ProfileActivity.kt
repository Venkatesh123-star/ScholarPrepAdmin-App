package com.example.scholarprepadmin

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.scholarprepadmin.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var db: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var storageRef: StorageReference

    private var imageUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Profile"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance().reference
        storageRef = FirebaseStorage.getInstance().reference
        loadProfile()


        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                1
            )
        }

        binding.btnChoose.setOnClickListener {
            pickImage()
        }

        binding.save.setOnClickListener {
            uploadProfile()
        }

    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == RESULT_OK) {
            imageUri = data?.data
            binding.profileImage.setImageURI(imageUri)
        }
    }

    private fun uploadProfile() {

        val username = binding.username.text.toString().trim()

        if (username.isEmpty()) {
            binding.username.error = "Enter username"
            return
        }

        if (imageUri == null) {
            Toast.makeText(this, "Select image", Toast.LENGTH_SHORT).show()
            return
        }

        val uid = auth.currentUser!!.uid
        val fileRef = storageRef.child("profile_images/$uid.jpg")

        fileRef.putFile(imageUri!!)
            .addOnSuccessListener {

                fileRef.downloadUrl.addOnSuccessListener { uri ->

                    val map = HashMap<String, Any>()
                    map["username"] = username
                    map["image"] = uri.toString()

                    db.child("admins").child(uid).updateChildren(map)

                    Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show()

                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadProfile() {

        val uid = auth.currentUser!!.uid

        db.child("admins").child(uid).get()
            .addOnSuccessListener { snapshot ->

                if (snapshot.exists()) {

                    val username = snapshot.child("username").value?.toString() ?: ""
                    val imageUrl = snapshot.child("image").value?.toString() ?: ""

                    binding.username.setText(username)

                    if (imageUrl.isNotEmpty()) {
                        Glide.with(this)
                            .load(imageUrl)
                            .into(binding.profileImage)
                    }

                    if (snapshot.child("username").exists()) {
                        binding.save.text = "Update Profile"
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
    }
}
