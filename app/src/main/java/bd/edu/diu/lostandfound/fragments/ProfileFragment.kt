package bd.edu.diu.lostandfound.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import bd.edu.diu.lostandfound.ItemDetailActivity
import bd.edu.diu.lostandfound.LoginActivity
import bd.edu.diu.lostandfound.R
import bd.edu.diu.lostandfound.adapters.ItemAdapter
import bd.edu.diu.lostandfound.models.Item
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private lateinit var itemAdapter: ItemAdapter
    private val myItemList = mutableListOf<Item>()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val tvName = view.findViewById<TextView>(R.id.tvProfileName)
        val tvStudentId = view.findViewById<TextView>(R.id.tvProfileStudentId)
        val tvEmail = view.findViewById<TextView>(R.id.tvProfileEmail)
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)
        val rvMyItems = view.findViewById<RecyclerView>(R.id.rvMyItems)

        itemAdapter = ItemAdapter(myItemList) { item ->
            val intent = Intent(requireContext(), ItemDetailActivity::class.java).apply {
                putExtra("EXTRA_TITLE", item.title)
                putExtra("EXTRA_TYPE", item.type)
                putExtra("EXTRA_CATEGORY", item.category)
                putExtra("EXTRA_LOCATION", item.location)
                putExtra("EXTRA_DATE", item.date)
                putExtra("EXTRA_DESCRIPTION", item.description)
                putExtra("EXTRA_IMAGE_URL", item.imageUrl)
                putExtra("EXTRA_POSTER_NAME", item.posterName)
                putExtra("EXTRA_POSTER_CONTACT", item.posterContact)
            }
            startActivity(intent)
        }
        rvMyItems.layoutManager = LinearLayoutManager(requireContext())
        rvMyItems.adapter = itemAdapter

        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Fetch Profile Info
            db.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { doc ->
                    if (isAdded && doc.exists()) {
                        tvName.text = doc.getString("name") ?: "Student"
                        tvStudentId.text = "ID: ${doc.getString("studentId") ?: "N/A"}"
                        tvEmail.text = "Email: ${doc.getString("email") ?: currentUser.email}"
                    }
                }

            // Fetch User's Own Posted Items
            db.collection("items")
                .whereEqualTo("postedByUserId", currentUser.uid)
                .addSnapshotListener { snapshot, error ->
                    if (error == null && snapshot != null) {
                        myItemList.clear()
                        for (doc in snapshot.documents) {
                            val item = doc.toObject(Item::class.java)
                            item?.let { myItemList.add(it) }
                        }
                        myItemList.sortByDescending { it.timestamp }
                        itemAdapter.updateList(myItemList)
                    }
                }
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            if (isAdded) {
                Toast.makeText(requireContext(), "Logged out", Toast.LENGTH_SHORT).show()
            }
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finishAffinity()
        }

        return view
    }
}
