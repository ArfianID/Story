package com.arfian.story.view.story.detail

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.arfian.story.R
import com.arfian.story.data.room.StoryEntity
import com.arfian.story.databinding.ActivityDetailStoryBinding
import com.arfian.story.view.ViewModelFactory
import com.bumptech.glide.Glide

class DetailStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailStoryBinding
    private val viewModel: DetailStoryViewModel by viewModels {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        getStoryDetail()
        showLoading()

    }

    private fun setupView() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.detail_story)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }


    @Suppress("DEPRECATION")
    private fun getStoryDetail() {
        val story: StoryEntity? = intent.getParcelableExtra(EXTRA_STORY)
        binding.apply {
            story?.let {
                Glide.with(this@DetailStoryActivity)
                    .load(it.photoUrl)
                    .into(imgStory)
                tvName.text = it.name
                tvDescription.text = it.description
                tvCreatedAt.text = it.createdAt
                tvLan.text = it.lat.toString()
                tvLon.text = it.lon.toString()
            }
        }
        viewModel.setLoadingState(false)
    }

    private fun showLoading() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    companion object {
        const val EXTRA_STORY = "extra_story"
    }
}