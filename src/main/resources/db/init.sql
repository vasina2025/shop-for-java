-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 商品分类表
CREATE TABLE IF NOT EXISTS categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    parent_id BIGINT,
    sort_order INTEGER DEFAULT 0
);

-- 商品表
CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    stock INTEGER DEFAULT 0,
    category_id BIGINT,
    image_url VARCHAR(500),
    status VARCHAR(20) DEFAULT 'active',
    sales_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 购物车表
CREATE TABLE IF NOT EXISTS cart_items (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, product_id)
);

-- 订单表
CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    order_no VARCHAR(50) UNIQUE NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) DEFAULT 'pending',
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 初始化测试数据
INSERT INTO users (username, password, nickname) VALUES
('admin', 'admin123', '管理员'),
('test', 'test123', '测试用户')
ON CONFLICT (username) DO NOTHING;

INSERT INTO categories (name, parent_id, sort_order) VALUES
('电子产品', NULL, 1),
('服装', NULL, 2),
('食品', NULL, 3),
('手机', 1, 1),
('电脑', 1, 2),
('上衣', 2, 1),
('裤子', 2, 2)
ON CONFLICT DO NOTHING;

INSERT INTO products (name, description, price, stock, category_id, status, sales_count) VALUES
('iPhone 15', '苹果手机', 8999.00, 100, 4, 'active', 500),
('MacBook Pro', '苹果笔记本', 12999.00, 50, 5, 'active', 300),
('纯棉T恤', '舒适纯棉', 99.00, 500, 7, 'active', 1000),
('牛仔裤', '经典版型', 199.00, 300, 8, 'active', 800),
('小米手机', '性价比高', 1999.00, 200, 4, 'active', 1000),
('联想电脑', '办公首选', 4999.00, 80, 5, 'active', 400)
ON CONFLICT DO NOTHING;
