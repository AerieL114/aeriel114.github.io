INSERT INTO inventory (sku_code, quantity) VALUES
('ts001_black_m', 10),
('je001_blue_32', 5),
('sh001_white_l', 0)
ON CONFLICT (sku_code) DO NOTHING;
