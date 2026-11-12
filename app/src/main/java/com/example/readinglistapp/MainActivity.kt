package com.example.readinglistapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.readinglistapp.auth.AuthManager
import com.example.readinglistapp.databinding.ActivityMainBinding
import com.example.readinglistapp.ui.AuthFragment
import com.example.readinglistapp.ui.BookSearchFragment
import com.example.readinglistapp.ui.CountryExplorerFragment
import com.example.readinglistapp.ui.ReadingListFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val readingListFragment by lazy { ReadingListFragment() }
    private val bookSearchFragment by lazy { BookSearchFragment() }
    private val countryExplorerFragment by lazy { CountryExplorerFragment() }
    private val authFragment by lazy { AuthFragment() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        setupBottomNavigation()

        if (savedInstanceState == null) {
            val authManager = AuthManager.getInstance(this)
            if (!authManager.isLoggedIn) {
                // If not logged in, open Account screen (with Google Sign-In button) directly on launch
                binding.bottomNavigation.selectedItemId = R.id.navigation_account
                switchFragment(authFragment)
            } else {
                switchFragment(readingListFragment)
            }
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_my_books -> {
                    switchFragment(readingListFragment)
                    true
                }
                R.id.navigation_search_books -> {
                    switchFragment(bookSearchFragment)
                    true
                }
                R.id.navigation_countries -> {
                    switchFragment(countryExplorerFragment)
                    true
                }
                R.id.navigation_account -> {
                    switchFragment(authFragment)
                    true
                }
                else -> false
            }
        }
    }

    private fun switchFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
