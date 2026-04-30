package com.example.scholarprepadmin

import android.R
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.scholarprepadmin.databinding.ActivityAddQuestionBinding
import com.google.firebase.firestore.FirebaseFirestore

class AddQuestionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddQuestionBinding
    private var selectedTopic: String = ""
    private var selectedButton: Button? = null
    private var collectionName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddQuestionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(com.example.scholarprepadmin.R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Add Question"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        collectionName = intent.getStringExtra("collectionName") ?: ""

        if (collectionName.isEmpty()) {
            Toast.makeText(this, "Collection not found", Toast.LENGTH_SHORT).show()
            finish()
        }

        setupDifficulty()
        loadTopics()

        binding.savesolution.setOnClickListener {
            saveQuestion()
        }
        binding.select.setOnClickListener {

            if (binding.layoutTopics.visibility == View.GONE) {
                binding.layoutTopics.visibility = View.VISIBLE
                loadTopics()
            } else {
                binding.layoutTopics.visibility = View.GONE
            }
        }
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun setupDifficulty() {
        val list = listOf("easy", "medium", "hard")

        val adapter = ArrayAdapter(
            this,
            R.layout.simple_spinner_dropdown_item,
            list
        )

        binding.spDifficulty.adapter = adapter
    }

    private fun loadTopics() {

        binding.layoutTopics.removeAllViews()

        FirebaseFirestore.getInstance()
            .collection(collectionName)
            .get()
            .addOnSuccessListener { result ->

                for (doc in result) {

                    val topicName = doc.id

                    val btn = Button(this)
                    btn.text = topicName
                    btn.textSize = 14f
                    btn.setTextColor(android.graphics.Color.WHITE)
                    btn.setTypeface(null, android.graphics.Typeface.BOLD)


                    btn.setOnClickListener {
                        selectedTopic = topicName
                        selectedButton?.setBackgroundColor(android.graphics.Color.WHITE)
                        btn.setBackgroundColor(android.graphics.Color.WHITE)
                        selectedButton = btn

                        binding.select.text = "Selected: $topicName"
                        binding.layoutTopics.visibility = View.GONE
                    }

                    binding.layoutTopics.addView(btn)
                }
            }
    }
    private fun saveQuestion() {

        if (selectedTopic.isEmpty()) {
            Toast.makeText(this, "Select Topic", Toast.LENGTH_SHORT).show()
            return
        }

        val difficulty = binding.spDifficulty.selectedItem.toString()

        val question = binding.etQuestion.text.toString()
        val opt1 = binding.etOpt1.text.toString()
        val opt2 = binding.etOpt2.text.toString()
        val opt3 = binding.etOpt3.text.toString()
        val opt4 = binding.etOpt4.text.toString()
        val correct = binding.etCorrect.text.toString()
        val hint1 = binding.etHint1.text.toString()
        val hint2 = binding.etHint2.text.toString()
        val solution = binding.etSolution.text.toString()

        if (question.isEmpty() || opt1.isEmpty() || opt2.isEmpty() ||
            opt3.isEmpty() || opt4.isEmpty() || correct.isEmpty()
        ) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val data = hashMapOf(
            "question" to question,
            "option1" to opt1,
            "option2" to opt2,
            "option3" to opt3,
            "option4" to opt4,
            "correctAnswer" to correct,
            "hint1" to hint1,
            "hint2" to hint2,
            "solution" to solution
        )

        FirebaseFirestore.getInstance()
            .collection(collectionName)
            .document(selectedTopic)
            .collection(difficulty)
            .add(data)
            .addOnSuccessListener {

                Toast.makeText(this, "Question Added", Toast.LENGTH_SHORT).show()
                clearFields()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
    private fun clearFields() {
        binding.etQuestion.text.clear()
        binding.etOpt1.text.clear()
        binding.etOpt2.text.clear()
        binding.etOpt3.text.clear()
        binding.etOpt4.text.clear()
        binding.etCorrect.text.clear()
        binding.etHint1.text.clear()
        binding.etHint2.text.clear()
        binding.etSolution.text.clear()
    }
}