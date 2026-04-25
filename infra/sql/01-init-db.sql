CREATE DATABASE IF NOT EXISTS takeout_user DEFAULT CHARACTER SET utf8mb4;
CREATE DATABASE IF NOT EXISTS takeout_product DEFAULT CHARACTER SET utf8mb4;
CREATE DATABASE IF NOT EXISTS takeout_order DEFAULT CHARACTER SET utf8mb4;

USE takeout_user;
CREATE TABLE IF NOT EXISTS t_user (
    id BIGINT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(30) NOT NULL
);
INSERT INTO t_user (id, name, phone) VALUES (1, '张三', '13800000001');

USE takeout_product;
CREATE TABLE IF NOT EXISTS t_product (
    id BIGINT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    stock INT NOT NULL
);
INSERT INTO t_product (id, name, price, stock) VALUES (1, '宫保鸡丁饭', 25.00, 999);

USE takeout_order;
CREATE TABLE IF NOT EXISTS t_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    count INT NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(30) NOT NULL
);
