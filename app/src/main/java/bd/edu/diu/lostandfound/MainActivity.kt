package bd.edu.diu.lostandfound

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import bd.edu.diu.lostandfound.fragments.FoundFragment
import bd.edu.diu.lostandfound.fragments.HomeFragment
import bd.edu.diu.lostandfound.fragments.LostFragment
import bd.edu.diu.lostandfound.fragments.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private var backPressedTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNav = findViewById(R.id.bottomNavigation)
        val fabReport = findViewById<ExtendedFloatingActionButton>(R.id.fabReport)
        val btnBack = findViewById<ImageView>(R.id.btnNavBack)

        // Default to HomeFragment
        if (savedInstanceState == null) {
            loadFragment(HomeFragment(), "HomeFragment")
        }

        btnBack.setOnClickListener {
            handleBackNavigation()
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment(), "HomeFragment")
                    true
                }
                R.id.nav_lost -> {
                    loadFragment(LostFragment(), "LostFragment")
                    true
                }
                R.id.nav_found -> {
                    loadFragment(FoundFragment(), "FoundFragment")
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(ProfileFragment(), "ProfileFragment")
                    true
                }
                else -> false
            }
        }

        fabReport.setOnClickListener {
            val intent = Intent(this, ReportItemActivity::class.java)
            startActivity(intent)
        }

        // Handle System Hardware Back Button cleanly
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackNavigation()
            }
        })
    }

    private fun handleBackNavigation() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        if (currentFragment !is HomeFragment) {
            // If user is on Lost, Found, or Profile tab, navigate back to HomeFragment
            bottomNav.selectedItemId = R.id.nav_home
        } else {
            // If already on HomeFragment, require double tap to exit app
            if (backPressedTime + 2000 > System.currentTimeMillis()) {
                finishAffinity()
            } else {
                Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show()
                backPressedTime = System.currentTimeMillis()
            }
        }
    }

    private fun loadFragment(fragment: Fragment, tag: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment, tag)
            .commit()
    }
}
