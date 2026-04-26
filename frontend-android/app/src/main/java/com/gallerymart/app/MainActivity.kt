package com.gallerymart.app

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.fragment.app.Fragment
import com.gallerymart.app.core.network.NetworkModule
import com.gallerymart.app.core.network.SessionManager
import com.gallerymart.app.data.repository.AuthRepository
import com.gallerymart.app.databinding.ActivityMainBinding
import com.gallerymart.app.feature.explore.ui.ExploreFragment
import com.gallerymart.app.feature.home.ui.HomeFragment
import com.gallerymart.app.feature.home.ui.NotificationCenterFragment
import com.gallerymart.app.feature.profile.ui.ProfileFragment
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null

    fun navigateToTab(tabId: Int) {
        binding?.bottomNav?.selectedItemId = tabId
    }

    fun navigateToNotificationCenter() {
        openScreen(NotificationCenterFragment())
        // Optionally: you can deselect bottom nav to show no tab is selected
        binding?.bottomNav?.selectedItemId = 0
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
        NetworkModule.init(this)

        if (SessionManager.accessToken.isNullOrBlank()) {
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
            return
        }

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

class AuthActivity : AppCompatActivity() {
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NetworkModule.init(this)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
        }

        val title = TextView(this).apply {
            text = "GalleryMart Auth"
            textSize = 24f
        }

        val nameInput = EditText(this).apply { hint = "Full name (register only)" }
        val emailInput = EditText(this).apply { hint = "Email" }
        val passwordInput = EditText(this).apply { hint = "Password" }
        val loginButton = Button(this).apply { text = "Login" }
        val registerButton = Button(this).apply { text = "Register" }

        root.addView(title)
        root.addView(nameInput)
        root.addView(emailInput)
        root.addView(passwordInput)
        root.addView(loginButton)
        root.addView(registerButton)
        setContentView(root)

        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Email and password are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                runCatching { authRepository.login(email, password) }
                    .onSuccess { openMain() }
                    .onFailure { error ->
                        Toast.makeText(this@AuthActivity, error.message ?: "Login failed", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        registerButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            if (name.isBlank() || email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Name, email and password are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                runCatching { authRepository.register(email, password, name) }
                    .onSuccess { openMain() }
                    .onFailure { error ->
                        Toast.makeText(this@AuthActivity, error.message ?: "Register failed", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun openMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
