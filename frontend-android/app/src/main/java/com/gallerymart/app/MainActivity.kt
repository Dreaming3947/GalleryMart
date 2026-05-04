/**
 * Tệp này định nghĩa các Activities chính của ứng dụng:
 * 1. MainActivity: Màn hình chính sau khi đăng nhập, quản lý việc chuyển đổi giữa các Tab (Trang chủ, Khám phá, Cá nhân).
 * 2. AuthActivity: Màn hình xử lý Đăng nhập và Đăng ký người dùng. Giao diện được xây dựng bằng code (View Code).
 * Đây là điểm khởi đầu (Entry Point) của ứng dụng Android.
 */
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

/**
 * Hoạt động chính của ứng dụng, quản lý điều hướng giữa các Tab (Home, Explore, Profile).
 */
class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null

    /**
     * Chuyển sang một Tab cụ thể dựa trên ID.
     */
    fun navigateToTab(tabId: Int) {
        binding?.bottomNav?.selectedItemId = tabId
    }

    /**
     * Mở màn hình Trung tâm thông báo.
     */
    fun navigateToNotificationCenter() {
        openScreen(NotificationCenterFragment())
        binding?.bottomNav?.selectedItemId = 0
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    /**
     * Xử lý các Intent truyền vào để điều hướng màn hình.
     */
    private fun handleIntent(intent: Intent?) {
        val tabId = intent?.getIntExtra("NAVIGATE_TO", -1) ?: -1
        if (tabId != -1) {
            binding?.bottomNav?.selectedItemId = tabId
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NetworkModule.init(this)

        // Kiểm tra nếu chưa đăng nhập thì chuyển hướng sang AuthActivity
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

        // Khởi tạo màn hình đầu tiên
        if (savedInstanceState == null || supportFragmentManager.findFragmentById(R.id.fragmentContainer) == null) {
            openInitialScreen()
            handleIntent(intent)
        } else {
            hideStartupPlaceholder()
        }

        // Thiết lập sự kiện chọn Tab ở thanh điều hướng dưới cùng
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

    /**
     * Mở màn hình mặc định khi khởi động ứng dụng.
     */
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

    /**
     * Thực hiện thay thế Fragment hiện tại trong container.
     */
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

    /**
     * Hiển thị UI dự phòng nếu xảy ra lỗi nghiêm trọng khi khởi tạo giao diện.
     */
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
 * Hoạt động quản lý Đăng nhập và Đăng ký.
 * Sử dụng View Code hoàn toàn để xây dựng giao diện thay vì XML truyền thống.
 */
class AuthActivity : AppCompatActivity() {
    private val authRepository = AuthRepository()
    private var isLoginMode = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NetworkModule.init(this)

        // Cấu hình giao diện bằng code (Programmatic UI)
        val root = FrameLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setBackgroundColor(ContextCompat.getColor(this@AuthActivity, R.color.gm_bg))
        }

        val scrollContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(64, 120, 64, 64)
        }

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
            text = "Khám phá và giao dịch nghệ thuật độc bản"
            textSize = 14f
            setTextColor(Color.GRAY)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = 80
            }
        }

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

        // Các ô nhập liệu cho biểu mẫu
        val nameInputLayout = createTextInputLayout("Họ và Tên")
        val nameInput = nameInputLayout.editText as TextInputEditText
        
        val emailInputLayout = createTextInputLayout("Địa chỉ Email")
        val emailInput = emailInputLayout.editText as TextInputEditText
        
        val passwordInputLayout = createTextInputLayout("Mật khẩu", isPassword = true)
        val passwordInput = passwordInputLayout.editText as TextInputEditText

        val primaryButton = MaterialButton(this).apply {
            text = "Đăng nhập"
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
            text = "Chưa có tài khoản? Đăng ký ngay"
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

        updateUiMode(nameInputLayout, primaryButton, switchModeText)

        // Chuyển đổi giữa chế độ Đăng nhập và Đăng ký
        switchModeText.setOnClickListener {
            isLoginMode = !isLoginMode
            updateUiMode(nameInputLayout, primaryButton, switchModeText)
        }

        // Xử lý sự kiện nhấn nút chính (Đăng nhập hoặc Đăng ký)
        primaryButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val name = nameInput.text.toString().trim()

            // Kiểm tra tính hợp lệ cơ bản của dữ liệu nhập
            if (email.isBlank() || password.isBlank() || (!isLoginMode && name.isBlank())) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            primaryButton.visibility = View.GONE
            loadingBar.visibility = View.VISIBLE

            lifecycleScope.launch {
                runCatching {
                    // Gọi API thông qua Repository
                    if (isLoginMode) authRepository.login(email, password)
                    else authRepository.register(email, password, name)
                }.onSuccess {
                    // Đăng nhập thành công, chuyển đến màn hình chính
                    openMain()
                }.onFailure { error ->
                    // Hiển thị thông báo lỗi (bao gồm cả lỗi 409 đã được xử lý trong Repository)
                    primaryButton.visibility = View.VISIBLE
                    loadingBar.visibility = View.GONE
                    Toast.makeText(this@AuthActivity, error.message ?: "Xác thực thất bại", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Helper tạo một ô nhập liệu với style Material Design (OutlinedBox).
     */
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

    /**
     * Cập nhật tiêu đề và nhãn giao diện dựa trên chế độ đang chọn (Đăng nhập/Đăng ký).
     */
    private fun updateUiMode(nameInput: View, button: Button, switchText: TextView) {
        if (isLoginMode) {
            nameInput.visibility = View.GONE
            button.text = "Đăng nhập"
            switchText.text = "Chưa có tài khoản? Đăng ký ngay"
        } else {
            nameInput.visibility = View.VISIBLE
            button.text = "Tạo tài khoản"
            switchText.text = "Đã có tài khoản? Đăng nhập"
        }
    }

    private fun openMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
