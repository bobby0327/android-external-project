package com.example.readinglistapp.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.readinglistapp.databinding.ItemBookSearchBinding
import com.example.readinglistapp.model.Book

class BookSearchAdapter(
    private val onAddClicked: (Book) -> Unit
) : ListAdapter<Book, BookSearchAdapter.ViewHolder>(BookSearchDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBookSearchBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemBookSearchBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(book: Book) {
            binding.tvSearchTitle.text = book.title
            binding.tvSearchAuthor.text = book.authors
            binding.tvSearchYear.text = "First published: ${book.publishYear}"

            if (book.coverUrl.isNotEmpty()) {
                binding.ivSearchCover.load(book.coverUrl) {
                    crossfade(true)
                    placeholder(android.R.color.darker_gray)
                    error(android.R.color.darker_gray)
                }
            } else {
                binding.ivSearchCover.setImageResource(android.R.color.darker_gray)
            }

            binding.btnAddToReadingList.setOnClickListener {
                onAddClicked(book)
            }
        }
    }

    class BookSearchDiffCallback : DiffUtil.ItemCallback<Book>() {
        override fun areItemsTheSame(oldItem: Book, newItem: Book): Boolean {
            return (oldItem.isbn.isNotEmpty() && oldItem.isbn == newItem.isbn) || oldItem.title == newItem.title
        }
        override fun areContentsTheSame(oldItem: Book, newItem: Book): Boolean = oldItem == newItem
    }
}
