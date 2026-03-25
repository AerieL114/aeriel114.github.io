INSERT INTO products (name, description, price, sku_code, category, size, color, image_url) VALUES
('Ao thun basic', 'Ao thun cotton', 199, 'ts001_black_m', 'ao-thun', 'M', 'black', '/assets/products/ts001_black_m.jpg'),
('Quan jeans slim', 'Quan jeans slim fit', 399, 'je001_blue_32', 'quan-jeans', '32', 'blue', '/assets/products/je001_blue_32.jpg'),
('Ao so mi', 'Ao so mi tay dai', 299, 'sh001_white_l', 'ao-so-mi', 'L', 'white', '/assets/products/sh001_white_l.jpg')
ON CONFLICT (sku_code) DO NOTHING;
