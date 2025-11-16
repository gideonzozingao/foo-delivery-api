-- ============= V1__init_schema.sql =============
-- Create roles table
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- Create users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create user_roles join table
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Create categories table
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500)
);

-- Create menu_items table
CREATE TABLE menu_items (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    image_url VARCHAR(500),
    category_id BIGINT NOT NULL,
    available BOOLEAN NOT NULL DEFAULT TRUE,
    vegetarian BOOLEAN NOT NULL DEFAULT FALSE,
    vegan BOOLEAN NOT NULL DEFAULT FALSE,
    preparation_time INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT
);

-- Create addresses table
CREATE TABLE addresses (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    country VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20),
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    label VARCHAR(50),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create carts table
CREATE TABLE carts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create cart_items table
CREATE TABLE cart_items (
    id BIGSERIAL PRIMARY KEY,
    cart_id BIGINT NOT NULL,
    menu_item_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    special_instructions TEXT,
    FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE,
    FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE CASCADE
);

-- Create orders table
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    delivery_address_id BIGINT NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    delivery_fee DECIMAL(10, 2) NOT NULL,
    tax DECIMAL(10, 2) NOT NULL,
    total DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    special_instructions TEXT,
    estimated_delivery_time TIMESTAMP,
    actual_delivery_time TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    FOREIGN KEY (delivery_address_id) REFERENCES addresses(id) ON DELETE RESTRICT
);

-- Create order_items table
CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    menu_item_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    price_at_order DECIMAL(10, 2) NOT NULL,
    special_instructions TEXT,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE RESTRICT
);

-- Create payments table
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE,
    amount DECIMAL(10, 2) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    transaction_id VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_menu_items_category ON menu_items(category_id);
CREATE INDEX idx_menu_items_available ON menu_items(available);
CREATE INDEX idx_orders_user ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at);
CREATE INDEX idx_cart_items_cart ON cart_items(cart_id);

-- ============= V2__insert_roles.sql =============
-- Insert default roles
INSERT INTO roles (name, description) VALUES 
('ROLE_CUSTOMER', 'Customer role with basic access'),
('ROLE_ADMIN', 'Administrator role with full access'),
('ROLE_RESTAURANT', 'Restaurant staff role'),
('ROLE_DELIVERY', 'Delivery personnel role');

-- ============= V3__sample_menu_items.sql =============
-- Insert sample categories
INSERT INTO categories (name, description) VALUES 
('Appetizers', 'Start your meal with our delicious appetizers'),
('Main Course', 'Hearty and satisfying main dishes'),
('Desserts', 'Sweet treats to end your meal'),
('Beverages', 'Refreshing drinks and beverages'),
('Salads', 'Fresh and healthy salad options'),
('Pizza', 'Wood-fired pizzas with various toppings');

-- Insert sample menu items
INSERT INTO menu_items (name, description, price, category_id, available, vegetarian, vegan, preparation_time) VALUES 
-- Appetizers
('Spring Rolls', 'Crispy vegetable spring rolls served with sweet chili sauce', 8.99, 1, true, true, true, 15),
('Chicken Wings', 'Buffalo wings with blue cheese dip', 12.99, 1, true, false, false, 20),
('Mozzarella Sticks', 'Golden fried mozzarella with marinara sauce', 9.99, 1, true, true, false, 15),

-- Main Course
('Grilled Chicken', 'Herb-marinated grilled chicken breast with vegetables', 18.99, 2, true, false, false, 30),
('Beef Burger', 'Juicy beef burger with lettuce, tomato, and fries', 15.99, 2, true, false, false, 25),
('Vegetable Stir Fry', 'Mixed vegetables in Asian sauce with rice', 14.99, 2, true, true, true, 20),
('Salmon Fillet', 'Pan-seared salmon with lemon butter sauce', 24.99, 2, true, false, false, 30),

-- Pizza
('Margherita Pizza', 'Classic pizza with tomato sauce, mozzarella, and basil', 13.99, 6, true, true, false, 25),
('Pepperoni Pizza', 'Loaded with pepperoni and extra cheese', 15.99, 6, true, false, false, 25),
('Veggie Supreme', 'Bell peppers, mushrooms, onions, olives, and tomatoes', 14.99, 6, true, true, false, 25),

-- Salads
('Caesar Salad', 'Romaine lettuce with Caesar dressing and croutons', 10.99, 5, true, true, false, 10),
('Greek Salad', 'Fresh vegetables with feta cheese and olives', 11.99, 5, true, true, false, 10),
('Garden Salad', 'Mixed greens with vinaigrette', 8.99, 5, true, true, true, 10),

-- Desserts
('Chocolate Cake', 'Rich chocolate cake with chocolate frosting', 7.99, 3, true, true, false, 5),
('Tiramisu', 'Classic Italian dessert with coffee and mascarpone', 8.99, 3, true, true, false, 5),
('Ice Cream Sundae', 'Three scoops with toppings of your choice', 6.99, 3, true, true, false, 5),

-- Beverages
('Coca Cola', 'Classic soft drink', 2.99, 4, true, true, true, 2),
('Fresh Orange Juice', 'Freshly squeezed orange juice', 4.99, 4, true, true, true, 5),
('Iced Tea', 'Refreshing iced tea', 2.99, 4, true, true, true, 2),
('Coffee', 'Freshly brewed coffee', 3.99, 4, true, true, true, 5);