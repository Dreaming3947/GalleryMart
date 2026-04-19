# Android Folder Structure Guide (Kotlin)

Muc tieu: team frontend co cung 1 structure, de chia viec nhanh va merge de.

## 1. Recommended Architecture

- Pattern: MVVM + Repository
- UI: Jetpack Compose (neu team da quen), hoac XML neu can an toan
- Dependency Injection: Hilt
- Network: Retrofit + OkHttp
- Async: Coroutines + Flow

## 2. Project Package Root

Vi du package root:

- com.gallerymart.app

Cau truc tong quan:

- app/src/main/java/com/gallerymart/app
  - core
    - network
    - auth
    - ui
    - util
  - data
    - remote
    - local
    - repository
    - mapper
  - domain
    - model
    - repository
    - usecase
  - feature
    - auth
      - ui
      - vm
      - model
    - profile
      - ui
      - vm
      - model
    - artwork
      - ui
      - vm
      - model
    - order
      - ui
      - vm
      - model
    - notification
      - ui
      - vm
      - model
  - navigation
  - MainActivity.kt
  - GalleryMartApp.kt

## 3. Feature-level Structure

Moi feature dung 1 form giong nhau:

- feature/<feature-name>
  - ui
    - screen
    - component
  - vm
    - <Feature>ViewModel.kt
    - <Feature>UiState.kt
    - <Feature>UiEvent.kt
  - model
    - <Feature>UiModel.kt

Vi du:

- feature/artwork/ui/screen/ArtworkFeedScreen.kt
- feature/artwork/ui/screen/ArtworkDetailScreen.kt
- feature/artwork/vm/ArtworkViewModel.kt
- feature/artwork/vm/ArtworkUiState.kt

## 4. Data Layer Structure

- data/remote
  - api
    - AuthApi.kt
    - ArtworkApi.kt
    - OrderApi.kt
    - NotificationApi.kt
  - dto
    - request
    - response
- data/repository
  - AuthRepositoryImpl.kt
  - ArtworkRepositoryImpl.kt
  - OrderRepositoryImpl.kt
  - NotificationRepositoryImpl.kt
- data/mapper
  - AuthMapper.kt
  - ArtworkMapper.kt
  - OrderMapper.kt

## 5. Core Layer Structure

- core/network
  - RetrofitProvider.kt
  - AuthInterceptor.kt
  - NetworkModule.kt
- core/auth
  - TokenManager.kt
  - SessionManager.kt
- core/ui
  - component
  - theme
- core/util
  - Resource.kt
  - ErrorParser.kt

## 6. Navigation Structure

- navigation
  - AppNavGraph.kt
  - AppRoutes.kt
  - AuthGraph.kt
  - BuyerGraph.kt
  - SellerGraph.kt

## 7. Folder Ownership for 5 UI Members

Member A (Auth + Navigation shell):
- feature/auth/**
- navigation/**
- core/auth/**

Member B (Buyer feed + detail):
- feature/artwork/ui/**
- feature/artwork/vm/**

Member C (Buyer checkout + order tracking):
- feature/order/ui/buyer/**
- feature/order/vm/**

Member D (Seller listing + inventory):
- feature/artwork/ui/seller/**
- feature/artwork/vm/seller/**

Member E (Seller order handling + notifications + shared UI):
- feature/order/ui/seller/**
- feature/notification/**
- core/ui/component/**

## 8. Quick Create Folders (PowerShell)

Chay trong root Android project:

```powershell
$base = "app/src/main/java/com/gallerymart/app"
$folders = @(
  "$base/core/network", "$base/core/auth", "$base/core/ui/component", "$base/core/ui/theme", "$base/core/util",
  "$base/data/remote/api", "$base/data/remote/dto/request", "$base/data/remote/dto/response",
  "$base/data/repository", "$base/data/mapper",
  "$base/domain/model", "$base/domain/repository", "$base/domain/usecase",
  "$base/feature/auth/ui/screen", "$base/feature/auth/ui/component", "$base/feature/auth/vm", "$base/feature/auth/model",
  "$base/feature/profile/ui/screen", "$base/feature/profile/vm", "$base/feature/profile/model",
  "$base/feature/artwork/ui/screen", "$base/feature/artwork/ui/component", "$base/feature/artwork/vm", "$base/feature/artwork/model",
  "$base/feature/order/ui/buyer", "$base/feature/order/ui/seller", "$base/feature/order/vm", "$base/feature/order/model",
  "$base/feature/notification/ui/screen", "$base/feature/notification/vm", "$base/feature/notification/model",
  "$base/navigation"
)

$folders | ForEach-Object { New-Item -ItemType Directory -Path $_ -Force | Out-Null }
```

## 9. API Mapping to Existing Backend

Frontend can implement ngay theo backend da co:

- Auth: /api/auth/register, /api/auth/login, /api/auth/me, /api/auth/me (PATCH), /api/auth/me/enable-seller
- Artwork: /api/artworks, /api/artworks/{id}, /api/artworks/my, create/update/delete
- Order: /api/orders and order actions
- Notification: /api/notifications, /api/notifications/read-all

## 10. Scope Lock (small scale)

Do now:
- Auth
- Feed + detail
- Buy now + order tracking
- Seller post/manage artwork
- Seller confirm/cancel order
- Notifications

Do later:
- Chat
- Revenue analytics
- Complex cart (multi-item)
