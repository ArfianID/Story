package com.arfian.story.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.arfian.story.R
import com.arfian.story.data.service.responses.StoryItem
import com.arfian.story.databinding.ItemLayoutBinding
import com.bumptech.glide.Glide

class ListStoryAdapter(private val onClick: (story: StoryItem) -> Unit) : ListAdapter<StoryItem, ListStoryAdapter.ViewHolder>(DIFF_CALLBACK) {
    class ViewHolder(val binding: ItemLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            getItem(position)?.let { story ->
                Glide.with(itemView.context)
                    .load(story.photoUrl)
                    .placeholder(R.drawable.ic_place_holder)
                    .into(binding.imageView)
                binding.name.text = story.name
                binding.description.text = story.description

                itemView.setOnClickListener {
                    onClick(story)
                }
            }
        }
    }
}

val DIFF_CALLBACK = object : DiffUtil.ItemCallback<StoryItem>() {
    override fun areItemsTheSame(oldItem: StoryItem, newItem: StoryItem): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: StoryItem, newItem: StoryItem): Boolean {
        return oldItem.id == newItem.id
    }
}