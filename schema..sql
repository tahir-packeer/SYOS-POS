-- File: database/schema.sql
-- SYOS Database Schema

CREATE DATABASE IF NOT EXISTS syos;
USE syos;

-- Users table for authentication
CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('CASHIER', 'MANAGER', 'ADMIN', 'CUSTOMER') NOT NULL,
    email VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

-- Customers table
CREATE TABLE customers (
    customer_id INT PRIMARY KEY AUTO_INCREMENT,
    phone VARCHAR(15) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    address TEXT,
    user_id INT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Items/Products table
CREATE TABLE items (
    item_code VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    category VARCHAR(50) NOT NULL,
    description TEXT,
    min_stock_level INT DEFAULT 50,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Stock batches in warehouse
CREATE TABLE stock_batches (
    batch_id INT PRIMARY KEY AUTO_INCREMENT,
    item_code VARCHAR(20) NOT NULL,
    quantity INT NOT NULL,
    received_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    supplier_name VARCHAR(100),
    purchase_price DECIMAL(10,2),
    is_moved_to_shelf BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (item_code) REFERENCES items(item_code) ON DELETE CASCADE
);

-- Shelf stock (in-store)
CREATE TABLE shelf_stock (
    item_code VARCHAR(20) PRIMARY KEY,
    quantity INT NOT NULL DEFAULT 0,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (item_code) REFERENCES items(item_code) ON DELETE CASCADE
);

-- Website inventory (separate from shelf)
CREATE TABLE website_inventory (
    item_code VARCHAR(20) PRIMARY KEY,
    quantity INT NOT NULL DEFAULT 0,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (item_code) REFERENCES items(item_code) ON DELETE CASCADE
);

-- Bills/Invoices
CREATE TABLE bills (
    bill_id INT PRIMARY KEY AUTO_INCREMENT,
    bill_serial_number INT NOT NULL,
    bill_date DATE NOT NULL,
    customer_id INT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    discount DECIMAL(10,2) DEFAULT 0,
    cash_received DECIMAL(10,2),
    change_amount DECIMAL(10,2),
    transaction_type ENUM('IN_STORE', 'ONLINE') NOT NULL,
    bill_status ENUM('COMPLETED', 'PENDING', 'CANCELLED') DEFAULT 'COMPLETED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
);

-- Bill items
CREATE TABLE bill_items (
    bill_item_id INT PRIMARY KEY AUTO_INCREMENT,
    bill_id INT NOT NULL,
    item_code VARCHAR(20) NOT NULL,
    item_name VARCHAR(100) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (bill_id) REFERENCES bills(bill_id) ON DELETE CASCADE,
    FOREIGN KEY (item_code) REFERENCES items(item_code)
);

-- Indexes for performance
CREATE INDEX idx_bills_date ON bills(bill_date);
CREATE INDEX idx_bills_customer ON bills(customer_id);
CREATE INDEX idx_stock_batches_item ON stock_batches(item_code);
CREATE INDEX idx_stock_batches_expiry ON stock_batches(expiry_date);

-- Insert default admin user (password: admin123)
INSERT INTO users (username, password_hash, role, email) VALUES 
('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 
'ADMIN', 'admin@syos.com');

-- Insert sample cashier and manager
INSERT INTO users (username, password_hash, role, email) VALUES 
('cashier1', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'CASHIER', 'cashier1@syos.com'),
('manager1', 'ac9689e2272427085e35b9d3e3e8bed88cb3434828b43b86fc0596cad4c6e270', 'MANAGER', 'manager1@syos.com');

-- Insert sample items
INSERT INTO items (item_code, name, price, category, description, min_stock_level) VALUES
('RICE001', 'Basmati Rice 1kg', 350.00, 'GROCERY', 'Premium Basmati Rice', 100),
('MILK001', 'Fresh Milk 1L', 180.00, 'DAIRY', 'Fresh cow milk', 50),
('BREAD001', 'White Bread', 85.00, 'BAKERY', 'Fresh white bread', 30),
('TEA001', 'Ceylon Tea 100g', 120.00, 'BEVERAGE', 'Premium Ceylon tea', 75),
('SOAP001', 'Bath Soap', 45.00, 'PERSONAL_CARE', 'Antibacterial soap', 60);

-- Insert sample stock batches
INSERT INTO stock_batches (item_code, quantity, received_date, expiry_date, supplier_name, purchase_price) VALUES
('RICE001', 200, '2025-08-25', '2026-08-25', 'Rice Mills Ltd', 300.00),
('MILK001', 100, '2025-08-27', '2025-09-02', 'Dairy Farm Co', 150.00),
('BREAD001', 50, '2025-08-27', '2025-08-30', 'Fresh Bakery', 70.00),
('TEA001', 150, '2025-08-20', '2026-08-20', 'Tea Gardens Ltd', 100.00),
('SOAP001', 120, '2025-08-15', '2027-08-15', 'Soap Factory', 35.00);

-- Initialize shelf stock
INSERT INTO shelf_stock (item_code, quantity) VALUES
('RICE001', 80),
('MILK001', 40),
('BREAD001', 25),
('TEA001', 60),
('SOAP001', 45);

-- Initialize website inventory
INSERT INTO website_inventory (item_code, quantity) VALUES
('RICE001', 120),
('MILK001', 60),
('BREAD001', 0),
('TEA001', 90),
('SOAP001', 75);

1	admin	240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9	ADMIN	admin@syos.com	2025-08-28 02:36:01	1
2	cashier1	ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f	CASHIER	cashier1@syos.com	2025-08-28 02:36:01	1
3	manager1	ac9689e2272427085e35b9d3e3e8bed88cb3434828b43b86fc0596cad4c6e270	MANAGER	manager1@syos.com	2025-08-28 02:36:01	1
6	Raja	abde82eddf1de7beb7207d194c1f9db678735b971e5023da32f891579932369a	CASHIER		2025-08-28 12:41:19	1
7	mahmood	65725932f96be47c811f07ba9c721f09ef8e17863511ded08c85c3d8682a7058	MANAGER	mahmood@gmail.com	2025-08-28 12:52:48	1