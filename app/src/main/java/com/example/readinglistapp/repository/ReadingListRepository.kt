package com.example.readinglistapp.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.readinglistapp.model.Book
import com.example.readinglistapp.model.ReadingStatus
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ReadingListRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("reading_list_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val key = "saved_books"

    fun getBooks(): List<Book> {
        val json = prefs.getString(key, null) ?: return emptyList()
        val type = object : TypeToken<List<Book>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addBook(book: Book) {
        val current = getBooks().toMutableList()
        // If already exists by ISBN or ID, update or skip
        val index = current.indexOfFirst { (book.isbn.isNotEmpty() && it.isbn == book.isbn) || it.id == book.id }
        if (index >= 0) {
            current[index] = book
        } else {
            current.add(0, book)
        }
        saveBooks(current)
    }

    fun updateBookStatus(bookId: String, newStatus: ReadingStatus) {
        val current = getBooks().toMutableList()
        val index = current.indexOfFirst { it.id == bookId }
        if (index >= 0) {
            current[index].status = newStatus
            saveBooks(current)
        }
    }

    fun removeBook(bookId: String) {
        val current = getBooks().filterNot { it.id == bookId }
        saveBooks(current)
    }

    fun isBookInList(isbn: String): Boolean {
        if (isbn.isEmpty()) return false
        return getBooks().any { it.isbn == isbn }
    }

    private fun saveBooks(books: List<Book>) {
        val json = gson.toJson(books)
        prefs.edit().putString(key, json).apply()
    }
}
