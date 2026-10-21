package bd.edu.diu.lostandfound

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import bd.edu.diu.lostandfound.models.User
import bd.edu.diu.lostandfound.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val etName = findViewById<EditText>(R.id.etRegisterName)
        val etStudentId = findViewById<EditText>(R.id.etRegisterStudentId)
        val etEmail = findViewById<EditText>(R.id.etRegisterEmail)
        val etPhone = findViewById<EditText>(R.id.etRegisterPhone)
        val etPassword = findViewById<EditText>(R.id.etRegisterPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvGoToLogin = findViewById<TextView>(R.id.tvGoToLogin)

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val studentId = etStudentId.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (name.isEmpty()) {
                etName.error = "Name is required"
                return@setOnClickListener
            }

            if (studentId.isEmpty() || !Constants.isValidStudentId(studentId)) {
                etStudentId.error = "Enter a valid Student ID (e.g., CS-D-89-23-124123 or 211-15-1243)"
                return@setOnClickListener
            }

            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Please enter a valid email address"
                return@setOnClickListener
            }

            if (phone.isEmpty()) {
                etPhone.error = "Phone number is required"
                return@setOnClickListener
            }

            if (password.length < 6) {
                etPassword.error = "Password must be at least 6 characters"
                return@setOnClickListener
            }

            btnRegister.isEnabled = false
            Toast.makeText(this, "Creating account...", Toast.LENGTH_SHORT).show()

            // Firebase Auth Signup
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid ?: ""
                        val user = User(
                            userId = uid,
                            name = name,
                            studentId = studentId,
                            email = email,
                            phone = phone
                        )

                        // Save user profile into Firestore
                        db.collection("users").document(uid)
                            .set(user)
                            .addOnCompleteListener {
                                // Sign out after registration so user can explicitly login
                                auth.signOut()

                                Toast.makeText(this, "Registration Successful! Please login.", Toast.LENGTH_LONG).show()

                                // Redirect to LoginActivity
                                val intent = Intent(this, LoginActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                                finish()
                            }
                    } else {
                        btnRegister.isEnabled = true
                        Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        tvGoToLogin.setOnClickListener {
            finish()
        }
    }
}
