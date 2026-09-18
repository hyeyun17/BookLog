package com.example.booklog.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.booklog.data.aladin.AladinBookItem
import com.example.booklog.databinding.ItemBookSimpleBinding

class SimpleBookAdapter :
    RecyclerView.Adapter<SimpleBookAdapter.VH>() {

    private val items = mutableListOf<AladinBookItem>()

    fun submitList(list: List<AladinBookItem>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class VH(
        private val binding: ItemBookSimpleBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AladinBookItem) {
            binding.tvTitle.text = item.title
            binding.tvAuthor.text = item.author
            com.example.booklog.ui.utils.ImageUtils.loadCover(binding.ivCover, item.cover)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemBookSimpleBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
