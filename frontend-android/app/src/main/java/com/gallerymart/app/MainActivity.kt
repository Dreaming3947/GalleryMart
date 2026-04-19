package com.gallerymart.app

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.gallerymart.app.databinding.ActivityMainBinding
import com.gallerymart.app.feature.explore.ui.ExploreFragment
import com.gallerymart.app.feature.home.ui.HomeFragment
import com.gallerymart.app.feature.profile.ui.ProfileFragment

class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null

    fun navigateToTab(tabId: Int) {
        binding?.bottomNav?.selectedItemId = tabId
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val tabId = intent?.getIntExtra("NAVIGATE_TO", -1) ?: -1
        if (tabId != -1) {
            binding?.bottomNav?.selectedItemId = tabId
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val safeBinding = runCatching { ActivityMainBinding.inflate(layoutInflater) }
            .getOrElse {
                showHardFallbackUi()
                return
            }
        binding = safeBinding
        setContentView(safeBinding.root)

        showStartupPlaceholder(getString(R.string.app_starting))

        if (savedInstanceState == null || supportFragmentManager.findFragmentById(R.id.fragmentContainer) == null) {
            openInitialScreen()
            handleIntent(intent)
        } else {
            hideStartupPlaceholder()
        }

        safeBinding.bottomNav.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_explore -> ExploreFragment()
                R.id.nav_profile -> ProfileFragment()
                else -> ProfileFragment()
            }
            openScreen(fragment)
            true
        }
    }

    private fun openInitialScreen() {
        if (openScreen(HomeFragment())) {
            binding?.bottomNav?.selectedItemId = R.id.nav_home
            return
        }

        if (!openScreen(ProfileFragment())) {
            showStartupPlaceholder(getString(R.string.app_start_failed))
        } else {
            binding?.bottomNav?.selectedItemId = R.id.nav_profile
        }
    }

    private fun openScreen(fragment: Fragment): Boolean {
        return runCatching {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .runOnCommit { hideStartupPlaceholder() }
                .commit()
            true
        }.getOrElse {
            false
        }
    }

    private fun showStartupPlaceholder(message: String) {
        binding?.startupMessage?.text = message
        binding?.startupPlaceholder?.visibility = View.VISIBLE
    }

    private fun hideStartupPlaceholder() {
        binding?.startupPlaceholder?.visibility = View.GONE
    }

    private fun showHardFallbackUi() {
        val container = FrameLayout(this).apply {
            setBackgroundColor(Color.WHITE)
        }
        val message = TextView(this).apply {
            text = getString(R.string.app_start_failed)
            setTextColor(Color.BLACK)
            textSize = 16f
            gravity = Gravity.CENTER
        }
        container.addView(
            message,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )
        setContentView(container)
    }
}
