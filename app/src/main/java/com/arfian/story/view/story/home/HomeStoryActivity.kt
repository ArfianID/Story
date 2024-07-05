package com.arfian.story.view.story.home

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.view.animation.AnimationUtils
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.arfian.story.R
import com.arfian.story.databinding.ActivityHomeStoryBinding
import com.arfian.story.view.ViewModelFactory
import com.arfian.story.view.map.MapsActivity
import com.arfian.story.view.upload.AddStoryActivity
import com.arfian.story.view.welcome.WelcomeActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

class HomeStoryActivity : AppCompatActivity() {
    private val viewModel by viewModels<HomeStoryViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var binding: ActivityHomeStoryBinding
    private lateinit var adapter: HomeStoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setLocale(viewModel.getSelectedLanguage().ifEmpty { "en" })

        setupView()
        showLoading()
        checkUserSession()
        loadStories()
        setupFabAddStory()
        setupRefresh()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        supportActionBar?.title = getString(R.string.app_name)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_map -> {
                startActivity(Intent(this, MapsActivity::class.java))
                true
            }

            R.id.action_language -> {
                showLanguageSelectionDialog()
                true
            }

            R.id.action_logout -> {
                viewModel.logout()
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    @SuppressLint("AppBundleLocaleChanges")
    @Suppress("DEPRECATION")
    private fun setLocale(langCode: String) {
        val locale = Locale(langCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
    }

    private fun checkUserSession() {
        viewModel.getSession().asLiveData().observe(this) { session ->
            if (session.token.isEmpty()) {
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            } else {
                loadStories()
            }
        }
    }

    private suspend fun setupAdapter() {
        var isFirstLoad = true

        adapter = HomeStoryAdapter()
        binding.rvStory.apply {
            layoutManager = LinearLayoutManager(this@HomeStoryActivity)
            layoutAnimation = AnimationUtils.loadLayoutAnimation(this@HomeStoryActivity, R.anim.item_animation_from_top)
        }

        binding.rvStory.adapter = adapter.withLoadStateFooter(
            footer = LoadingStateAdapter { adapter.retry() }
        )

        viewModel.getStories().collectLatest { pagingData ->
            adapter.submitData(pagingData)
            viewModel.setLoadingState(true)
            adapter.addLoadStateListener { loadStates ->
                if (loadStates.refresh is LoadState.NotLoading) {
                    viewModel.setLoadingState(false) // Set loading state to false when data has loaded
                    if (adapter.itemCount == 0) {
                        binding.ivBlankList.visibility = View.VISIBLE // Show ImageView when data is empty
                    } else {
                        binding.ivBlankList.visibility = View.GONE // Hide ImageView when data has loaded
                        if (isFirstLoad) {
                            binding.rvStory.scheduleLayoutAnimation()
                            isFirstLoad = false
                        }
                    }
                } else if (loadStates.refresh is LoadState.Error) {
                    viewModel.setLoadingState(false) // Set loading state to false when there's an error
                } else {
                    binding.ivBlankList.visibility = View.GONE // Hide ImageView when data is loading
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun setupView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.app_name)
    }

    private fun showLoading() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun setupFabAddStory() {
        binding.fabAddStory.setOnClickListener {
            startActivity(Intent(this, AddStoryActivity::class.java))
        }
    }

    private fun setupRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            loadStories()
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun showLanguageSelectionDialog() {
        val languages = arrayOf(
            "Dutch",
            "English",
            "French",
            "German",
            "Indonesia",
            "Japanese",
            "Korean",
            "Spanish"
        )
        val languageCodes = arrayOf("nl", "en", "fr", "de", "in", "ja", "ko", "es")

        AlertDialog.Builder(this).apply {
            setTitle(getString(R.string.select_language))
            setSingleChoiceItems(languages, -1) { dialog, which ->
                val selectedLanguageCode = languageCodes[which]
                setLocale(selectedLanguageCode)
                viewModel.setSelectedLanguage(selectedLanguageCode)
                recreate()
                dialog.dismiss()
            }
            setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
        }.create().show()
    }

    private fun loadStories() {
        lifecycleScope.launch {
            setupAdapter()
        }
    }
}