# GalleryMart Frontend (XML + API)

Android frontend converted sang XML theo mockup:
- Home, Explore, Detail giao dien theo style art marketplace.
- Bottom navigation click duoc va chuyen tab Home/Explore/Profile.
- Co goi API backend `/api/artworks` de hien thi danh sach tac pham.

## Structure

- `app/src/main/res/layout/activity_main.xml`
- `app/src/main/res/layout/fragment_home.xml`
- `app/src/main/res/layout/fragment_explore.xml`
- `app/src/main/res/layout/activity_artwork_detail.xml`
- `app/src/main/java/com/gallerymart/app/feature/home/ui/HomeFragment.kt`
- `app/src/main/java/com/gallerymart/app/feature/explore/ui/ExploreFragment.kt`
- `app/src/main/java/com/gallerymart/app/feature/detail/ui/ArtworkDetailActivity.kt`
- `app/src/main/java/com/gallerymart/app/data/remote/api/ArtworkApi.kt`

## Run

Open `frontend-android` with Android Studio and run `app`.

Neu dung emulator Android, backend local mac dinh qua `10.0.2.2:8080`.

If you use terminal and already have Gradle + Android SDK configured:

```powershell
Set-Location "C:\Users\lemin\AndroidStudioProjects\GalleryMarthuyend\frontend-android"
gradle assembleDebug
```

## Notes

- BASE_URL duoc cau hinh tai `gradle.properties` qua key `GALLERYMART_BASE_URL`.
- Emulator Android: de `GALLERYMART_BASE_URL=http://10.0.2.2:8080/`.
- Device that cung mang LAN: doi sang `GALLERYMART_BASE_URL=http://<LAN_IP_MAY_BACKEND>:8080/`.
- Anh du lieu lay tu API (fallback se dung anh placeholder neu `imageUrl` rong).


