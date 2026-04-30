package com.example.scholarprepadmin

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.scholarprepadmin.databinding.ActivityFifthMatactivityBinding
import com.example.scholarprepadmin.databinding.ActivityFifthMathBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import org.json.JSONObject

class FifthMATActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFifthMatactivityBinding
    private var pdfUri: Uri? = null
    private var selectedTopic = ""
    private val collectionName = "mentalAbilityTest_5th_class"
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding= ActivityFifthMatactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Mental Ability Test - Fifth"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        binding.addquestion.setOnClickListener {
            val intent = Intent(this, AddQuestionActivity::class.java)
            intent.putExtra("collectionName", "mentalAbilityTest_5th_class")
            startActivity(intent)
        }

        binding.addtopic.setOnClickListener {
            val topic = binding.ettopic.text.toString()
            if (topic.isNotEmpty()) addTopic(topic)
        }

        loadTopics()

        binding.btnupload.setOnClickListener {
            pickJson.launch(arrayOf("application/json"))
        }


        binding.addmocktest.setOnClickListener {
            pickFile.launch("application/json")
        }


    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }


    private fun addTopic(topic: String) {

        val map = hashMapOf(
            "topicName" to topic,
            "pdfUrl" to ""
        )

        FirebaseFirestore.getInstance()
            .collection(collectionName)
            .document(topic)
            .set(map)
            .addOnSuccessListener { loadTopics() }
    }

    private fun loadTopics() {

        binding.layoutTopics.removeAllViews()

        FirebaseFirestore.getInstance()
            .collection(collectionName)
            .get()
            .addOnSuccessListener { result ->

                for (doc in result) {
                    val name = doc.id
                    if (name.startsWith("mocktest_") || name.startsWith("results_")) {
                        continue
                    }
                    val topic = doc.getString("topicName") ?: ""
                    val url = doc.getString("pdfUrl") ?: ""

                    addTopicView(topic, url)
                }
            }
    }

    private fun addTopicView(topic: String, url: String) {

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(10, 10, 10, 10)

        val txt = TextView(this)
        txt.text = topic

        txt.setTextColor(android.graphics.Color.WHITE)
        txt.textSize = 20f
        txt.setTypeface(null, android.graphics.Typeface.BOLD)
        val topicParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        topicParams.setMargins(0, 0, 0, 20)

        txt.layoutParams = topicParams


        val btnUpload = Button(this)
        btnUpload.text = "Add / Update PDF"
        btnUpload.minHeight = 0
        btnUpload.minimumHeight = 0
        btnUpload.minWidth = 0
        btnUpload.minimumWidth = 0

        btnUpload.setPadding(20, 16, 20, 16)
        btnUpload.textSize = 11f


        btnUpload.setBackgroundColor(android.graphics.Color.parseColor("#74A6C8"))
        btnUpload.setTextColor(android.graphics.Color.BLACK)


        val btnView = Button(this)
        btnView.text = "View PDF"
        btnView.minHeight = 0
        btnView.minimumHeight = 0
        btnView.minWidth = 0
        btnView.minimumWidth = 0
        btnView.setPadding(20, 16, 20, 16)
        btnView.textSize = 11f
        btnView.setBackgroundColor(android.graphics.Color.parseColor("#74A6C8"))
        btnView.setTextColor(android.graphics.Color.BLACK)


        val btnDelete = Button(this)
        btnDelete.text = "Delete Topic"
        btnDelete.minHeight = 0
        btnDelete.minimumHeight = 0
        btnDelete.minWidth = 0
        btnDelete.minimumWidth = 0
        btnDelete.setPadding(20, 16, 20, 16)
        btnDelete.textSize = 11f
        btnDelete.setBackgroundColor(android.graphics.Color.parseColor("#74A6C8"))
        btnDelete.setTextColor(android.graphics.Color.BLACK)

        btnUpload.setOnClickListener {
            selectedTopic = topic
            pickPdf()
        }

        btnView.setOnClickListener {
            if (url.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(Uri.parse(url), "application/pdf")
                startActivity(intent)
            }
            else {
                Toast.makeText(this, "PDF not uploaded", Toast.LENGTH_SHORT).show()
            }

        }

        btnDelete.setOnClickListener {
            if (url.isNotEmpty()) {
                deleteTopicWithPdf(topic)
            } else {
                deleteTopicOnly(topic)
            }

        }
        layout.setPadding(10, 10, 10, 10)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 10, 0, 10)

        btnUpload.layoutParams = params
        btnView.layoutParams = params
        btnDelete.layoutParams = params

        layout.addView(txt)
        layout.addView(btnUpload)
        layout.addView(btnView)
        layout.addView(btnDelete)

        binding.layoutTopics.addView(layout)
    }

    private fun pickPdf() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/pdf"
        startActivityForResult(intent, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == RESULT_OK) {
            pdfUri = data?.data
            uploadPdf()
        }
    }

    private fun uploadPdf() {

        val ref = FirebaseStorage.getInstance()
            .reference.child("$collectionName/$selectedTopic.pdf")

        pdfUri?.let {
            ref.putFile(it).addOnSuccessListener {

                ref.downloadUrl.addOnSuccessListener { url ->

                    FirebaseFirestore.getInstance()
                        .collection(collectionName)
                        .document(selectedTopic)
                        .update("pdfUrl", url.toString())
                        .addOnSuccessListener {
                            Toast.makeText(this, "PDF uploaded successfully", Toast.LENGTH_SHORT).show()

                            loadTopics() }
                }
            }
        }
    }

    private fun deleteTopicWithPdf(topic: String) {

        FirebaseStorage.getInstance()
            .reference.child("$collectionName/$topic.pdf")
            .delete()
            .addOnSuccessListener {

                FirebaseFirestore.getInstance()
                    .collection(collectionName)
                    .document(topic)
                    .delete()
                    .addOnSuccessListener {

                        Toast.makeText(this, "Topic and PDF deleted", Toast.LENGTH_SHORT).show()
                        loadTopics()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete PDF", Toast.LENGTH_SHORT).show()
            }
    }
    private fun deleteTopicOnly(topic: String) {

        FirebaseFirestore.getInstance()
            .collection(collectionName)
            .document(topic)
            .delete()
            .addOnSuccessListener {

                Toast.makeText(this, "Topic deleted (no PDF)", Toast.LENGTH_SHORT).show()
                loadTopics()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show()
            }
    }

    private val pickJson =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->

            if (uri != null) {
                Toast.makeText(this, "File selected", Toast.LENGTH_SHORT).show()
                uploadJsonToFirestore(uri)
            } else {
                Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show()
            }
        }
    private fun uploadJsonToFirestore(uri: Uri) {

        val db = FirebaseFirestore.getInstance()

        try {
            val inputStream = contentResolver.openInputStream(uri)
            val json = inputStream?.bufferedReader().use { it?.readText() }

            val jsonArray = org.json.JSONArray(json)

            var success = 0
            var fail = 0

            db.collection(collectionName).get().addOnSuccessListener { result ->

                val topics = result.map { it.id }

                for (i in 0 until jsonArray.length()) {

                    val obj = jsonArray.getJSONObject(i)

                    val topic = obj.getString("topic")
                    val difficulty = obj.getString("difficulty").lowercase()

                    val actualTopic = topics.find {
                        it.equals(topic, ignoreCase = true)
                    }

                    if (actualTopic == null) {
                        fail++
                        continue
                    }

                    if (difficulty !in listOf("easy", "medium", "hard")) {
                        fail++
                        continue
                    }

                    val data = hashMapOf(
                        "question" to obj.getString("question"),
                        "option1" to obj.getString("option1"),
                        "option2" to obj.getString("option2"),
                        "option3" to obj.getString("option3"),
                        "option4" to obj.getString("option4"),
                        "correctAnswer" to obj.getString("correctAnswer"),
                        "hint1" to obj.getString("hint1"),
                        "hint2" to obj.getString("hint2"),
                        "solution" to obj.getString("solution")
                    )

                    db.collection(collectionName)
                        .document(actualTopic)
                        .collection(difficulty)
                        .add(data)

                    success++
                }

                Toast.makeText(
                    this,
                    "Upload Completed \nSuccess: $success Failed: $fail",
                    Toast.LENGTH_LONG
                ).show()
            }

        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private val pickFile = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->

        if (uri == null) {
            toast("No file selected")
            return@registerForActivityResult
        }

        val text = contentResolver.openInputStream(uri)
            ?.bufferedReader()
            ?.use { it.readText() }

        if (text.isNullOrEmpty()) {
            toast("Empty file")
            return@registerForActivityResult
        }

        processJson(text)
    }

    private fun processJson(json: String) {

        var success = 0
        var fail = 0

        try {
            val root = JSONObject(json)
            val type = root.optString("type", "").lowercase()

            if (type !in listOf("easy", "medium", "hard")) {
                toast("Invalid type")
                return
            }

            val arr = root.optJSONArray("questions")

            if (arr == null || arr.length() == 0) {
                toast("No questions found")
                return
            }

            val list = ArrayList<HashMap<String, Any>>()

            for (i in 0 until arr.length()) {

                val q = arr.getJSONObject(i)

                val question = q.optString("question")
                val op1 = q.optString("op1")
                val op2 = q.optString("op2")
                val op3 = q.optString("op3")
                val op4 = q.optString("op4")
                val answer = q.optString("answer")

                if (question.isBlank() || op1.isBlank() || op2.isBlank()
                    || op3.isBlank() || op4.isBlank() || answer.isBlank()
                ) {
                    fail++
                    continue
                }

                if (answer != op1 && answer != op2 && answer != op3 && answer != op4) {
                    fail++
                    continue
                }

                val map: HashMap<String, Any> = hashMapOf(
                    "question" to question,
                    "op1" to op1,
                    "op2" to op2,
                    "op3" to op3,
                    "op4" to op4,
                    "answer" to answer
                )

                list.add(map)
                success++
            }

            val timer = when (type) {
                "easy" -> 30
                "medium" -> 45
                else -> 60
            }

            db.collection(collectionName)
                .document("mocktest_$type")
                .set(
                    hashMapOf(
                        "questions" to list,
                        "timer" to timer
                    )
                )
                .addOnSuccessListener {
                    toast("Upload Completed\nSuccess: $success  Fail: $fail")
                }
                .addOnFailureListener {
                    toast("Upload failed")
                }

        } catch (e: Exception) {
            toast("Invalid JSON: ${e.message}")
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

}