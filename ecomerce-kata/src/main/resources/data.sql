-- Catégories
INSERT INTO category (id, name, description, parent_category_id) VALUES
    (1, 'Electronique', 'Appareils et gadgets électroniques', NULL),
    (2, 'Smartphones', 'Téléphones intelligents', 1),
    (3, 'Laptops', 'Ordinateurs portables', 1),
    (4, 'Maison', 'Articles pour la maison', NULL),
    (5, 'Cuisine', 'Ustensiles et appareils de cuisine', 4);

-- Produits
INSERT INTO product (id, name, price, stock_quantity) VALUES
    (1, 'iPhone 15', 999.99, 50),
    (2, 'Samsung Galaxy S24', 849.99, 75),
    (3, 'MacBook Pro 14"', 1999.99, 30),
    (4, 'ThinkPad X1 Carbon', 1499.99, 40),
    (5, 'Robot Cuiseur', 299.99, 100),
    (6, 'Cafetière Expresso', 149.99, 60);

-- Associations catégorie-produit
INSERT INTO category_product (category_id, product_id) VALUES
    (2, 1), (2, 2),
    (3, 3), (3, 4),
    (5, 5), (5, 6),
    (1, 1), (1, 2), (1, 3), (1, 4);
