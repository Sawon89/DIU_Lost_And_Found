package bd.edu.diu.lostandfound

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class ItemDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_detail)

        val ivImage = findViewById<ImageView>(R.id.ivDetailImage)
        val tvTitle = findViewById<TextView>(R.id.tvDetailTitle)
        val tvType = findViewById<TextView>(R.id.tvDetailType)
        val tvCategory = findViewById<TextView>(R.id.tvDetailCategory)
        val tvLocation = findViewById<TextView>(R.id.tvDetailLocation)
        val tvDate = findViewById<TextView>(R.id.tvDetailDate)
        val tvDescription = findViewById<TextView>(R.id.tvDetailDescription)
        val tvPosterName = findViewById<TextView>(R.id.tvPosterName)
        val tvPosterContact = findViewById<TextView>(R.id.tvPosterContact)
        val btnContact = findViewById<Button>(R.id.btnContactPoster)

        // Intent Data Receive
        val title = intent.getStringExtra("EXTRA_TITLE") ?: "No Title"
        val type = intent.getStringExtra("EXTRA_TYPE") ?: "Lost"
        val category = intent.getStringExtra("EXTRA_CATEGORY") ?: "General"
        val location = intent.getStringExtra("EXTRA_LOCATION") ?: "Campus"
        val date = intent.getStringExtra("EXTRA_DATE") ?: "N/A"
        val description = intent.getStringExtra("EXTRA_DESCRIPTION") ?: "No description available."
        val imageUrl = intent.getStringExtra("EXTRA_IMAGE_URL")
        val posterName = intent.getStringExtra("EXTRA_POSTER_NAME") ?: "Anonymous Student"
        val posterContact = intent.getStringExtra("EXTRA_POSTER_CONTACT") ?: "N/A"

        // Set UI Data
        tvTitle.text = title
        tvType.text = type.uppercase()
        tvCategory.text = "Category: $category"
        tvLocation.text = "Location: $location"
        tvDate.text = "Date: $date"
        tvDescription.text = description
        tvPosterName.text = posterName
        tvPosterContact.text = "Contact: $posterContact"

        // Load Image using Glide
        if (!imageUrl.isNull_Empty()) {
            Glide.with(this)
                .load(imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(ivImage)
        }

        // Contact Button Click Listener
        btnContact.setOnClickListener {
            if (posterContact.isNotEmpty() && posterContact != "N/A") {
                val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:$posterContact")
                    putExtra(Intent.EXTRA_SUBJECT, "Regarding DIU Lost & Found Item: $title")
                }
                try {
                    startActivity(Intent.createChooser(emailIntent, "Send Email..."))
                } catch (e: Exception) {
                    Toast.makeText(this, "No email client found", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Contact info not available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun String?.isNull_Empty(): Boolean {
        return this == null || this.trim().isEmpty()
    }
}

