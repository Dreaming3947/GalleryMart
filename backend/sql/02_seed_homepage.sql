PRAGMA foreign_keys = ON;

DELETE FROM home_section_items;
DELETE FROM home_sections;
DELETE FROM home_banners;
DELETE FROM artwork_categories;
DELETE FROM artworks;
DELETE FROM categories;
DELETE FROM artists;

INSERT INTO artists (id, name, slug, bio) VALUES
  (1, 'Le The Anh', 'le-the-anh', 'Vietnamese painter'),
  (2, 'Pham Binh Chuong', 'pham-binh-chuong', 'Gallery artist'),
  (3, 'Nguyen Quoc Huy', 'nguyen-quoc-huy', 'Landscape and abstract painter'),
  (4, 'Tran Minh Khoa', 'tran-minh-khoa', 'Modern urban artist');

INSERT INTO categories (id, name, slug, icon, image_path, sort_order, is_active) VALUES
  (1, 'Landscape', 'landscape', 'ic_landscape', 'app/sampledata/1.jpg', 1, 1),
  (2, 'Portrait', 'portrait', 'ic_portrait', 'app/sampledata/2.jpg', 2, 1),
  (3, 'Abstract', 'abstract', 'ic_abstract', 'app/sampledata/3.jpg', 3, 1),
  (4, 'Modern', 'modern', 'ic_modern', 'app/sampledata/4.jpg', 4, 1);

INSERT INTO artworks (
  id, title, slug, artist_id, style, medium, price_vnd, rating,
  is_featured, is_hot, is_auction, image_path, description, status
) VALUES
  (1, 'Binh Minh Tren Bien', 'binh-minh-tren-bien', 1, 'landscape', 'oil', 16000000, 4.8, 1, 1, 0, 'app/sampledata/1.jpg', 'Warm sunrise over the sea.', 'active'),
  (2, 'Tinh Vat Hoa Hong', 'tinh-vat-hoa-hong', 2, 'portrait', 'oil', 12000000, 4.7, 1, 0, 0, 'app/sampledata/2.jpg', 'Portrait with rose still-life mood.', 'active'),
  (3, 'Mua Vang Tren Nui', 'mua-vang-tren-nui', 3, 'abstract', 'acrylic', 20000000, 4.9, 1, 0, 1, 'app/sampledata/3.jpg', 'Auction artwork with mountain color blocks.', 'active'),
  (4, 'May Den Pho Cu', 'may-den-pho-cu', 4, 'modern', 'mixed', 8000000, 4.6, 1, 1, 0, 'app/sampledata/4.jpg', 'Moody urban skyline.', 'active'),
  (5, 'Dem Im Lang', 'dem-im-lang', 1, 'modern', 'oil', 11000000, 4.5, 0, 0, 0, 'app/sampledata/5.jpg', 'Quiet night composition.', 'active'),
  (6, 'Pho Sau Mua', 'pho-sau-mua', 4, 'modern', 'acrylic', 14000000, 4.4, 0, 0, 0, 'app/sampledata/6.jpg', 'Street after rain.', 'active'),
  (7, 'Song Xanh', 'song-xanh', 3, 'landscape', 'watercolor', 9000000, 4.3, 0, 0, 0, 'app/sampledata/7.jpg', 'Blue river scene.', 'active');

INSERT INTO artwork_categories (artwork_id, category_id) VALUES
  (1, 1),
  (2, 2),
  (3, 3),
  (4, 4),
  (5, 4),
  (6, 4),
  (7, 1);

INSERT INTO home_banners (id, title, subtitle, image_path, cta_primary, cta_secondary, sort_order, is_active) VALUES
  (1, 'Discover Unique Artworks', 'Buy and sell original paintings from artists worldwide', 'app/sampledata/1.jpg', 'Explore Gallery', 'Sell Art', 1, 1);

INSERT INTO home_sections (id, section_key, title, sort_order, is_active) VALUES
  (1, 'banner', 'Banner', 1, 1),
  (2, 'categories', 'Categories', 2, 1),
  (3, 'featured', 'Featured Artworks', 3, 1);

INSERT INTO home_section_items (section_id, item_type, banner_id, category_id, artwork_id, sort_order) VALUES
  (1, 'banner', 1, NULL, NULL, 1),
  (2, 'category', NULL, 1, NULL, 1),
  (2, 'category', NULL, 2, NULL, 2),
  (2, 'category', NULL, 3, NULL, 3),
  (2, 'category', NULL, 4, NULL, 4),
  (3, 'artwork', NULL, NULL, 1, 1),
  (3, 'artwork', NULL, NULL, 2, 2),
  (3, 'artwork', NULL, NULL, 3, 3),
  (3, 'artwork', NULL, NULL, 4, 4);

