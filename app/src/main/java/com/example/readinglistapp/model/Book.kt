package com.example.readinglistapp.model

enum class ReadingStatus(val displayName: String) {
    WANT_TO_READ("Want to Read"),
    READING("Currently Reading"),
    READ("Read");

    companion object {
        fun fromString(value: String): ReadingStatus {
            return entries.find { it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) }
                ?: WANT_TO_READ
        }
    }
}

data class Book(
    val id: String = java.util.UUID.randomUUID().toString(),
    val isbn: String = "",
    val title: String,
    val authors: String = "Unknown Author",
    val publishYear: String = "N/A",
    val coverUrl: String = "",
    var status: ReadingStatus = ReadingStatus.WANT_TO_READ,
    val addedTimestamp: Long = System.currentTimeMillis()
)
