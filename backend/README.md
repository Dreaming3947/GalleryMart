# Backend Seed Package

This folder gives you a starter DB design and homepage seed data using the images you added in `app/sampledata`.

## Files

- `backend/sql/01_schema.sql`: database schema
- `backend/sql/02_seed_homepage.sql`: seed data mapped to:
  - `app/sampledata/1.jpg`
  - `app/sampledata/2.jpg`
  - `app/sampledata/3.jpg`
  - `app/sampledata/4.jpg`
  - `app/sampledata/5.jpg`
  - `app/sampledata/6.jpg`
  - `app/sampledata/7.jpg`
- `backend/docs/api-homepage.md`: API contract suggestion for home screen
- `backend/scripts/verify_seed.py`: quick SQLite validation

## Quick validate with SQLite

```powershell
python -u "C:\Users\lemin\AndroidStudioProjects\trangchu\backend\scripts\verify_seed.py"
```

Expected output includes counts for artists/categories/artworks and a sample search result.

## If you use MySQL/PostgreSQL

- Keep the same table design.
- Replace SQLite identity syntax as needed:
  - PostgreSQL: `BIGSERIAL` or `GENERATED ALWAYS AS IDENTITY`
  - MySQL: `BIGINT AUTO_INCREMENT`

## How to use these images in API

In API response, return `imagePath` from DB.
Example:

```json
{
  "id": 1,
  "title": "Binh Minh Tren Bien",
  "imagePath": "app/sampledata/1.jpg"
}
```

Then your backend can map this to a static route (for example `/static/sampledata/1.jpg`).

