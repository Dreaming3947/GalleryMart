PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS artists (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL,
  slug TEXT NOT NULL UNIQUE,
  bio TEXT DEFAULT ''
);

CREATE TABLE IF NOT EXISTS categories (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL,
  slug TEXT NOT NULL UNIQUE,
  icon TEXT,
  image_path TEXT,
  sort_order INTEGER NOT NULL DEFAULT 0,
  is_active INTEGER NOT NULL DEFAULT 1 CHECK (is_active IN (0, 1))
);

CREATE TABLE IF NOT EXISTS artworks (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  title TEXT NOT NULL,
  slug TEXT NOT NULL UNIQUE,
  artist_id INTEGER NOT NULL,
  style TEXT NOT NULL,
  medium TEXT NOT NULL,
  price_vnd INTEGER NOT NULL,
  rating REAL NOT NULL DEFAULT 0,
  is_featured INTEGER NOT NULL DEFAULT 0 CHECK (is_featured IN (0, 1)),
  is_hot INTEGER NOT NULL DEFAULT 0 CHECK (is_hot IN (0, 1)),
  is_auction INTEGER NOT NULL DEFAULT 0 CHECK (is_auction IN (0, 1)),
  image_path TEXT NOT NULL,
  description TEXT DEFAULT '',
  status TEXT NOT NULL DEFAULT 'active',
  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (artist_id) REFERENCES artists(id)
);

CREATE TABLE IF NOT EXISTS artwork_categories (
  artwork_id INTEGER NOT NULL,
  category_id INTEGER NOT NULL,
  PRIMARY KEY (artwork_id, category_id),
  FOREIGN KEY (artwork_id) REFERENCES artworks(id) ON DELETE CASCADE,
  FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS home_banners (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  title TEXT NOT NULL,
  subtitle TEXT NOT NULL,
  image_path TEXT NOT NULL,
  cta_primary TEXT,
  cta_secondary TEXT,
  sort_order INTEGER NOT NULL DEFAULT 0,
  is_active INTEGER NOT NULL DEFAULT 1 CHECK (is_active IN (0, 1))
);

CREATE TABLE IF NOT EXISTS home_sections (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  section_key TEXT NOT NULL UNIQUE,
  title TEXT,
  sort_order INTEGER NOT NULL DEFAULT 0,
  is_active INTEGER NOT NULL DEFAULT 1 CHECK (is_active IN (0, 1))
);

CREATE TABLE IF NOT EXISTS home_section_items (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  section_id INTEGER NOT NULL,
  item_type TEXT NOT NULL CHECK (item_type IN ('banner', 'category', 'artwork')),
  banner_id INTEGER,
  category_id INTEGER,
  artwork_id INTEGER,
  sort_order INTEGER NOT NULL DEFAULT 0,
  FOREIGN KEY (section_id) REFERENCES home_sections(id) ON DELETE CASCADE,
  FOREIGN KEY (banner_id) REFERENCES home_banners(id) ON DELETE CASCADE,
  FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE,
  FOREIGN KEY (artwork_id) REFERENCES artworks(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_artworks_title ON artworks(title);
CREATE INDEX IF NOT EXISTS idx_artworks_style ON artworks(style);
CREATE INDEX IF NOT EXISTS idx_artworks_featured ON artworks(is_featured, is_hot, is_auction);
CREATE INDEX IF NOT EXISTS idx_artworks_price ON artworks(price_vnd);
CREATE INDEX IF NOT EXISTS idx_categories_sort ON categories(sort_order);

