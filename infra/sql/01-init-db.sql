CREATE DATABASE IF NOT EXISTS takeout_user DEFAULT CHARACTER SET utf8mb4;
CREATE DATABASE IF NOT EXISTS takeout_product DEFAULT CHARACTER SET utf8mb4;
CREATE DATABASE IF NOT EXISTS takeout_order DEFAULT CHARACTER SET utf8mb4;

USE takeout_user;
DROP TABLE IF EXISTS t_user;
CREATE TABLE t_user (
    id BIGINT PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(128) NOT NULL,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    taste_preference VARCHAR(50) DEFAULT NULL,
    avatar VARCHAR(255) DEFAULT NULL,
    bio VARCHAR(255) DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
INSERT INTO t_user (id, username, password, name, phone, taste_preference, avatar, bio)
VALUES (1, 'demo', '123456', '张三', '13800000001', '微辣', 'https://picsum.photos/120/120?random=1', '喜欢微辣和米饭')
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    phone = VALUES(phone),
    taste_preference = VALUES(taste_preference),
    avatar = VALUES(avatar),
    bio = VALUES(bio);

USE takeout_product;
DROP TABLE IF EXISTS t_product;
CREATE TABLE t_product (
    id BIGINT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    stock INT NOT NULL,
    spicy_level VARCHAR(20) NOT NULL DEFAULT '不辣',
    sales INT NOT NULL DEFAULT 0,
    description VARCHAR(255) DEFAULT NULL
);
INSERT INTO t_product (id, name, price, stock, spicy_level, sales, description)
VALUES
    (1, '宫保鸡丁饭', 25.00, 999, '中辣', 328, '经典川味搭配花生，酱香浓郁'),
    (2, '番茄牛腩饭', 32.00, 300, '不辣', 196, '慢炖牛腩软烂入味，汤汁拌饭'),
    (3, '香辣鸡腿堡套餐', 28.00, 450, '重辣', 415, '鸡腿堡+薯条+可乐，口感丰富')
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    price = VALUES(price),
    stock = VALUES(stock),
    spicy_level = VALUES(spicy_level),
    sales = VALUES(sales),
    description = VALUES(description);

USE takeout_order;
DROP TABLE IF EXISTS t_order;
CREATE TABLE t_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    count INT NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(30) NOT NULL,
    order_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    remark VARCHAR(255) DEFAULT NULL
);
