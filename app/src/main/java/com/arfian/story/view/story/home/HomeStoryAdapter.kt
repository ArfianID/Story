package com.arfian.story.view.story.home

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.arfian.story.R
import com.arfian.story.data.service.responses.StoryItem
import com.arfian.story.databinding.ItemLayoutBinding
import com.arfian.story.view.story.detail.DetailStoryActivity
import com.bumptech.glide.Glide

class HomeStoryAdapter : PagingDataAdapter<StoryItem, HomeStoryAdapter.ViewHolder>(DIFF_CALLBACK) {
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
                    val intent = Intent(it.context, DetailStoryActivity::class.java).apply {
                        putExtra(DetailStoryActivity.EXTRA_STORY, story)
                    }
                    it.context.startActivity(intent)
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