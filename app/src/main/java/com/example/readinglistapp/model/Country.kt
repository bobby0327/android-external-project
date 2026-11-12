package com.example.readinglistapp.model

import java.text.NumberFormat
import java.util.Locale

data class Country(
    val code: String,
    val name: String,
    val capital: String,
    val region: String,
    val population: Long,
    val currencies: String,
    val languages: String,
    val flagUrl: String = "",
    var isWishToVisit: Boolean = false
) {
    val formattedPopulation: String
        get() = NumberFormat.getNumberInstance(Locale.US).format(population)
}
