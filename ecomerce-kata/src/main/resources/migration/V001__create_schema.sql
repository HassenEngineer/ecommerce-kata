-- Tables principales
CREATE TABLE product (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    price       NUMERIC(19,2) NOT NULL,
    stock_quantity INTEGER NOT NULL
);

CREATE TABLE category (
    id                 BIGSERIAL PRIMARY KEY,
    name               VARCHAR(255) NOT NULL,
    description        VARCHAR(255),
    parent_category_id BIGINT,
    CONSTRAINT fk_category_parent FOREIGN KEY (parent_category_id) REFERENCES category(id)
);

CREATE TABLE cart (
    id         BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE cart_item (
    id         BIGSERIAL PRIMARY KEY,
    quantity   INTEGER NOT NULL,
    cart_id    BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    CONSTRAINT fk_cart_item_cart    FOREIGN KEY (cart_id)    REFERENCES cart(id),
    CONSTRAINT fk_cart_item_product FOREIGN KEY (product_id) REFERENCES product(id)
);

-- Table de jointure Many-to-Many
CREATE TABLE category_product (
    category_id BIGINT NOT NULL,
    product_id  BIGINT NOT NULL,
    PRIMARY KEY (category_id, product_id),
    CONSTRAINT fk_cp_category FOREIGN KEY (category_id) REFERENCES category(id),
    CONSTRAINT fk_cp_product  FOREIGN KEY (product_id)  REFERENCES product(id)
);
