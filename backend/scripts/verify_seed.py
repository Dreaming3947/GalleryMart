import sqlite3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SQL_DIR = ROOT / "backend" / "sql"
DB_PATH = ROOT / "backend" / "sample.db"

schema_sql = (SQL_DIR / "01_schema.sql").read_text(encoding="utf-8")
seed_sql = (SQL_DIR / "02_seed_homepage.sql").read_text(encoding="utf-8")

if DB_PATH.exists():
    DB_PATH.unlink()

con = sqlite3.connect(DB_PATH)
con.executescript(schema_sql)
con.executescript(seed_sql)

queries = {
    "artists": "SELECT COUNT(*) FROM artists",
    "categories": "SELECT COUNT(*) FROM categories",
    "artworks": "SELECT COUNT(*) FROM artworks",
    "featured": "SELECT COUNT(*) FROM artworks WHERE is_featured = 1",
    "home_items": "SELECT COUNT(*) FROM home_section_items"
}

for name, sql in queries.items():
    count = con.execute(sql).fetchone()[0]
    print(f"{name}: {count}")

sample_search = con.execute(
    """
    SELECT a.title, ar.name, a.style, a.image_path
    FROM artworks a
    JOIN artists ar ON ar.id = a.artist_id
    WHERE lower(a.title) LIKE '%pho%' OR lower(ar.name) LIKE '%tran%'
    ORDER BY a.id
    """
).fetchall()

print("\nsearch sample:")
for row in sample_search:
    print(row)

con.close()
print(f"\nSQLite DB created: {DB_PATH}")

