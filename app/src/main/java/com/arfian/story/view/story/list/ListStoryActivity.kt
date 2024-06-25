package com.arfian.story.view.story.list

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.arfian.story.R
import com.arfian.story.data.pref.SessionModel
import com.arfian.story.data.service.responses.Result
import com.arfian.story.databinding.ActivityListStoryBinding
import com.arfian.story.view.ViewModelFactory
import com.arfian.story.view.adapter.ListStoryAdapter
import com.arfian.story.view.map.MapsActivity
import com.arfian.story.view.story.detail.DetailStoryActivity
import com.arfian.story.view.upload.AddStoryActivity
import com.arfian.story.view.welcome.WelcomeActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.util.Locale

class ListStoryActivity : AppCompatActivity() {
    private val viewModel by viewModels<ListStoryViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var binding: ActivityListStoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setLocale(viewModel.getSelectedLanguage().ifEmpty { "en" })

        setupView()
        showLoading()
        checkUserSession()
        loadStories()
        setupFabAddStory()
        setupRefresh()
    }

    override fun onResume() {
        super.onResume()
        checkUserSession()
        loadStories()
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
        viewModel.getSession().asLiveData().observe(this) { user: SessionModel ->
            if (!user.isLogin) {
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            }
        }
    }

    private suspend fun setupAdapter() {
        val adapter = ListStoryAdapter { story ->
            val intent = Intent(this, DetailStoryActivity::class.java).apply {
                putExtra(DetailStoryActivity.EXTRA_STORY, story)
            }
            startActivity(intent)
        }
        binding.rvStory.apply {
            layoutManager = LinearLayoutManager(this@ListStoryActivity)
            this.adapter = adapter
            layoutAnimation = AnimationUtils.loadLayoutAnimation(this@ListStoryActivity, R.anim.item_animation_from_top)
        }

        viewModel.setLoadingState(true)
        viewModel.getStories().collect { result ->
            when (result) {
                is Result.Loading -> {
                    viewModel.setLoadingState(true)
                }
                is Result.Success -> {
                    viewModel.setLoadingState(false)
                    val stories = result.data
                    if (stories.isEmpty()) {
                        binding.ivBlankList.visibility = View.VISIBLE
                    } else {
                        binding.ivBlankList.visibility = View.GONE
                        adapter.submitList(stories)
                        binding.rvStory.scheduleLayoutAnimation()
                    }
                }
                is Result.Error -> {
                    viewModel.setLoadingState(false)
                    val errorMessage = result.exception.message
                    if (errorMessage != null) showNetworkErrorSnackbar(errorMessage)
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

    private fun isNetworkConnected(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        return networkCapabilities != null &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun showNetworkErrorSnackbar(message: String) {
        Snackbar.make(
            binding.root,
            message,
            Snackbar.LENGTH_SHORT
        ).setAction(getString(R.string.try_again)) {
            loadStories()
        }.show()
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
        val languages = arrayOf("Dutch", "English", "French", "German", "Indonesia", "Japanese", "Korean", "Spanish")
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
        if (isNetworkConnected()) {
            lifecycleScope.launch {
                setupAdapter()
            }
        } else {
            showNetworkErrorSnackbar(getString(R.string.no_internet_connection_please_try_again))
        }
    }
}