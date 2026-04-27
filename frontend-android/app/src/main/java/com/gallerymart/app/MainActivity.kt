package com.gallerymart.app

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.gallerymart.app.core.network.NetworkModule
import com.gallerymart.app.core.network.SessionManager
import com.gallerymart.app.data.repository.AuthRepository
import com.gallerymart.app.databinding.ActivityMainBinding
import com.gallerymart.app.feature.explore.ui.ExploreFragment
import com.gallerymart.app.feature.home.ui.HomeFragment
import com.gallerymart.app.feature.home.ui.NotificationCenterFragment
import com.gallerymart.app.feature.profile.ui.ProfileFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null

    fun navigateToTab(tabId: Int) {
        binding?.bottomNav?.selectedItemId = tabId
    }

    fun navigateToNotificationCenter() {
        openScreen(NotificationCenterFragment())
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

/**
 * AuthActivity handles Login and Registration with a modern UI.
 */
class AuthActivity : AppCompatActivity() {
    private val authRepository = AuthRepository()
    private var isLoginMode = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NetworkModule.init(this)

        // Root container with brand background
        val root = FrameLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setBackgroundColor(ContextCompat.getColor(this@AuthActivity, R.color.gm_bg))
        }

        val scrollContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(64, 120, 64, 64)
        }

        // Brand Logo Placeholder / Title
        val brandTitle = TextView(this).apply {
            text = "GalleryMart"
            textSize = 32f
            setTypeface(null, Typeface.BOLD)
            setTextColor(ContextCompat.getColor(this@AuthActivity, R.color.gm_primary))
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = 16
            }
        }

        val subTitle = TextView(this).apply {
            text = "Discover and trade unique artworks"
            textSize = 14f
            setTextColor(Color.GRAY)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = 80
            }
        }

        // Form Card
        val card = MaterialCardView(this).apply {
            radius = 32f
            cardElevation = 0f
            setCardBackgroundColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setContentPadding(48, 48, 48, 48)
        }

        val formLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        // Inputs
        val nameInputLayout = createTextInputLayout("Full Name")
        val nameInput = nameInputLayout.editText as TextInputEditText
        
        val emailInputLayout = createTextInputLayout("Email Address")
        val emailInput = emailInputLayout.editText as TextInputEditText
        
        val passwordInputLayout = createTextInputLayout("Password", isPassword = true)
        val passwordInput = passwordInputLayout.editText as TextInputEditText

        val primaryButton = MaterialButton(this).apply {
            text = "Login"
            cornerRadius = 24
            setPadding(0, 32, 0, 32)
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                topMargin = 40
            }
        }

        val loadingBar = ProgressBar(this).apply {
            visibility = View.GONE
            layoutParams = LinearLayout.LayoutParams(80, 80).apply {
                gravity = Gravity.CENTER
                topMargin = 40
            }
        }

        val switchModeText = TextView(this).apply {
            text = "Don't have an account? Register"
            textSize = 13f
            setTextColor(Color.GRAY)
            gravity = Gravity.CENTER
            setPadding(0, 40, 0, 0)
            isClickable = true
            isFocusable = true
        }

        formLayout.addView(nameInputLayout)
        formLayout.addView(emailInputLayout)
        formLayout.addView(passwordInputLayout)
        formLayout.addView(primaryButton)
        formLayout.addView(loadingBar)
        formLayout.addView(switchModeText)

        card.addView(formLayout)
        scrollContainer.addView(brandTitle)
        scrollContainer.addView(subTitle)
        scrollContainer.addView(card)
        root.addView(scrollContainer)
        setContentView(root)

        // Initial UI State
        updateUiMode(nameInputLayout, primaryButton, switchModeText)

        // Listeners
        switchModeText.setOnClickListener {
            isLoginMode = !isLoginMode
            updateUiMode(nameInputLayout, primaryButton, switchModeText)
        }

        primaryButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val name = nameInput.text.toString().trim()

            if (email.isBlank() || password.isBlank() || (!isLoginMode && name.isBlank())) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            primaryButton.visibility = View.GONE
            loadingBar.visibility = View.VISIBLE

            lifecycleScope.launch {
                runCatching {
                    if (isLoginMode) authRepository.login(email, password)
                    else authRepository.register(email, password, name)
                }.onSuccess {
                    openMain()
                }.onFailure { error ->
                    primaryButton.visibility = View.VISIBLE
                    loadingBar.visibility = View.GONE
                    Toast.makeText(this@AuthActivity, error.message ?: "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun createTextInputLayout(hintText: String, isPassword: Boolean = false): TextInputLayout {
        return TextInputLayout(this).apply {
            hint = hintText
            boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
            setBoxCornerRadii(12f, 12f, 12f, 12f)
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = 16
            }
            if (isPassword) {
                endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
            }
            addView(TextInputEditText(context).apply {
                if (isPassword) {
                    inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                } else {
                    inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                }
                textSize = 14f
            })
        }
    }

    private fun updateUiMode(nameInput: View, button: Button, switchText: TextView) {
        if (isLoginMode) {
            nameInput.visibility = View.GONE
            button.text = "Login"
            switchText.text = "Don't have an account? Register"
        } else {
            nameInput.visibility = View.VISIBLE
            button.text = "Create Account"
            switchText.text = "Already have an account? Login"
        }
    }

    private fun openMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
