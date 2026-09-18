package com.example.booklog.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.bumptech.glide.Glide
import com.example.booklog.databinding.ItemBookSimpleBinding
import com.example.booklog.data.aladin.AladinBookItem

class SearchBookAdapter(
    private val onClick: (AladinBookItem) -> Unit
) : ListAdapter<AladinBookItem, SearchBookAdapter.ViewHolder>(Diff) {
    private object Diff : DiffUtil.ItemCallback<AladinBookItem>() {
        override fun areItemsTheSame(oldItem: AladinBookItem, newItem: AladinBookItem): Boolean =
            if (oldItem.isbn13.isNotBlank() && newItem.isbn13.isNotBlank()) oldItem.isbn13 == newItem.isbn13
            else oldItem.title == newItem.title && oldItem.author == newItem.author && oldItem.cover == newItem.cover
        override fun areContentsTheSame(oldItem: AladinBookItem, newItem: AladinBookItem) = oldItem == newItem
    }

    inner class ViewHolder(
        private val binding: ItemBookSimpleBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AladinBookItem) {
            binding.tvTitle.text = item.title
            binding.tvAuthor.text = item.author
            com.example.booklog.ui.utils.ImageUtils.loadCover(binding.ivCover, item.cover)
            binding.root.setOnClickListener {
                onClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemBookSimpleBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

}
