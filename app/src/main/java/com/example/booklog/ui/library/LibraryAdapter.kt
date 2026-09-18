package com.example.booklog.ui.library

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.booklog.data.local.Review
import com.example.booklog.databinding.ItemLibraryBookBinding

class LibraryAdapter(
    private val onClick: (Long) -> Unit
) : ListAdapter<Review, LibraryAdapter.ViewHolder>(Diff) {

    object Diff : DiffUtil.ItemCallback<Review>() {
        override fun areItemsTheSame(oldItem: Review, newItem: Review) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Review, newItem: Review) = oldItem == newItem
    }

    inner class ViewHolder(
        private val binding: ItemLibraryBookBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(review: Review) {
            val image = review.coverUrl?.takeIf { it.isNotBlank() } ?: review.photoUri
            com.example.booklog.ui.utils.ImageUtils.loadCover(binding.ivCover, image)
            binding.root.setOnClickListener { onClick(review.id) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLibraryBookBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
