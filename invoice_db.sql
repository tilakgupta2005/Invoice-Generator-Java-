CREATE DATABASE IF NOT EXISTS invoice_db;
USE invoice_db;

-- Items table
CREATE TABLE IF NOT EXISTS items (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  cost_price DECIMAL(10,2) NOT NULL,
  selling_price DECIMAL(10,2) NOT NULL
);

-- Transaction history table
CREATE TABLE IF NOT EXISTS transaction_history (
  uid INT AUTO_INCREMENT PRIMARY KEY,
  date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  customer_name VARCHAR(255),
  phone VARCHAR(20),
  payment_no VARCHAR(100),
  subtotal DECIMAL(10,2),
  discount_amt DECIMAL(10,2),
  tax_amt DECIMAL(10,2),
  profit DECIMAL(10,2),
  grand_total DECIMAL(10,2)
);

INSERT INTO items (name, cost_price, selling_price) VALUES
('Veg Sandwich', 20.00, 40.00),
('Cold Coffee', 25.00, 60.00),
('Paneer Roll', 30.00, 70.00),
('Samosa', 5.00, 15.00),
('Choco Lava Cake', 18.00, 45.00),
('Aloo Tikki Burger', 22.00, 50.00),
('Masala Dosa', 30.00, 65.00),
('Veg Biryani', 40.00, 90.00),
('Fruit Juice', 15.00, 35.00),
('Fried Rice', 28.00, 60.00);

select *FROM items;
select *FROM transaction_history;
