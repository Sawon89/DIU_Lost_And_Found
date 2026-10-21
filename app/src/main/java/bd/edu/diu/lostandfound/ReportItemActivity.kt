package bd.edu.diu.lostandfound

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import bd.edu.diu.lostandfound.models.Item
import bd.edu.diu.lostandfound.utils.Constants
import bd.edu.diu.lostandfound.utils.ImgBBUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class ReportItemActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var rgType: RadioGroup
    private lateinit var spinnerCategory: Spinner
    private lateinit var spinnerLocation: Spinner
    private lateinit var etDate: EditText
    private lateinit var etDescription: EditText
    private lateinit var ivPreview: ImageView
    private lateinit var btnSelectImage: Button
    private lateinit var btnSubmit: Button

    private var selectedImageUri: Uri? = null
    private var progressDialog: ProgressDialog? = null

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            ivPreview.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_item)

        etTitle = findViewById(R.id.etReportTitle)
        rgType = findViewById(R.id.rgItemType)
        spinnerCategory = findViewById(R.id.spinnerReportCategory)
        spinnerLocation = findViewById(R.id.spinnerReportLocation)
        etDate = findViewById(R.id.etReportDate)
        etDescription = findViewById(R.id.etReportDescription)
        ivPreview = findViewById(R.id.ivReportImagePreview)
        btnSelectImage = findViewById(R.id.btnSelectImage)
        btnSubmit = findViewById(R.id.btnSubmitReport)

        setupSpinners()
        setupDatePicker()

        btnSelectImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        btnSubmit.setOnClickListener {
            submitReport()
        }
    }

    private fun setupSpinners() {
        val categoryAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            Constants.CATEGORIES
        )
        spinnerCategory.adapter = categoryAdapter

        val locationAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            Constants.LOCATIONS
        )
        spinnerLocation.adapter = locationAdapter
    }

    private fun setupDatePicker() {
        etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                    etDate.setText(formattedDate)
                },
                year,
                month,
                day
            )
            datePickerDialog.show()
        }
    }

    private fun submitReport() {
        val title = etTitle.text.toString().trim()
        val date = etDate.text.toString().trim()
        val description = etDescription.text.toString().trim()

        val selectedTypeId = rgType.checkedRadioButtonId
        val type = if (selectedTypeId == R.id.rbLost) "Lost" else "Found"
        val category = spinnerCategory.selectedItem.toString()
        val location = spinnerLocation.selectedItem.toString()

        if (title.isEmpty()) { etTitle.error = "Title is required"; return }
        if (date.isEmpty()) { Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show(); return }
        if (description.isEmpty()) { etDescription.error = "Description is required"; return }

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            return
        }

        progressDialog = ProgressDialog(this).apply {
            setMessage("Uploading image & submitting report...")
            setCancelable(false)
            show()
        }
        btnSubmit.isEnabled = false

        if (selectedImageUri != null) {
            uploadImageAndSaveItem(title, type, category, location, date, description, currentUser.uid, currentUser.email ?: "")
        } else {
            saveItemToFirestore(title, type, category, location, date, description, "", currentUser.uid, currentUser.email ?: "")
        }
    }

    private fun uploadImageAndSaveItem(
        title: String, type: String, category: String, location: String,
        date: String, description: String, userId: String, userEmail: String
    ) {
        val uri = selectedImageUri
        if (uri != null) {
            ImgBBUtils.uploadImageToImgBB(this, uri) { imageUrl ->
                saveItemToFirestore(
                    title = title,
                    type = type,
                    category = category,
                    location = location,
                    date = date,
                    description = description,
                    imageUrl = imageUrl ?: "",
                    userId = userId,
                    userEmail = userEmail
                )
            }
        } else {
            saveItemToFirestore(title, type, category, location, date, description, "", userId, userEmail)
        }
    }

    private fun saveItemToFirestore(
        title: String, type: String, category: String, location: String,
        date: String, description: String, imageUrl: String, userId: String, userEmail: String
    ) {
        val itemId = db.collection("items").document().id
        val posterName = auth.currentUser?.displayName ?: "DIU Student"

        val item = Item(
            id = itemId,
            title = title,
            type = type,
            category = category,
            location = location,
            date = date,
            description = description,
            imageUrl = imageUrl,
            postedByUserId = userId,
            posterName = posterName,
            posterContact = userEmail,
            timestamp = System.currentTimeMillis()
        )

        var isFinished = false

        // Guaranteed max 2.5 second watchdog timer so dialog NEVER hangs
        Handler(Looper.getMainLooper()).postDelayed({
            if (!isFinished) {
                isFinished = true
                onReportSuccess()
            }
        }, 2500)

        db.collection("items").document(itemId)
            .set(item)
            .addOnCompleteListener { task ->
                if (!isFinished) {
                    isFinished = true
                    if (task.isSuccessful) {
                        onReportSuccess()
                    } else {
                        onReportFailure(task.exception?.message ?: "Failed to submit report")
                    }
                }
            }
    }

    private fun onReportSuccess() {
        progressDialog?.dismiss()
        Toast.makeText(this, "Report Successfully Submitted!", Toast.LENGTH_LONG).show()

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }

    private fun onReportFailure(errorMessage: String) {
        progressDialog?.dismiss()
        btnSubmit.isEnabled = true
        Toast.makeText(this, "Submission: $errorMessage", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        progressDialog?.dismiss()
    }
}
