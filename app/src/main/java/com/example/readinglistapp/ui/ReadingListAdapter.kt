package com.example.readinglistapp.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.readinglistapp.R
import com.example.readinglistapp.databinding.ItemBookReadingListBinding
import com.example.readinglistapp.model.Book
import com.example.readinglistapp.model.ReadingStatus

class ReadingListAdapter(
    private val onStatusChanged: (Book, ReadingStatus) -> Unit,
    private val onDeleteClicked: (Book) -> Unit
) : ListAdapter<Book, ReadingListAdapter.ViewHolder>(BookDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBookReadingListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemBookReadingListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(book: Book) {
            binding.tvBookTitle.text = book.title
            binding.tvBookAuthors.text = book.authors
            binding.tvBookIsbn.text = if (book.isbn.isNotEmpty()) "ISBN: ${book.isbn}" else "Year: ${book.publishYear}"

            if (book.coverUrl.isNotEmpty()) {
                binding.ivBookCover.load(book.coverUrl) {
                    crossfade(true)
                    placeholder(android.R.color.darker_gray)
                    error(android.R.color.darker_gray)
                }
            } else {
                binding.ivBookCover.setImageResource(android.R.color.darker_gray)
            }

            // Setup Status Spinner
            val statuses = ReadingStatus.entries.map { it.displayName }
            val adapter = ArrayAdapter(
                binding.root.context,
                android.R.layout.simple_spinner_item,
                statuses
            ).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            binding.spinnerStatus.adapter = adapter

            val currentStatusIndex = ReadingStatus.entries.indexOf(book.status)
            binding.spinnerStatus.setSelection(if (currentStatusIndex >= 0) currentStatusIndex else 0, false)

            binding.spinnerStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val selectedStatus = ReadingStatus.entries[position]
                    if (selectedStatus != book.status) {
                        onStatusChanged(book, selectedStatus)
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            binding.btnDeleteBook.setOnClickListener {
                onDeleteClicked(book)
            }
        }
    }

    class BookDiffCallback : DiffUtil.ItemCallback<Book>() {
        override fun areItemsTheSame(oldItem: Book, newItem: Book): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Book, newItem: Book): Boolean = oldItem == newItem
    }
}
