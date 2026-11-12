package com.example.readinglistapp.repository

import android.content.Context
import android.content.SharedPreferences

class WishlistRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("wishlist_prefs", Context.MODE_PRIVATE)
    private val key = "wishlist_country_codes"

    fun getWishlistCodes(): Set<String> {
        return prefs.getStringSet(key, emptySet()) ?: emptySet()
    }

    fun toggleWishlist(countryCode: String): Boolean {
        val current = getWishlistCodes().toMutableSet()
        val isAdded: Boolean
        if (current.contains(countryCode)) {
            current.remove(countryCode)
            isAdded = false
        } else {
            current.add(countryCode)
            isAdded = true
        }
        prefs.edit().putStringSet(key, current).apply()
        return isAdded
    }

    fun isWishlist(countryCode: String): Boolean {
        return getWishlistCodes().contains(countryCode)
    }
}
