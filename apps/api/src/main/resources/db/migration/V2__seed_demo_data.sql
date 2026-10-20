-- Demo data so the API returns something right after `docker compose up`.
INSERT INTO branches (name, slug, address, phone)
VALUES ('Downtown', 'downtown', 'Abay Ave 10, Almaty', '+7 700 000 00 01'),
       ('Mega Park', 'mega-park', 'Makatayev St 127, Almaty', '+7 700 000 00 02');

INSERT INTO menu_items (branch_id, name, description, category, price, sort_order)
VALUES (1, 'Beef Burger', 'Beef patty, cheddar, pickles, house sauce', 'Burgers', 3490.00, 10),
       (1, 'Chicken Burger', 'Crispy chicken, lettuce, garlic mayo', 'Burgers', 2990.00, 20),
       (1, 'French Fries', 'Classic fries with sea salt', 'Sides', 990.00, 10),
       (1, 'Lemonade', 'Homemade lemonade, 0.4 l', 'Drinks', 890.00, 10),
       (2, 'Beef Burger', 'Beef patty, cheddar, pickles, house sauce', 'Burgers', 3690.00, 10),
       (2, 'Caesar Salad', 'Romaine, chicken, parmesan, croutons', 'Salads', 2790.00, 10);
