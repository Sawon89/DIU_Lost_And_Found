package bd.edu.diu.lostandfound.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import bd.edu.diu.lostandfound.ItemDetailActivity
import bd.edu.diu.lostandfound.R
import bd.edu.diu.lostandfound.ReportItemActivity
import bd.edu.diu.lostandfound.adapters.ItemAdapter
import bd.edu.diu.lostandfound.models.Item
import bd.edu.diu.lostandfound.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private lateinit var itemAdapter: ItemAdapter
    private val itemList = mutableListOf<Item>()
    private var filteredList = mutableListOf<Item>()
    private val db = FirebaseFirestore.getInstance()

    private lateinit var tvRecentHeader: TextView
    private lateinit var tvViewAll: TextView
    private lateinit var etSearch: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var spinnerLocation: Spinner

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val rvHomeItems = view.findViewById<RecyclerView>(R.id.rvHomeItems)
        etSearch = view.findViewById(R.id.etSearchHome)
        spinnerCategory = view.findViewById(R.id.spinnerCategoryFilter)
        spinnerLocation = view.findViewById(R.id.spinnerLocationFilter)
        tvRecentHeader = view.findViewById(R.id.tvRecentPostingsHeader)
        tvViewAll = view.findViewById(R.id.tvViewAll)

        val btnHeroReport = view.findViewById<Button>(R.id.btnHeroReport)
        val btnHeroBrowse = view.findViewById<Button>(R.id.btnHeroBrowse)

        btnHeroReport.setOnClickListener {
            val intent = Intent(requireContext(), ReportItemActivity::class.java)
            startActivity(intent)
        }

        btnHeroBrowse.setOnClickListener {
            rvHomeItems.requestFocus()
        }

        // View All Button Listener
        tvViewAll.setOnClickListener {
            etSearch.setText("")
            spinnerCategory.setSelection(0)
            spinnerLocation.setSelection(0)
            applyFilters()
            Toast.makeText(requireContext(), "Showing all postings", Toast.LENGTH_SHORT).show()
        }

        itemAdapter = ItemAdapter(filteredList) { item ->
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

        rvHomeItems.layoutManager = LinearLayoutManager(requireContext())
        rvHomeItems.adapter = itemAdapter

        val categories = arrayOf("All Categories") + Constants.CATEGORIES
        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories)
        spinnerCategory.adapter = categoryAdapter

        val locations = arrayOf("All Locations") + Constants.LOCATIONS
        val locationAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, locations)
        spinnerLocation.adapter = locationAdapter

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int, id: Long) {
                applyFilters()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spinnerLocation.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int, id: Long) {
                applyFilters()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        loadLiveItems()

        return view
    }

    private fun loadLiveItems() {
        db.collection("items")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Failed to load items", Toast.LENGTH_SHORT).show()
                    }
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    itemList.clear()
                    for (doc in snapshot.documents) {
                        val item = doc.toObject(Item::class.java)
                        item?.let { itemList.add(it) }
                    }
                    itemList.sortByDescending { it.timestamp }
                    applyFilters()
                }
            }
    }

    private fun applyFilters() {
        val query = etSearch.text.toString().trim()
        val selectedCategory = spinnerCategory.selectedItem?.toString() ?: "All Categories"
        val selectedLocation = spinnerLocation.selectedItem?.toString() ?: "All Locations"

        val result = itemList.filter { item ->
            val matchesQuery = query.isEmpty() ||
                    item.title.contains(query, ignoreCase = true) ||
                    item.description.contains(query, ignoreCase = true) ||
                    item.posterName.contains(query, ignoreCase = true) ||
                    item.posterContact.contains(query, ignoreCase = true)

            val matchesCategory = selectedCategory == "All Categories" ||
                    item.category.equals(selectedCategory, ignoreCase = true)

            val matchesLocation = selectedLocation == "All Locations" ||
                    item.location.equals(selectedLocation, ignoreCase = true)

            matchesQuery && matchesCategory && matchesLocation
        }

        filteredList.clear()
        filteredList.addAll(result)
        itemAdapter.updateList(filteredList)
        tvRecentHeader.text = "Recent Postings (${filteredList.size})"
    }
}
