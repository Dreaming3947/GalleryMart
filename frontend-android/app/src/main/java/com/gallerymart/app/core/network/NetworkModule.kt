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

object NetworkModule {
    // Default for Android emulator -> host machine localhost.
    // Change BASE_URL in gradle.properties when demoing with a physical device.
    private const val BASE_URL = BuildConfig.BASE_URL

    @Volatile
    private var initialized = false

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val authInterceptor = AuthInterceptor()

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    /**
     * Kept for explicit bootstrap in Application class.
     *
     * We currently do not require context here, but this hook gives a single place
     * for future setup (certificate pinning, debug inspector, etc.).
     */
    fun init(@Suppress("UNUSED_PARAMETER") context: Context) {
        SessionManager.init(context)
        initialized = true
    }

    val artworkApi: ArtworkApi by lazy { createApi(ArtworkApi::class.java) }
    val authApi: AuthApi by lazy { createApi(AuthApi::class.java) }
    val orderApi: OrderApi by lazy { createApi(OrderApi::class.java) }
    val notificationApi: NotificationApi by lazy { createApi(NotificationApi::class.java) }

    fun <T> createApi(service: Class<T>): T = retrofit.create(service)
}

/**
 * Single source of truth for auth/session fields stored on device.
 *
 * This object is intentionally small and explicit so token-related issues
 * are easy to inspect during debugging.
 */
object SessionManager {
    private const val PREFS_NAME = "gallerymart_session"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_TOKEN_TYPE = "token_type"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_USER_ROLES = "user_roles"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        if (::prefs.isInitialized) return
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

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
 * Injects Authorization header when a token exists.
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

