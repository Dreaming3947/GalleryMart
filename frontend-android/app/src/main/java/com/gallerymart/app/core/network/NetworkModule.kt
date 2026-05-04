/**
 * Tệp này chứa cấu hình hệ thống mạng của ứng dụng (Network & Session).
 * - NetworkModule: Khởi tạo Retrofit và các API Service để gọi lên server.
 * - SessionManager: Quản lý đăng nhập, lưu trữ Token và thông tin người dùng vào bộ nhớ máy.
 * - AuthInterceptor: Tự động đính kèm Token vào các yêu cầu gửi lên server để xác thực.
 * Đây là "trái tim" của việc giao tiếp dữ liệu và bảo mật.
 */
package com.gallerymart.app.core.network

import android.content.Context
import android.content.SharedPreferences
import com.gallerymart.app.BuildConfig
import com.gallerymart.app.data.remote.api.ArtworkApi
import com.gallerymart.app.data.remote.api.AuthApi
import com.gallerymart.app.data.remote.api.NotificationApi
import com.gallerymart.app.data.remote.api.OrderApi
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Quản lý cấu hình mạng và Retrofit cho toàn bộ ứng dụng.
 */
object NetworkModule {
    // URL cơ sở cho API, được lấy từ BuildConfig (cấu hình trong gradle)
    private const val BASE_URL = BuildConfig.BASE_URL

    @Volatile
    private var initialized = false

    // Interceptor để log các request và response HTTP ra Logcat
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Interceptor tự động thêm token xác thực vào header
    private val authInterceptor = AuthInterceptor()

    // Cấu hình OkHttpClient với các interceptor
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    // Khởi tạo Retrofit instance
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    /**
     * Khởi tạo các thành phần cần thiết như SessionManager.
     * Cần được gọi trong class Application hoặc Activity đầu tiên.
     */
    fun init(@Suppress("UNUSED_PARAMETER") context: Context) {
        SessionManager.init(context)
        initialized = true
    }

    // Khai báo các API service sử dụng lazy initialization
    val artworkApi: ArtworkApi by lazy { createApi(ArtworkApi::class.java) }
    val authApi: AuthApi by lazy { createApi(AuthApi::class.java) }
    val orderApi: OrderApi by lazy { createApi(OrderApi::class.java) }
    val notificationApi: NotificationApi by lazy { createApi(NotificationApi::class.java) }

    fun <T> createApi(service: Class<T>): T = retrofit.create(service)
}

/**
 * Quản lý phiên làm việc (session) và lưu trữ token trong SharedPreferences.
 */
object SessionManager {
    private const val PREFS_NAME = "gallerymart_session"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_TOKEN_TYPE = "token_type"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_USER_ROLES = "user_roles"

    private lateinit var prefs: SharedPreferences

    /**
     * Khởi tạo SharedPreferences.
     */
    fun init(context: Context) {
        if (::prefs.isInitialized) return
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Các thuộc tính truy xuất nhanh thông tin người dùng đã đăng nhập
    var accessToken: String?
        get() = ensurePrefs().getString(KEY_ACCESS_TOKEN, null)
        set(value) = ensurePrefs().edit().putString(KEY_ACCESS_TOKEN, value).apply()

    var tokenType: String?
        get() = ensurePrefs().getString(KEY_TOKEN_TYPE, null)
        set(value) = ensurePrefs().edit().putString(KEY_TOKEN_TYPE, value).apply()

    var userId: Long?
        get() = ensurePrefs().getString(KEY_USER_ID, null)?.toLongOrNull()
        set(value) {
            val editor = ensurePrefs().edit()
            if (value == null) {
                editor.remove(KEY_USER_ID)
            } else {
                editor.putString(KEY_USER_ID, value.toString())
            }
            editor.apply()
        }

    var userEmail: String?
        get() = ensurePrefs().getString(KEY_USER_EMAIL, null)
        set(value) = ensurePrefs().edit().putString(KEY_USER_EMAIL, value).apply()

    var userRoles: String?
        get() = ensurePrefs().getString(KEY_USER_ROLES, null)
        set(value) = ensurePrefs().edit().putString(KEY_USER_ROLES, value).apply()

    /**
     * Lưu toàn bộ thông tin xác thực sau khi đăng nhập thành công.
     */
    fun saveAuth(accessToken: String, tokenType: String?, userId: Long?, email: String?, roles: String?) {
        val editor = ensurePrefs().edit()
        editor.putString(KEY_ACCESS_TOKEN, accessToken)
        editor.putString(KEY_TOKEN_TYPE, tokenType)
        if (userId == null) {
            editor.remove(KEY_USER_ID)
        } else {
            editor.putString(KEY_USER_ID, userId.toString())
        }
        editor.putString(KEY_USER_EMAIL, email)
        editor.putString(KEY_USER_ROLES, roles)
        editor.apply()
    }

    /**
     * Xóa sạch thông tin phiên làm việc (khi đăng xuất).
     */
    fun clear() {
        ensurePrefs().edit().clear().apply()
    }

    private fun ensurePrefs(): SharedPreferences {
        check(::prefs.isInitialized) {
            "SessionManager not initialized. Ensure app bootstrap calls NetworkModule.init(context)."
        }
        return prefs
    }
}

/**
 * Interceptor tự động chèn header Authorization: Bearer <token> vào mỗi request nếu có token.
 */
class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = SessionManager.accessToken

        val updatedRequest = if (!token.isNullOrBlank()) {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }

        return chain.proceed(updatedRequest)
    }
}
