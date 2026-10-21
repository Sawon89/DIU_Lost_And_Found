package bd.edu.diu.lostandfound.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import bd.edu.diu.lostandfound.ItemDetailActivity
import bd.edu.diu.lostandfound.R
import bd.edu.diu.lostandfound.adapters.ItemAdapter
import bd.edu.diu.lostandfound.models.Item
import com.google.firebase.firestore.FirebaseFirestore

class LostFragment : Fragment() {

    private lateinit var itemAdapter: ItemAdapter
    private val lostItemList = mutableListOf<Item>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_lost, container, false)

        val rvLostItems = view.findViewById<RecyclerView>(R.id.rvLostItems)

        itemAdapter = ItemAdapter(lostItemList) { item ->
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

        rvLostItems.layoutManager = LinearLayoutManager(requireContext())
        rvLostItems.adapter = itemAdapter

        loadLostItems()

        return view
    }

    private fun loadLostItems() {
        db.collection("items")
            .whereEqualTo("type", "Lost")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Error loading lost items", Toast.LENGTH_SHORT).show()
                    }
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    lostItemList.clear()
                    for (doc in snapshot.documents) {
                        val item = doc.toObject(Item::class.java)
                        item?.let { lostItemList.add(it) }
                    }
                    lostItemList.sortByDescending { it.timestamp }
                    itemAdapter.updateList(lostItemList)
                }
            }
    }
}
