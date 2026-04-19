# Homepage API (proposal)

This API set is enough for the current Android home screen.

## 1) Get homepage aggregate

`GET /api/home`

### Response

```json
{
  "banner": {
    "title": "Discover Unique Artworks",
    "subtitle": "Buy and sell original paintings from artists worldwide",
    "imagePath": "app/sampledata/1.jpg",
    "ctaPrimary": "Explore Gallery",
    "ctaSecondary": "Sell Art"
  },
  "categories": [
    { "id": 1, "name": "Landscape", "slug": "landscape", "imagePath": "app/sampledata/1.jpg" },
    { "id": 2, "name": "Portrait", "slug": "portrait", "imagePath": "app/sampledata/2.jpg" }
  ],
  "featuredArtworks": [
    {
      "id": 1,
      "title": "Binh Minh Tren Bien",
      "artistName": "Le The Anh",
      "priceVnd": 16000000,
      "rating": 4.8,
      "imagePath": "app/sampledata/1.jpg",
      "isHot": true,
      "isAuction": false
    }
  ],
  "filters": ["all", "landscape", "portrait", "abstract", "modern"]
}
```

---

## 2) Search + filter artworks

`GET /api/artworks?query={keyword}&style={style}&category={slug}&featured={0|1}&page=1&limit=10`

### Notes
- `query`: search by title + artist name
- `style`: `landscape | portrait | abstract | modern`
- `category`: category slug

---

## 3) Get categories

`GET /api/categories`

---

## 4) Profile summary for bottom tab

`GET /api/users/me/summary`

### Response

```json
{
  "favoritesCount": 24,
  "ordersCount": 8
}
```

