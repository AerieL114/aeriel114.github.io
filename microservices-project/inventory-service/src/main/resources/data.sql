INSERT INTO inventory (sku_code, warehouse_id, quantity_on_hand, quantity_reserved, reorder_point, reorder_quantity) VALUES
('ts001_black_m', 'default', 10, 0, 10, 50),
('je001_blue_32', 'default', 5, 0, 10, 50),
('sh001_white_l', 'default', 0, 0, 10, 50)
ON CONFLICT (sku_code) DO NOTHING;
