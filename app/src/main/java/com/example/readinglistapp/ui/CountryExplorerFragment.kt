package com.example.readinglistapp.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.readinglistapp.R
import com.example.readinglistapp.api.CountryProvider
import com.example.readinglistapp.databinding.FragmentCountryExplorerBinding
import com.example.readinglistapp.model.Country
import com.example.readinglistapp.repository.WishlistRepository
import java.util.Locale

class CountryExplorerFragment : Fragment() {

    private var _binding: FragmentCountryExplorerBinding? = null
    private val binding get() = _binding!!

    private lateinit var wishlistRepository: WishlistRepository
    private lateinit var adapter: CountryAdapter

    private var allCountries: List<Country> = emptyList()
    private var selectedRegion: String? = null
    private var isWishlistOnly: Boolean = false
    private var searchQuery: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCountryExplorerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        wishlistRepository = WishlistRepository(requireContext())

        setupRecyclerView()
        setupListeners()
        loadCountries()
    }

    private fun setupRecyclerView() {
        adapter = CountryAdapter(
            onCountryClicked = { country ->
                showCountryDetails(country)
            },
            onWishlistToggled = { country ->
                val isWish = wishlistRepository.toggleWishlist(country.code)
                country.isWishToVisit = isWish
                adapter.notifyItemChanged(adapter.currentList.indexOf(country))
                val msg = if (isWish) "Added '${country.name}' to Wish-to-Visit" else "Removed '${country.name}' from Wish-to-Visit"
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                if (isWishlistOnly) {
                    filterAndSubmitList()
                }
            }
        )
        binding.rvCountries.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCountries.adapter = adapter
    }

    private fun setupListeners() {
        binding.etCountrySearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s?.toString()?.trim() ?: ""
                filterAndSubmitList()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.chipGroupRegion.setOnCheckedStateChangeListener { _, checkedIds ->
            isWishlistOnly = checkedIds.contains(R.id.chip_wishlist_only)
            selectedRegion = when {
                checkedIds.contains(R.id.chip_region_asia) -> "Asia"
                checkedIds.contains(R.id.chip_region_europe) -> "Europe"
                checkedIds.contains(R.id.chip_region_africa) -> "Africa"
                checkedIds.contains(R.id.chip_region_americas) -> "Americas"
                checkedIds.contains(R.id.chip_region_oceania) -> "Oceania"
                else -> null
            }
            filterAndSubmitList()
        }

        binding.btnDetectCountry.setOnClickListener {
            detectMyCountry()
        }
    }

    private fun loadCountries() {
        val wishlistCodes = wishlistRepository.getWishlistCodes()
        allCountries = CountryProvider.defaultCountries.map { country ->
            country.copy(isWishToVisit = wishlistCodes.contains(country.code))
        }
        filterAndSubmitList()
    }

    private fun filterAndSubmitList() {
        val filtered = allCountries.filter { country ->
            val matchesQuery = searchQuery.isEmpty() || country.name.contains(searchQuery, ignoreCase = true)
            val matchesRegion = selectedRegion == null || country.region.equals(selectedRegion, ignoreCase = true)
            val matchesWishlist = !isWishlistOnly || wishlistRepository.isWishlist(country.code)

            matchesQuery && matchesRegion && matchesWishlist
        }

        adapter.submitList(filtered)

        if (filtered.isEmpty()) {
            binding.tvCountryEmpty.visibility = View.VISIBLE
            binding.rvCountries.visibility = View.GONE
        } else {
            binding.tvCountryEmpty.visibility = View.GONE
            binding.rvCountries.visibility = View.VISIBLE
        }
    }

    private fun detectMyCountry() {
        val deviceCountryCode = Locale.getDefault().country
        val detected = allCountries.find { it.code.equals(deviceCountryCode, ignoreCase = true) }
            ?: allCountries.firstOrNull()

        if (detected != null) {
            Toast.makeText(requireContext(), "Detected country: ${detected.name}", Toast.LENGTH_SHORT).show()
            showCountryDetails(detected)
        } else {
            Toast.makeText(requireContext(), "Could not detect current country", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showCountryDetails(country: Country) {
        val dialog = CountryDetailDialogFragment.newInstance(country)
        dialog.onWishlistChangedListener = {
            loadCountries()
        }
        dialog.show(childFragmentManager, "country_detail_dialog")
    }

    override fun onResume() {
        super.onResume()
        loadCountries()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
