package com.example.readinglistapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.readinglistapp.api.OpenLibraryApiClient
import com.example.readinglistapp.databinding.FragmentBookSearchBinding
import com.example.readinglistapp.model.Book
import com.example.readinglistapp.model.ReadingStatus
import com.example.readinglistapp.repository.ReadingListRepository
import com.example.readinglistapp.scanner.IsbnScannerHelper
import kotlinx.coroutines.launch

class BookSearchFragment : Fragment() {

    private var _binding: FragmentBookSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: ReadingListRepository
    private lateinit var adapter: BookSearchAdapter
    private lateinit var scannerHelper: IsbnScannerHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = ReadingListRepository(requireContext())
        scannerHelper = IsbnScannerHelper(requireContext())

        setupRecyclerView()
        setupListeners()
    }

    private fun setupRecyclerView() {
        adapter = BookSearchAdapter { book ->
            repository.addBook(book)
            Toast.makeText(requireContext(), "Added '${book.title}' to Reading List!", Toast.LENGTH_SHORT).show()
        }
        binding.rvSearchResults.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSearchResults.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnSearch.setOnClickListener {
            performSearch()
        }

        binding.etSearchQuery.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else {
                false
            }
        }

        binding.btnScanIsbnSearch.setOnClickListener {
            scannerHelper.scanIsbn(
                onIsbnScanned = { isbn ->
                    binding.etSearchQuery.setText(isbn)
                    fetchBookByIsbn(isbn)
                },
                onError = { errorMsg ->
                    Toast.makeText(requireContext(), "Scan error: $errorMsg", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun performSearch() {
        val query = binding.etSearchQuery.text.toString().trim()
        if (query.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a title or author", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.tvSearchEmpty.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val response = OpenLibraryApiClient.service.searchBooks(query)
                val docs = response.docs ?: emptyList()
                val books = docs.mapNotNull { doc ->
                    val title = doc.title ?: return@mapNotNull null
                    val authors = doc.authorNames?.joinToString(", ") ?: "Unknown Author"
                    val year = doc.firstPublishYear?.toString() ?: "N/A"
                    val isbn = doc.isbns?.firstOrNull() ?: ""
                    val coverUrl = when {
                        doc.coverI != null -> OpenLibraryApiClient.getCoverUrlByCoverId(doc.coverI)
                        isbn.isNotEmpty() -> OpenLibraryApiClient.getCoverUrlByIsbn(isbn)
                        else -> ""
                    }
                    Book(
                        isbn = isbn,
                        title = title,
                        authors = authors,
                        publishYear = year,
                        coverUrl = coverUrl,
                        status = ReadingStatus.WANT_TO_READ
                    )
                }

                adapter.submitList(books)
                binding.progressBar.visibility = View.GONE

                if (books.isEmpty()) {
                    binding.tvSearchEmpty.text = "No books found for '$query'"
                    binding.tvSearchEmpty.visibility = View.VISIBLE
                } else {
                    binding.tvSearchEmpty.visibility = View.GONE
                }

            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Search error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchBookByIsbn(isbn: String) {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = OpenLibraryApiClient.service.getBookByIsbn(isbn)
                val bookTitle = response.title ?: "ISBN $isbn"
                val publishYear = response.publishDate ?: "N/A"
                val coverUrl = OpenLibraryApiClient.getCoverUrlByIsbn(isbn)

                val book = Book(
                    isbn = isbn,
                    title = bookTitle,
                    authors = "Open Library Book",
                    publishYear = publishYear,
                    coverUrl = coverUrl,
                    status = ReadingStatus.WANT_TO_READ
                )

                adapter.submitList(listOf(book))
                binding.progressBar.visibility = View.GONE
                binding.tvSearchEmpty.visibility = View.GONE
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                val fallbackBook = Book(
                    isbn = isbn,
                    title = "Book (ISBN: $isbn)",
                    authors = "Unknown Author",
                    publishYear = "N/A",
                    coverUrl = OpenLibraryApiClient.getCoverUrlByIsbn(isbn),
                    status = ReadingStatus.WANT_TO_READ
                )
                adapter.submitList(listOf(fallbackBook))
                binding.tvSearchEmpty.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
