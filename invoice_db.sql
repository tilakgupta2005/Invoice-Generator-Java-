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

select *FROM items;
select *FROM transaction_history;