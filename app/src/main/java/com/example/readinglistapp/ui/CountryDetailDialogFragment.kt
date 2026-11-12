package com.example.readinglistapp.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.load
import com.example.readinglistapp.R
import com.example.readinglistapp.databinding.DialogCountryDetailBinding
import com.example.readinglistapp.model.Country
import com.example.readinglistapp.repository.WishlistRepository

class CountryDetailDialogFragment : DialogFragment() {

    private var _binding: DialogCountryDetailBinding? = null
    private val binding get() = _binding!!

    private var country: Country? = null
    private lateinit var wishlistRepository: WishlistRepository
    var onWishlistChangedListener: (() -> Unit)? = null

    companion object {
        fun newInstance(country: Country): CountryDetailDialogFragment {
            val fragment = CountryDetailDialogFragment()
            fragment.country = country
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCountryDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        wishlistRepository = WishlistRepository(requireContext())

        val c = country ?: return

        binding.tvDetailName.text = c.name
        binding.tvDetailRegion.text = "Region: ${c.region}"
        binding.tvDetailCapital.text = "Capital: ${c.capital}"
        binding.tvDetailPopulation.text = "Population: ${c.formattedPopulation}"
        binding.tvDetailCurrencies.text = "Currencies: ${c.currencies}"
        binding.tvDetailLanguages.text = "Languages: ${c.languages}"

        val imageLoader = ImageLoader.Builder(requireContext())
            .components { add(SvgDecoder.Factory()) }
            .build()

        binding.ivDetailFlag.load(c.flagUrl, imageLoader) {
            crossfade(true)
            placeholder(android.R.color.darker_gray)
            error(android.R.color.darker_gray)
        }

        updateWishlistIcon(c.code)

        binding.btnDetailWishlist.setOnClickListener {
            wishlistRepository.toggleWishlist(c.code)
            updateWishlistIcon(c.code)
            onWishlistChangedListener?.invoke()
        }

        binding.btnCloseDetail.setOnClickListener {
            dismiss()
        }
    }

    private fun updateWishlistIcon(code: String) {
        val isWish = wishlistRepository.isWishlist(code)
        binding.btnDetailWishlist.setImageResource(
            if (isWish) R.drawable.ic_favorite else R.drawable.ic_favorite_border
        )
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
