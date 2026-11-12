package com.example.readinglistapp.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.load
import com.example.readinglistapp.R
import com.example.readinglistapp.databinding.ItemCountryBinding
import com.example.readinglistapp.model.Country

class CountryAdapter(
    private val onCountryClicked: (Country) -> Unit,
    private val onWishlistToggled: (Country) -> Unit
) : ListAdapter<Country, CountryAdapter.ViewHolder>(CountryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCountryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemCountryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(country: Country) {
            binding.tvCountryName.text = country.name
            binding.tvCountryRegionCapital.text = "${country.region} • ${country.capital}"

            val context = binding.root.context
            val imageLoader = ImageLoader.Builder(context)
                .components { add(SvgDecoder.Factory()) }
                .build()

            binding.ivCountryFlag.load(country.flagUrl, imageLoader) {
                crossfade(true)
                placeholder(android.R.color.darker_gray)
                error(android.R.color.darker_gray)
            }

            binding.btnWishlistToggle.setImageResource(
                if (country.isWishToVisit) R.drawable.ic_favorite else R.drawable.ic_favorite_border
            )

            binding.cardCountry.setOnClickListener {
                onCountryClicked(country)
            }

            binding.btnWishlistToggle.setOnClickListener {
                onWishlistToggled(country)
            }
        }
    }

    class CountryDiffCallback : DiffUtil.ItemCallback<Country>() {
        override fun areItemsTheSame(oldItem: Country, newItem: Country): Boolean = oldItem.code == newItem.code
        override fun areContentsTheSame(oldItem: Country, newItem: Country): Boolean = oldItem == newItem
    }
}
