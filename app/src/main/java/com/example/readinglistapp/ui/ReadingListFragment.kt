package com.example.readinglistapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.readinglistapp.R
import com.example.readinglistapp.api.OpenLibraryApiClient
import com.example.readinglistapp.databinding.FragmentReadingListBinding
import com.example.readinglistapp.model.Book
import com.example.readinglistapp.model.ReadingStatus
import com.example.readinglistapp.repository.ReadingListRepository
import com.example.readinglistapp.scanner.IsbnScannerHelper
import kotlinx.coroutines.launch

class ReadingListFragment : Fragment() {

    private var _binding: FragmentReadingListBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: ReadingListRepository
    private lateinit var adapter: ReadingListAdapter
    private lateinit var scannerHelper: IsbnScannerHelper

    private var selectedFilterStatus: ReadingStatus? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReadingListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = ReadingListRepository(requireContext())
        scannerHelper = IsbnScannerHelper(requireContext())

        setupRecyclerView()
        setupListeners()
        loadBooks()
    }

    private fun setupRecyclerView() {
        adapter = ReadingListAdapter(
            onStatusChanged = { book, newStatus ->
                repository.updateBookStatus(book.id, newStatus)
                loadBooks()
            },
            onDeleteClicked = { book ->
                repository.removeBook(book.id)
                Toast.makeText(requireContext(), "Removed '${book.title}'", Toast.LENGTH_SHORT).show()
                loadBooks()
            }
        )
        binding.rvReadingList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvReadingList.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnScanIsbn.setOnClickListener {
            triggerIsbnScan()
        }

        binding.chipGroupStatus.setOnCheckedStateChangeListener { _, checkedIds ->
            selectedFilterStatus = when {
                checkedIds.contains(R.id.chip_filter_want_to_read) -> ReadingStatus.WANT_TO_READ
                checkedIds.contains(R.id.chip_filter_reading) -> ReadingStatus.READING
                checkedIds.contains(R.id.chip_filter_read) -> ReadingStatus.READ
                else -> null // All
            }
            loadBooks()
        }
    }

    private fun triggerIsbnScan() {
        scannerHelper.scanIsbn(
            onIsbnScanned = { isbn ->
                fetchBookByIsbnAndSave(isbn)
            },
            onError = { errorMsg ->
                Toast.makeText(requireContext(), "Scan error: $errorMsg", Toast.LENGTH_SHORT).show()
            }
        )
    }

    fun fetchBookByIsbnAndSave(isbn: String) {
        lifecycleScope.launch {
            try {
                Toast.makeText(requireContext(), "Fetching book for ISBN $isbn...", Toast.LENGTH_SHORT).show()
                val response = OpenLibraryApiClient.service.getBookByIsbn(isbn)
                val coverUrl = OpenLibraryApiClient.getCoverUrlByIsbn(isbn)
                val bookTitle = response.title ?: "ISBN $isbn"
                val publishYear = response.publishDate ?: "N/A"

                val book = Book(
                    isbn = isbn,
                    title = bookTitle,
                    authors = "Open Library Book",
                    publishYear = publishYear,
                    coverUrl = coverUrl,
                    status = ReadingStatus.WANT_TO_READ
                )

                repository.addBook(book)
                Toast.makeText(requireContext(), "Added '$bookTitle' to Want to Read", Toast.LENGTH_LONG).show()
                loadBooks()
            } catch (e: Exception) {
                // If API fetch fails or book not found in Open Library endpoint, create fallback entry with scanned ISBN
                val fallbackBook = Book(
                    isbn = isbn,
                    title = "Book (ISBN: $isbn)",
                    authors = "Unknown Author",
                    publishYear = "N/A",
                    coverUrl = OpenLibraryApiClient.getCoverUrlByIsbn(isbn),
                    status = ReadingStatus.WANT_TO_READ
                )
                repository.addBook(fallbackBook)
                Toast.makeText(requireContext(), "Added ISBN $isbn to Want to Read", Toast.LENGTH_SHORT).show()
                loadBooks()
            }
        }
    }

    fun loadBooks() {
        val allBooks = repository.getBooks()
        val filteredBooks = if (selectedFilterStatus != null) {
            allBooks.filter { it.status == selectedFilterStatus }
        } else {
            allBooks
        }

        adapter.submitList(filteredBooks)

        if (filteredBooks.isEmpty()) {
            binding.tvEmptyState.visibility = View.VISIBLE
            binding.rvReadingList.visibility = View.GONE
        } else {
            binding.tvEmptyState.visibility = View.GONE
            binding.rvReadingList.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        loadBooks()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
