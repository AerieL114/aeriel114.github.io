INSERT INTO products (name, description, category)
SELECT 'Ao thun basic', 'Ao thun cotton', 'ao-thun'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Ao thun basic');

INSERT INTO products (name, description, category)
SELECT 'Quan jeans slim', 'Quan jeans slim fit', 'quan-jeans'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Quan jeans slim');

INSERT INTO products (name, description, category)
SELECT 'Ao so mi', 'Ao so mi tay dai', 'ao-so-mi'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Ao so mi');

INSERT INTO product_variants (product_id, sku_code, size, color, image_url, price)
SELECT p.id, 'ts001_black_m', 'M', 'black', '/assets/products/ts001_black_m.jpg', 199
FROM products p
WHERE p.name = 'Ao thun basic'
  AND NOT EXISTS (SELECT 1 FROM product_variants WHERE sku_code = 'ts001_black_m');

INSERT INTO product_variants (product_id, sku_code, size, color, image_url, price)
SELECT p.id, 'je001_blue_32', '32', 'blue', '/assets/products/je001_blue_32.jpg', 399
FROM products p
WHERE p.name = 'Quan jeans slim'
  AND NOT EXISTS (SELECT 1 FROM product_variants WHERE sku_code = 'je001_blue_32');

INSERT INTO product_variants (product_id, sku_code, size, color, image_url, price)
SELECT p.id, 'sh001_white_l', 'L', 'white', '/assets/products/sh001_white_l.jpg', 299
FROM products p
WHERE p.name = 'Ao so mi'
  AND NOT EXISTS (SELECT 1 FROM product_variants WHERE sku_code = 'sh001_white_l');
