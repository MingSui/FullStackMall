-- 数据库初始化脚本
-- 设置正确的字符集以避免中文乱码

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS fullstackmall 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE fullstackmall;

-- 设置会话字符集
SET NAMES utf8mb4;
SET character_set_client = utf8mb4;
SET character_set_connection = utf8mb4;
SET character_set_database = utf8mb4;
SET character_set_results = utf8mb4;
SET character_set_server = utf8mb4;

-- 显示字符集配置（用于验证）
-- SHOW VARIABLES LIKE 'character_set_%';
-- SHOW VARIABLES LIKE 'collation_%';

-- 初始化 FullStackMall 数据库

-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 创建商品分类表
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建商品表
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    category_id BIGINT,
    image_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    INDEX idx_category (category_id),
    INDEX idx_price (price),
    INDEX idx_name (name)
);

-- 创建购物车表
CREATE TABLE IF NOT EXISTS cart_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_product (user_id, product_id),
    INDEX idx_user (user_id)
);

-- 创建订单表
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    shipping_address TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user (user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
);

-- 创建订单明细表
CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    INDEX idx_order (order_id)
);

-- 插入初始数据

-- 插入管理员用户 (密码: admin123)
INSERT INTO users (username, email, password, role) VALUES 
('admin', 'admin@fullstackmall.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM7lE6Py0bMV5n0B/I1K', 'ADMIN');

-- 插入商品分类
INSERT INTO categories (name, description) VALUES 
('电子产品', '各类电子设备和配件'),
('服装服饰', '男女服装、鞋帽等'),
('家居用品', '家具、装饰品、生活用品'),
('图书音像', '图书、音乐、影视等'),
('运动户外', '运动器材、户外用品');

-- 插入示例商品
INSERT INTO products (name, description, price, stock_quantity, category_id, image_url) VALUES 
('iPhone 15 Pro', '最新款苹果手机，配备A17 Pro芯片', 7999.00, 50, 1, 'https://example.com/iphone15pro.jpg'),
('MacBook Pro 14', '专业级笔记本电脑，M3芯片', 14999.00, 30, 1, 'https://example.com/macbookpro14.jpg'),
('Nike Air Force 1', '经典款运动鞋，舒适透气', 899.00, 100, 2, 'https://example.com/airforce1.jpg'),
('Adidas 三叶草卫衣', '时尚休闲卫衣，多色可选', 599.00, 80, 2, 'https://example.com/adidas-hoodie.jpg'),
('宜家书桌', '简约现代书桌，适合办公学习', 499.00, 60, 3, 'https://example.com/ikea-desk.jpg'),
('《深入理解Java虚拟机》', '经典Java技术书籍', 89.00, 200, 4, 'https://example.com/jvm-book.jpg'),
('瑜伽垫', '环保材质瑜伽垫，防滑耐用', 159.00, 150, 5, 'https://example.com/yoga-mat.jpg'),
('无线蓝牙耳机', '高品质音质，长续航', 299.00, 120, 1, 'https://example.com/bluetooth-earphones.jpg');