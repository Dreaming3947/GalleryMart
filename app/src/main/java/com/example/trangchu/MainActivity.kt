package com.example.trangchu

import android.content.Context
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewConfiguration
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import java.text.Normalizer
import java.util.Locale
import kotlinx.coroutines.launch
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private companion object {
        const val EXPLORE_PAGE_SIZE = 10
    }

    private enum class Tab { HOME, EXPLORE, PROFILE }
    private enum class ArtworkFilter { ALL, LANDSCAPE, PORTRAIT, ABSTRACT, MODERN }
    private enum class ExploreMoodFilter { ALL, CALM, ENERGY }

    private data class ProductSlot(
        val card: View,
        val image: ImageView,
        val title: TextView,
        val price: TextView,
        val artist: TextView
    )

    private data class AppNotification(
        val message: String,
        var isRead: Boolean = false
    )

    private lateinit var productGateway: ProductGateway

    private lateinit var homeScreen: View
    private lateinit var exploreScreen: View
    private lateinit var profileScreen: View
    private lateinit var detailScreen: View
    private lateinit var bottomNav: View
    private lateinit var detailFloatingTopBar: View
    private lateinit var detailFloatingPriceBar: View

    private lateinit var tabHome: View
    private lateinit var tabExplore: View
    private lateinit var tabProfile: View

    private lateinit var tabHomeIcon: ImageView
    private lateinit var tabExploreIcon: ImageView
    private lateinit var tabProfileIcon: ImageView

    private lateinit var tabHomeLabel: TextView
    private lateinit var tabExploreLabel: TextView
    private lateinit var tabProfileLabel: TextView

    private lateinit var detailMainImage: ImageView
    private lateinit var detailArtistAvatar: ImageView
    private lateinit var detailRelatedImageOne: ImageView
    private lateinit var detailRelatedImageTwo: ImageView
    private lateinit var detailRelatedCardOne: View
    private lateinit var detailRelatedCardTwo: View
    private lateinit var detailTitle: TextView
    private lateinit var detailArtistName: TextView
    private lateinit var detailRating: TextView
    private lateinit var detailStoryContent: TextView
    private lateinit var detailMaterialValueText: TextView
    private lateinit var detailSizeValueText: TextView
    private lateinit var detailPrice: TextView
    private lateinit var detailPriceFloating: TextView
    private lateinit var detailFavoriteButton: ImageButton
    private lateinit var detailImageFullscreenOverlay: View
    private lateinit var detailFullscreenImage: ImageView
    private lateinit var btnCloseDetailImageFullscreen: View
    private lateinit var detailRelatedSection: View
    private lateinit var detailRelatedEmpty: TextView
    private lateinit var detailRelatedTitleOne: TextView
    private lateinit var detailRelatedPriceOne: TextView
    private lateinit var detailRelatedTitleTwo: TextView
    private lateinit var detailRelatedPriceTwo: TextView

    private lateinit var searchInput: EditText
    private lateinit var clearSearchButton: View
    private lateinit var emptyState: TextView
    private lateinit var filterSummary: TextView
    private lateinit var exploreSearchInput: EditText
    private lateinit var clearExploreSearchButton: View
    private lateinit var exploreFilterSummary: TextView
    private lateinit var explorePageInfo: TextView
    private lateinit var exploreNextPageButton: MaterialButton
    private lateinit var exploreNotificationButton: View
    private lateinit var exploreNotificationBadge: TextView

    private lateinit var filterAll: MaterialButton
    private lateinit var filterLandscape: MaterialButton
    private lateinit var filterPortrait: MaterialButton
    private lateinit var filterAbstract: MaterialButton
    private lateinit var filterModern: MaterialButton
    private lateinit var exploreFilterAll: MaterialButton
    private lateinit var exploreFilterCalm: MaterialButton
    private lateinit var exploreFilterEnergy: MaterialButton

    private lateinit var homeSlots: List<ProductSlot>
    private lateinit var exploreSlots: List<ProductSlot>

    private var selectedArtworkFilter: ArtworkFilter = ArtworkFilter.ALL
    private var selectedExploreMoodFilter: ExploreMoodFilter = ExploreMoodFilter.ALL
    private lateinit var favoriteDb: FavoriteDbHelper

    private var displayProducts: List<Product> = emptyList()
    private var homeRenderedProducts: List<Product> = emptyList()
    private var exploreRenderedProducts: List<Product> = emptyList()
    private var detailRelatedProducts: List<Product> = emptyList()
    private var currentDetailProduct: Product? = null
    private var pendingUploadImageUri: Uri? = null
    private lateinit var fullscreenScaleDetector: ScaleGestureDetector
    private lateinit var fullscreenTapDetector: GestureDetector
    private var fullscreenScale = 1f
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var fullscreenDownX = 0f
    private var fullscreenDownY = 0f
    private var fullscreenMoved = false
    private var latestExploreFiltered: List<Product> = emptyList()
    private var exploreCurrentPage: Int = 0
    private var exploreTotalPages: Int = 0
    private val notifications = mutableListOf<AppNotification>()

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@registerForActivityResult
        contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        pendingUploadImageUri = uri
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        favoriteDb = FavoriteDbHelper(this)
        productGateway = ProductGatewayProvider.provide(this)

        bindViews()
        bindActions()

        refreshProducts()
        showTab(Tab.HOME)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun bindViews() {
        homeScreen = findViewById(R.id.screenHome)
        exploreScreen = findViewById(R.id.screenExplore)
        profileScreen = findViewById(R.id.screenProfile)
        detailScreen = findViewById(R.id.screenDetail)
        bottomNav = findViewById(R.id.bottomNav)
        detailFloatingTopBar = findViewById(R.id.detailFloatingTopBar)
        detailFloatingPriceBar = findViewById(R.id.detailFloatingPriceBar)

        tabHome = findViewById(R.id.tabHome)
        tabExplore = findViewById(R.id.tabExplore)
        tabProfile = findViewById(R.id.tabProfile)

        tabHomeIcon = findViewById(R.id.tabHomeIcon)
        tabExploreIcon = findViewById(R.id.tabExploreIcon)
        tabProfileIcon = findViewById(R.id.tabProfileIcon)

        tabHomeLabel = findViewById(R.id.tabHomeLabel)
        tabExploreLabel = findViewById(R.id.tabExploreLabel)
        tabProfileLabel = findViewById(R.id.tabProfileLabel)

        detailMainImage = findViewById(R.id.detailMainImage)
        detailArtistAvatar = findViewById(R.id.detailArtistAvatar)
        detailRelatedImageOne = findViewById(R.id.detailRelatedImageOne)
        detailRelatedImageTwo = findViewById(R.id.detailRelatedImageTwo)
        detailRelatedCardOne = findViewById(R.id.detailRelatedCardOne)
        detailRelatedCardTwo = findViewById(R.id.detailRelatedCardTwo)
        detailTitle = findViewById(R.id.detailTitle)
        detailArtistName = findViewById(R.id.detailArtistName)
        detailRating = findViewById(R.id.detailRating)
        detailStoryContent = findViewById(R.id.detailStoryContent)
        detailMaterialValueText = findViewById(R.id.detailMaterialValueText)
        detailSizeValueText = findViewById(R.id.detailSizeValueText)
        detailPrice = findViewById(R.id.detailPrice)
        detailPriceFloating = findViewById(R.id.detailPriceFloating)
        detailFavoriteButton = findViewById(R.id.btnDetailFavoriteFloating)
        detailImageFullscreenOverlay = findViewById(R.id.detailImageFullscreenOverlay)
        detailFullscreenImage = findViewById(R.id.detailFullscreenImage)
        btnCloseDetailImageFullscreen = findViewById(R.id.btnCloseDetailImageFullscreen)
        detailRelatedSection = findViewById(R.id.detailRelatedSection)
        detailRelatedEmpty = findViewById(R.id.detailRelatedEmpty)
        detailRelatedTitleOne = findViewById(R.id.detailRelatedTitleOne)
        detailRelatedPriceOne = findViewById(R.id.detailRelatedPriceOne)
        detailRelatedTitleTwo = findViewById(R.id.detailRelatedTitleTwo)
        detailRelatedPriceTwo = findViewById(R.id.detailRelatedPriceTwo)

        searchInput = findViewById(R.id.searchInput)
        clearSearchButton = findViewById(R.id.btnClearSearch)
        emptyState = findViewById(R.id.txtHomeEmpty)
        filterSummary = findViewById(R.id.txtHomeFilterSummary)

        exploreSearchInput = findViewById(R.id.exploreSearchInput)
        clearExploreSearchButton = findViewById(R.id.btnClearExploreSearch)
        exploreFilterSummary = findViewById(R.id.txtExploreFilterSummary)
        explorePageInfo = findViewById(R.id.txtExplorePageInfo)
        exploreNextPageButton = findViewById(R.id.btnExploreNextPage)
        exploreNotificationButton = findViewById(R.id.btnExploreNotifications)
        exploreNotificationBadge = findViewById(R.id.txtExploreNotificationBadge)

        filterAll = findViewById(R.id.filterAll)
        filterLandscape = findViewById(R.id.filterLandscape)
        filterPortrait = findViewById(R.id.filterPortrait)
        filterAbstract = findViewById(R.id.filterAbstract)
        filterModern = findViewById(R.id.filterModern)
        exploreFilterAll = findViewById(R.id.exploreFilterAll)
        exploreFilterCalm = findViewById(R.id.exploreFilterCalm)
        exploreFilterEnergy = findViewById(R.id.exploreFilterEnergy)

        homeSlots = listOf(
            ProductSlot(
                card = findViewById(R.id.cardFeaturedOne),
                image = findViewById(R.id.imgHomeCard1),
                title = findViewById(R.id.txtHomeCard1Title),
                price = findViewById(R.id.txtHomeCard1Price),
                artist = findViewById(R.id.txtHomeCard1Artist)
            ),
            ProductSlot(
                card = findViewById(R.id.cardFeaturedTwo),
                image = findViewById(R.id.imgHomeCard2),
                title = findViewById(R.id.txtHomeCard2Title),
                price = findViewById(R.id.txtHomeCard2Price),
                artist = findViewById(R.id.txtHomeCard2Artist)
            ),
            ProductSlot(
                card = findViewById(R.id.cardFeaturedThree),
                image = findViewById(R.id.imgHomeCard3),
                title = findViewById(R.id.txtHomeCard3Title),
                price = findViewById(R.id.txtHomeCard3Price),
                artist = findViewById(R.id.txtHomeCard3Artist)
            ),
            ProductSlot(
                card = findViewById(R.id.cardFeaturedFour),
                image = findViewById(R.id.imgHomeCard4),
                title = findViewById(R.id.txtHomeCard4Title),
                price = findViewById(R.id.txtHomeCard4Price),
                artist = findViewById(R.id.txtHomeCard4Artist)
            )
        )

        exploreSlots = listOf(
            ProductSlot(
                card = findViewById(R.id.exploreCardStyles),
                image = findViewById(R.id.imgExploreCard1),
                title = findViewById(R.id.txtExploreCard1Title),
                price = findViewById(R.id.txtExploreCard1Price),
                artist = findViewById(R.id.txtExploreCard1Artist)
            ),
            ProductSlot(
                card = findViewById(R.id.exploreCardArtists),
                image = findViewById(R.id.imgExploreCard2),
                title = findViewById(R.id.txtExploreCard2Title),
                price = findViewById(R.id.txtExploreCard2Price),
                artist = findViewById(R.id.txtExploreCard2Artist)
            ),
            ProductSlot(
                card = findViewById(R.id.exploreCardNew),
                image = findViewById(R.id.imgExploreCard3),
                title = findViewById(R.id.txtExploreCard3Title),
                price = findViewById(R.id.txtExploreCard3Price),
                artist = findViewById(R.id.txtExploreCard3Artist)
            ),
            ProductSlot(
                card = findViewById(R.id.exploreCardFeatured),
                image = findViewById(R.id.imgExploreCard4),
                title = findViewById(R.id.txtExploreCard4Title),
                price = findViewById(R.id.txtExploreCard4Price),
                artist = findViewById(R.id.txtExploreCard4Artist)
            ),
            ProductSlot(
                card = findViewById(R.id.exploreCardFive),
                image = findViewById(R.id.imgExploreCard5),
                title = findViewById(R.id.txtExploreCard5Title),
                price = findViewById(R.id.txtExploreCard5Price),
                artist = findViewById(R.id.txtExploreCard5Artist)
            ),
            ProductSlot(
                card = findViewById(R.id.exploreCardSix),
                image = findViewById(R.id.imgExploreCard6),
                title = findViewById(R.id.txtExploreCard6Title),
                price = findViewById(R.id.txtExploreCard6Price),
                artist = findViewById(R.id.txtExploreCard6Artist)
            ),
            ProductSlot(
                card = findViewById(R.id.exploreCardSeven),
                image = findViewById(R.id.imgExploreCard7),
                title = findViewById(R.id.txtExploreCard7Title),
                price = findViewById(R.id.txtExploreCard7Price),
                artist = findViewById(R.id.txtExploreCard7Artist)
            ),
            ProductSlot(
                card = findViewById(R.id.exploreCardEight),
                image = findViewById(R.id.imgExploreCard8),
                title = findViewById(R.id.txtExploreCard8Title),
                price = findViewById(R.id.txtExploreCard8Price),
                artist = findViewById(R.id.txtExploreCard8Artist)
            ),
            ProductSlot(
                card = findViewById(R.id.exploreCardNine),
                image = findViewById(R.id.imgExploreCard9),
                title = findViewById(R.id.txtExploreCard9Title),
                price = findViewById(R.id.txtExploreCard9Price),
                artist = findViewById(R.id.txtExploreCard9Artist)
            ),
            ProductSlot(
                card = findViewById(R.id.exploreCardTen),
                image = findViewById(R.id.imgExploreCard10),
                title = findViewById(R.id.txtExploreCard10Title),
                price = findViewById(R.id.txtExploreCard10Price),
                artist = findViewById(R.id.txtExploreCard10Artist)
            )
        )

        clearSearchButton.isVisible = false
        clearExploreSearchButton.isVisible = false
        renderNotificationBadge()
    }

    private fun bindActions() {
        tabHome.setOnClickListener { showTab(Tab.HOME) }
        tabExplore.setOnClickListener { showTab(Tab.EXPLORE) }
        tabProfile.setOnClickListener { showTab(Tab.PROFILE) }

        findViewById<View>(R.id.btnHomeExplore).setOnClickListener {
            showTab(Tab.EXPLORE)
        }
        findViewById<View>(R.id.btnHomeCategoriesViewAll).setOnClickListener {
            showTab(Tab.EXPLORE)
        }
        findViewById<View>(R.id.btnHomeFeaturedViewAll).setOnClickListener {
            showTab(Tab.EXPLORE)
        }
        findViewById<View>(R.id.btnHomeSell).setOnClickListener {
            showUploadDialog()
        }
        findViewById<View>(R.id.btnHomeSearch).setOnClickListener {
            focusSearchInput()
        }

        findViewById<View>(R.id.btnHomeCategoryLandscape).setOnClickListener { selectArtworkFilter(ArtworkFilter.LANDSCAPE) }
        findViewById<View>(R.id.btnHomeCategoryPortrait).setOnClickListener { selectArtworkFilter(ArtworkFilter.PORTRAIT) }
        findViewById<View>(R.id.btnHomeCategoryAbstract).setOnClickListener { selectArtworkFilter(ArtworkFilter.ABSTRACT) }

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                clearSearchButton.isVisible = !s.isNullOrBlank()
                applyHomeFilters()
            }
        })
        clearSearchButton.setOnClickListener {
            searchInput.text?.clear()
            focusSearchInput()
        }

        findViewById<View>(R.id.btnExploreBackHome).setOnClickListener { showTab(Tab.HOME) }
        findViewById<View>(R.id.btnExploreSearch).setOnClickListener { focusExploreSearchInput() }
        exploreNotificationButton.setOnClickListener { openNotificationsDialog() }

        exploreSearchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                clearExploreSearchButton.isVisible = !s.isNullOrBlank()
                applyExploreFilters(resetPage = true)
            }
        })
        clearExploreSearchButton.setOnClickListener {
            exploreSearchInput.text?.clear()
            focusExploreSearchInput()
        }

        filterAll.setOnClickListener { selectArtworkFilter(ArtworkFilter.ALL) }
        filterLandscape.setOnClickListener { selectArtworkFilter(ArtworkFilter.LANDSCAPE) }
        filterPortrait.setOnClickListener { selectArtworkFilter(ArtworkFilter.PORTRAIT) }
        filterAbstract.setOnClickListener { selectArtworkFilter(ArtworkFilter.ABSTRACT) }
        filterModern.setOnClickListener { selectArtworkFilter(ArtworkFilter.MODERN) }

        exploreFilterAll.setOnClickListener { selectExploreMoodFilter(ExploreMoodFilter.ALL) }
        exploreFilterCalm.setOnClickListener { selectExploreMoodFilter(ExploreMoodFilter.CALM) }
        exploreFilterEnergy.setOnClickListener { selectExploreMoodFilter(ExploreMoodFilter.ENERGY) }
        exploreNextPageButton.setOnClickListener {
            if (exploreCurrentPage + 1 >= exploreTotalPages) return@setOnClickListener
            exploreCurrentPage += 1
            applyExploreFilters(resetPage = false)
        }

        homeSlots.forEachIndexed { index, slot ->
            slot.card.setOnClickListener {
                homeRenderedProducts.getOrNull(index)?.let(::openArtworkDetail)
            }
        }
        exploreSlots.forEachIndexed { index, slot ->
            slot.card.setOnClickListener {
                exploreRenderedProducts.getOrNull(index)?.let(::openArtworkDetail)
            }
        }

        findViewById<View>(R.id.btnDetailBackFloating).setOnClickListener { closeArtworkDetail() }
        detailMainImage.setOnClickListener { openFullscreenImage() }
        detailImageFullscreenOverlay.setOnClickListener { closeFullscreenImage() }
        btnCloseDetailImageFullscreen.setOnClickListener { closeFullscreenImage() }
        findViewById<View>(R.id.btnDetailShareFloating).setOnClickListener {
            toast(getString(R.string.action_share_artwork))
        }
        detailFavoriteButton.setOnClickListener {
            val product = currentDetailProduct ?: return@setOnClickListener
            val isFavorite = favoriteDb.toggleFavorite(product.id)
            updateFavoriteButtonUi(isFavorite)
            toast(
                getString(
                    if (isFavorite) R.string.action_favorite_artwork
                    else R.string.action_unfavorite_artwork
                )
            )
        }
        findViewById<View>(R.id.btnDetailBuyNowFloating).setOnClickListener {
            toast(getString(R.string.action_buy_now))
        }

        detailRelatedCardOne.setOnClickListener {
            detailRelatedProducts.getOrNull(0)?.let(::openArtworkDetail)
        }
        detailRelatedCardTwo.setOnClickListener {
            detailRelatedProducts.getOrNull(1)?.let(::openArtworkDetail)
        }
        findViewById<View>(R.id.btnDetailRelatedExplore).setOnClickListener {
            showTab(Tab.EXPLORE)
        }

        findViewById<View>(R.id.btnEditProfile).setOnClickListener {
            toast(getString(R.string.action_profile_edit))
        }
        findViewById<View>(R.id.btnMyOrders).setOnClickListener {
            toast(getString(R.string.action_orders))
        }
        findViewById<View>(R.id.btnProfileUploadProduct).setOnClickListener {
            showUploadDialog()
        }

        setupFullscreenImageGestures()
    }

    private fun refreshProducts() {
        lifecycleScope.launch {
            displayProducts = productGateway.getDisplayProducts()
            applyHomeFilters()
            applyExploreFilters(resetPage = true)
        }
    }

    private fun showTab(tab: Tab) {
        homeScreen.isVisible = tab == Tab.HOME
        exploreScreen.isVisible = tab == Tab.EXPLORE
        profileScreen.isVisible = tab == Tab.PROFILE
        detailScreen.isVisible = false
        detailFloatingTopBar.isVisible = false
        detailFloatingPriceBar.isVisible = false
        detailImageFullscreenOverlay.isVisible = false
        bottomNav.isVisible = true

        applyTabState(tabHome, tabHomeIcon, tabHomeLabel, tab == Tab.HOME)
        applyTabState(tabExplore, tabExploreIcon, tabExploreLabel, tab == Tab.EXPLORE)
        applyTabState(tabProfile, tabProfileIcon, tabProfileLabel, tab == Tab.PROFILE)
    }

    private fun selectArtworkFilter(filter: ArtworkFilter) {
        selectedArtworkFilter = filter
        updateFilterButtons()
        applyHomeFilters()
    }

    private fun selectExploreMoodFilter(filter: ExploreMoodFilter) {
        selectedExploreMoodFilter = filter
        updateExploreFilterButtons()
        applyExploreFilters(resetPage = true)
    }

    private fun updateFilterButtons() {
        applyFilterButtonState(filterAll, selectedArtworkFilter == ArtworkFilter.ALL)
        applyFilterButtonState(filterLandscape, selectedArtworkFilter == ArtworkFilter.LANDSCAPE)
        applyFilterButtonState(filterPortrait, selectedArtworkFilter == ArtworkFilter.PORTRAIT)
        applyFilterButtonState(filterAbstract, selectedArtworkFilter == ArtworkFilter.ABSTRACT)
        applyFilterButtonState(filterModern, selectedArtworkFilter == ArtworkFilter.MODERN)
    }

    private fun updateExploreFilterButtons() {
        applyFilterButtonState(exploreFilterAll, selectedExploreMoodFilter == ExploreMoodFilter.ALL)
        applyFilterButtonState(exploreFilterCalm, selectedExploreMoodFilter == ExploreMoodFilter.CALM)
        applyFilterButtonState(exploreFilterEnergy, selectedExploreMoodFilter == ExploreMoodFilter.ENERGY)
    }

    private fun applyFilterButtonState(button: MaterialButton, selected: Boolean) {
        val backgroundColor = if (selected) R.color.accent else android.R.color.white
        val textColor = if (selected) R.color.white else R.color.text_primary
        val strokeColor = if (selected) R.color.accent else R.color.border

        button.setBackgroundTintList(ColorStateList.valueOf(color(backgroundColor)))
        button.setTextColor(color(textColor))
        button.strokeColor = ColorStateList.valueOf(color(strokeColor))
        button.strokeWidth = if (selected) 0 else 1
    }

    private fun applyHomeFilters() {
        val query = normalize(searchInput.text?.toString().orEmpty())
        val tokens = query.split("\\s+".toRegex()).filter { it.isNotBlank() }
        val hashtagTokens = tokens.filter { it.startsWith("#") }.map { it.removePrefix("#") }
        val keywordTokens = tokens.filterNot { it.startsWith("#") }

        val filtered = displayProducts.filter { product ->
            val filterMatch = matchesHomeFilter(product)
            val index = normalize(product.searchIndex())
            val keywordMatch = keywordTokens.all { token -> index.contains(token) }
            val tagMatch = hashtagTokens.all { token ->
                product.tags.any { normalize(it).contains(token) }
            }
            filterMatch && keywordMatch && tagMatch
        }

        homeRenderedProducts = filtered.take(homeSlots.size)
        renderSlots(homeSlots, homeRenderedProducts)

        emptyState.isVisible = filtered.isEmpty()
        updateHomeFilterSummary(tokens)
    }

    private fun applyExploreFilters(resetPage: Boolean = false) {

        val query = normalize(exploreSearchInput.text?.toString().orEmpty())
        val tokens = query.split("\\s+".toRegex()).filter { it.isNotBlank() }

        val filtered = displayProducts.filter { product ->
            val moodMatch = matchesExploreMood(product)
            val index = normalize(product.searchIndex())
            val keywordMatch = tokens.all { token -> index.contains(token) }
            moodMatch && keywordMatch
        }

        latestExploreFiltered = filtered

        if (resetPage) exploreCurrentPage = 0
        exploreTotalPages = if (filtered.isEmpty()) 0 else (filtered.size + EXPLORE_PAGE_SIZE - 1) / EXPLORE_PAGE_SIZE
        if (exploreTotalPages == 0) {
            exploreCurrentPage = 0
        } else {
            exploreCurrentPage = exploreCurrentPage.coerceIn(0, exploreTotalPages - 1)
        }

        val startIndex = exploreCurrentPage * EXPLORE_PAGE_SIZE
        val endIndex = minOf(startIndex + EXPLORE_PAGE_SIZE, filtered.size)
        exploreRenderedProducts = if (startIndex < endIndex) filtered.subList(startIndex, endIndex) else emptyList()
        renderSlots(exploreSlots, exploreRenderedProducts)

        val moodText = when (selectedExploreMoodFilter) {
            ExploreMoodFilter.ALL -> getString(R.string.filter_all)
            ExploreMoodFilter.CALM -> getString(R.string.explore_chip_calm)
            ExploreMoodFilter.ENERGY -> getString(R.string.explore_chip_energy)
        }
        val keywordText = if (tokens.isEmpty()) getString(R.string.home_filter_summary_no_tag) else tokens.joinToString(", ")
        exploreFilterSummary.text = getString(
            R.string.home_filter_summary_format,
            moodText,
            "$keywordText • ${filtered.size}"
        )

        explorePageInfo.text = if (exploreTotalPages == 0) {
            getString(R.string.explore_page_info_empty)
        } else {
            getString(R.string.explore_page_info, exploreCurrentPage + 1, exploreTotalPages)
        }

        val hasNextPage = exploreCurrentPage + 1 < exploreTotalPages
        exploreNextPageButton.isEnabled = hasNextPage
        exploreNextPageButton.alpha = if (hasNextPage) 1f else 0.45f
    }

    private fun renderSlots(slots: List<ProductSlot>, products: List<Product>) {
        slots.forEachIndexed { index, slot ->
            val product = products.getOrNull(index)
            val visible = product != null
            slot.card.isVisible = visible
            if (!visible) return@forEachIndexed
            bindImage(slot.image, product)
            slot.title.text = product.title
            slot.price.text = product.priceLabel()
            slot.artist.text = product.artist
        }
    }

    private fun matchesHomeFilter(product: Product): Boolean {
        if (selectedArtworkFilter == ArtworkFilter.ALL) return true
        val style = normalize(product.style)
        val tags = product.tags.joinToString(" ") { normalize(it) }
        val haystack = "$style $tags"

        return when (selectedArtworkFilter) {
            ArtworkFilter.ALL -> true
            ArtworkFilter.LANDSCAPE -> haystack.contains("landscape") || haystack.contains("phong canh")
            ArtworkFilter.PORTRAIT -> haystack.contains("portrait") || haystack.contains("chan dung")
            ArtworkFilter.ABSTRACT -> haystack.contains("abstract") || haystack.contains("truu tuong")
            ArtworkFilter.MODERN -> haystack.contains("modern") || haystack.contains("hien dai")
        }
    }

    private fun matchesExploreMood(product: Product): Boolean {
        if (selectedExploreMoodFilter == ExploreMoodFilter.ALL) return true
        val tags = normalize(product.tags.joinToString(" "))
        val style = normalize(product.style)
        val haystack = "$tags $style"

        return when (selectedExploreMoodFilter) {
            ExploreMoodFilter.ALL -> true
            ExploreMoodFilter.CALM -> listOf("calm", "yen binh", "landscape", "nature").any { haystack.contains(it) }
            ExploreMoodFilter.ENERGY -> listOf("energy", "abstract", "modern", "urban").any { haystack.contains(it) }
        }
    }

    private fun updateHomeFilterSummary(queryTokens: List<String>) {
        val filterName = when (selectedArtworkFilter) {
            ArtworkFilter.ALL -> getString(R.string.filter_all)
            ArtworkFilter.LANDSCAPE -> getString(R.string.filter_landscape_short)
            ArtworkFilter.PORTRAIT -> getString(R.string.filter_portrait_short)
            ArtworkFilter.ABSTRACT -> getString(R.string.filter_abstract_short)
            ArtworkFilter.MODERN -> getString(R.string.filter_modern_short)
        }
        val tagHints = queryTokens.filter { it.startsWith("#") }
            .joinToString(separator = ", ")
            .ifBlank { getString(R.string.home_filter_summary_no_tag) }
        filterSummary.text = getString(R.string.home_filter_summary_format, filterName, tagHints)
    }

    private fun openArtworkDetail(product: Product) {
        currentDetailProduct = product

        bindImage(detailMainImage, product)
        bindImage(detailArtistAvatar, product)
        detailTitle.text = product.title
        detailArtistName.text = product.artist
        detailRating.text = product.ratingText
        detailStoryContent.text = product.description
        detailMaterialValueText.text = product.material
        detailSizeValueText.text = product.size
        detailPrice.text = product.priceCompactLabel()
        detailPriceFloating.text = product.priceCompactLabel()

        updateFavoriteButtonUi(favoriteDb.isFavorite(product.id))
        bindRelatedImages(product)

        homeScreen.isVisible = false
        exploreScreen.isVisible = false
        profileScreen.isVisible = false
        detailScreen.isVisible = true
        detailFloatingTopBar.isVisible = true
        detailFloatingPriceBar.isVisible = true
        detailImageFullscreenOverlay.isVisible = false
        bottomNav.isVisible = false

        lifecycleScope.launch {
            val detailedProduct = productGateway.getProductDetail(product.id) ?: return@launch
            val current = currentDetailProduct ?: return@launch
            if (current.id != detailedProduct.id) return@launch

            currentDetailProduct = detailedProduct
            displayProducts = displayProducts.map { if (it.id == detailedProduct.id) detailedProduct else it }

            bindImage(detailMainImage, detailedProduct)
            bindImage(detailArtistAvatar, detailedProduct)
            detailTitle.text = detailedProduct.title
            detailArtistName.text = detailedProduct.artist
            detailRating.text = detailedProduct.ratingText
            detailStoryContent.text = detailedProduct.description
            detailMaterialValueText.text = detailedProduct.material
            detailSizeValueText.text = detailedProduct.size
            detailPrice.text = detailedProduct.priceCompactLabel()
            detailPriceFloating.text = detailedProduct.priceCompactLabel()
            bindRelatedImages(detailedProduct)
        }
    }

    private fun bindRelatedImages(current: Product) {
        val related = relatedProducts(current)
        detailRelatedProducts = related
        val first = related.getOrNull(0)
        val second = related.getOrNull(1)

        detailRelatedCardOne.isVisible = first != null
        detailRelatedCardTwo.isVisible = second != null
        detailRelatedSection.isVisible = first != null || second != null
        detailRelatedEmpty.isVisible = first == null && second == null

        if (first != null) {
            bindImage(detailRelatedImageOne, first)
            detailRelatedTitleOne.text = first.title
            detailRelatedPriceOne.text = first.priceCompactLabel()
        }
        if (second != null) {
            bindImage(detailRelatedImageTwo, second)
            detailRelatedTitleTwo.text = second.title
            detailRelatedPriceTwo.text = second.priceCompactLabel()
        }
    }

    private fun relatedProducts(current: Product? = currentDetailProduct): List<Product> {
        val base = current ?: return emptyList()
        return displayProducts
            .filter { it.id != base.id }
            .shuffled(Random(System.currentTimeMillis()))
    }

    private fun closeArtworkDetail() {
        showTab(Tab.HOME)
    }

    private fun updateFavoriteButtonUi(isFavorite: Boolean) {
        val tint = if (isFavorite) R.color.favorite_red else android.R.color.white
        detailFavoriteButton.imageTintList = ColorStateList.valueOf(color(tint))
    }

    private fun showUploadDialog() {
        pendingUploadImageUri = null
        val content = LayoutInflater.from(this).inflate(R.layout.dialog_upload_product, null)
        val uploadPreview = content.findViewById<ImageView>(R.id.imgUploadPreview)
        val pickImageButton = content.findViewById<MaterialButton>(R.id.btnPickUploadImage)
        val titleInput = content.findViewById<EditText>(R.id.edtUploadTitle)
        val artistInput = content.findViewById<EditText>(R.id.edtUploadArtist)
        val priceInput = content.findViewById<EditText>(R.id.edtUploadPriceVnd)
        val styleInput = content.findViewById<EditText>(R.id.edtUploadStyle)
        val materialInput = content.findViewById<EditText>(R.id.edtUploadMaterial)
        val sizeInput = content.findViewById<EditText>(R.id.edtUploadSize)
        val locationInput = content.findViewById<EditText>(R.id.edtUploadLocation)
        val descriptionInput = content.findViewById<EditText>(R.id.edtUploadDescription)
        val tagsInput = content.findViewById<EditText>(R.id.edtUploadTags)

        pickImageButton.setOnClickListener {
            pickImageLauncher.launch(arrayOf("image/*"))
        }

        AlertDialog.Builder(this)
            .setTitle(R.string.upload_product_title)
            .setView(content)
            .setNegativeButton(R.string.upload_cancel, null)
            .setPositiveButton(R.string.upload_submit, null)
            .create()
            .also { dialog ->
                dialog.setOnShowListener {
                    // Polling nhẹ để preview ảnh mới chọn trong khi dialog đang mở.
                    val watcher = object : Runnable {
                        override fun run() {
                            val uri = pendingUploadImageUri
                            if (uri != null) uploadPreview.setImageURI(uri)
                            if (dialog.isShowing) uploadPreview.postDelayed(this, 250)
                        }
                    }
                    uploadPreview.post(watcher)

                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val title = titleInput.text?.toString().orEmpty().trim()
                        val artist = artistInput.text?.toString().orEmpty().trim()
                        val style = styleInput.text?.toString().orEmpty().trim()
                        val material = materialInput.text?.toString().orEmpty().trim()
                        val size = sizeInput.text?.toString().orEmpty().trim()
                        val location = locationInput.text?.toString().orEmpty().trim()
                        val description = descriptionInput.text?.toString().orEmpty().trim()
                        val priceRaw = priceInput.text?.toString().orEmpty().trim()
                        val price = priceRaw.toLongOrNull()
                        val tags = tagsInput.text?.toString().orEmpty()
                            .split(',')
                            .map { it.trim() }
                            .filter { it.isNotBlank() }

                        if (title.isBlank() || artist.isBlank() || style.isBlank() || material.isBlank() || size.isBlank() || location.isBlank() || description.isBlank()) {
                            toast(getString(R.string.upload_error_required))
                            return@setOnClickListener
                        }
                        if (price == null || price <= 0L) {
                            toast(getString(R.string.upload_error_price))
                            return@setOnClickListener
                        }

                        val payload = UploadProductPayload(
                            title = title,
                            artist = artist,
                            priceVnd = price,
                            style = style,
                            tags = tags,
                            description = description,
                            material = material,
                            size = size,
                            location = location,
                            imageUri = pendingUploadImageUri?.toString()
                        )
                        lifecycleScope.launch {
                            productGateway.createProduct(payload)
                            pushNotification(getString(R.string.notification_uploaded_product, title))
                            refreshProducts()
                            toast(getString(R.string.upload_success))
                            dialog.dismiss()
                        }
                    }
                }
                dialog.show()
            }
    }

    private fun bindImage(target: ImageView, product: Product) {
        val uri = product.imageUri?.let { runCatching { Uri.parse(it) }.getOrNull() }
        if (uri != null) {
            target.setImageURI(uri)
        } else {
            target.setImageResource(product.imageRes)
        }
    }

    private fun openFullscreenImage() {
        val product = currentDetailProduct ?: return
        bindImage(detailFullscreenImage, product)
        resetFullscreenImageTransform()
        detailImageFullscreenOverlay.isVisible = true
    }

    private fun closeFullscreenImage() {
        detailImageFullscreenOverlay.isVisible = false
        resetFullscreenImageTransform()
    }

    private fun setupFullscreenImageGestures() {
        fullscreenScaleDetector = ScaleGestureDetector(this, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val nextScale = (fullscreenScale * detector.scaleFactor).coerceIn(1f, 5f)
                fullscreenScale = nextScale
                detailFullscreenImage.scaleX = fullscreenScale
                detailFullscreenImage.scaleY = fullscreenScale
                return true
            }
        })

        fullscreenTapDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean = true

            override fun onDoubleTap(e: MotionEvent): Boolean {
                fullscreenScale = if (fullscreenScale > 1f) 1f else 2.5f
                detailFullscreenImage.scaleX = fullscreenScale
                detailFullscreenImage.scaleY = fullscreenScale
                if (fullscreenScale == 1f) {
                    detailFullscreenImage.translationX = 0f
                    detailFullscreenImage.translationY = 0f
                }
                return true
            }
        })

        detailFullscreenImage.setOnTouchListener { view, event ->
            fullscreenTapDetector.onTouchEvent(event)
            fullscreenScaleDetector.onTouchEvent(event)

            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    lastTouchX = event.rawX
                    lastTouchY = event.rawY
                    fullscreenDownX = event.x
                    fullscreenDownY = event.y
                    fullscreenMoved = false
                }

                MotionEvent.ACTION_MOVE -> {
                    if (!fullscreenMoved) {
                        val touchSlop = ViewConfiguration.get(this).scaledTouchSlop
                        val movedX = kotlin.math.abs(event.x - fullscreenDownX)
                        val movedY = kotlin.math.abs(event.y - fullscreenDownY)
                        fullscreenMoved = movedX > touchSlop || movedY > touchSlop
                    }

                    if (fullscreenScale > 1f && !fullscreenScaleDetector.isInProgress) {
                        val dx = event.rawX - lastTouchX
                        val dy = event.rawY - lastTouchY
                        detailFullscreenImage.translationX += dx
                        detailFullscreenImage.translationY += dy
                        lastTouchX = event.rawX
                        lastTouchY = event.rawY
                    }
                }

                MotionEvent.ACTION_UP -> {
                    val isTapOutsideImage =
                        !fullscreenMoved &&
                            fullscreenScale <= 1f &&
                            event.pointerCount == 1 &&
                            !isInsideDisplayedImage(detailFullscreenImage, event.x, event.y)
                    if (isTapOutsideImage) {
                        closeFullscreenImage()
                        return@setOnTouchListener true
                    }
                    view.performClick()
                }
            }

            true
        }
    }

    private fun isInsideDisplayedImage(imageView: ImageView, touchX: Float, touchY: Float): Boolean {
        val drawable = imageView.drawable ?: return false
        val viewWidth = imageView.width.toFloat()
        val viewHeight = imageView.height.toFloat()
        if (viewWidth <= 0f || viewHeight <= 0f || drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
            return false
        }

        val scale = minOf(
            viewWidth / drawable.intrinsicWidth.toFloat(),
            viewHeight / drawable.intrinsicHeight.toFloat()
        )
        val displayedWidth = drawable.intrinsicWidth * scale
        val displayedHeight = drawable.intrinsicHeight * scale
        val left = (viewWidth - displayedWidth) / 2f
        val top = (viewHeight - displayedHeight) / 2f
        val right = left + displayedWidth
        val bottom = top + displayedHeight

        return touchX in left..right && touchY in top..bottom
    }

    private fun resetFullscreenImageTransform() {
        fullscreenScale = 1f
        detailFullscreenImage.scaleX = 1f
        detailFullscreenImage.scaleY = 1f
        detailFullscreenImage.translationX = 0f
        detailFullscreenImage.translationY = 0f
    }

    private fun focusSearchInput() {
        searchInput.requestFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.showSoftInput(searchInput, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun focusExploreSearchInput() {
        exploreSearchInput.requestFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.showSoftInput(exploreSearchInput, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun applyTabState(tabView: View, icon: ImageView, label: TextView, selected: Boolean) {
        tabView.setBackgroundResource(if (selected) R.drawable.bg_chip_selected else android.R.color.transparent)
        label.setTextColor(color(if (selected) R.color.white else R.color.text_secondary))
        icon.imageTintList = ColorStateList.valueOf(color(if (selected) R.color.white else R.color.text_secondary))
        tabView.alpha = if (selected) 1f else 0.85f
    }

    private fun normalize(text: String): String {
        val normalized = Normalizer.normalize(text, Normalizer.Form.NFD)
        return normalized.replace("\\p{Mn}+".toRegex(), "")
            .lowercase(Locale.getDefault())
            .trim()
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun pushNotification(message: String) {
        notifications.add(0, AppNotification(message = message, isRead = false))
        renderNotificationBadge()
    }

    private fun renderNotificationBadge() {
        if (!::exploreNotificationBadge.isInitialized) return
        val unreadCount = notifications.count { !it.isRead }
        exploreNotificationBadge.isVisible = unreadCount > 0
        if (unreadCount > 0) {
            exploreNotificationBadge.text = if (unreadCount > 99) "99+" else unreadCount.toString()
        }
    }

    private fun openNotificationsDialog() {
        val message = if (notifications.isEmpty()) {
            getString(R.string.notifications_empty)
        } else {
            notifications.joinToString(separator = "\n\n") { "• ${it.message}" }
        }

        AlertDialog.Builder(this)
            .setTitle(R.string.notifications_title)
            .setMessage(message)
            .setPositiveButton(R.string.notifications_close, null)
            .show()

        notifications.forEach { it.isRead = true }
        renderNotificationBadge()
    }

    override fun onDestroy() {
        super.onDestroy()
        favoriteDb.close()
    }

    private fun color(colorRes: Int): Int = ContextCompat.getColor(this, colorRes)
}