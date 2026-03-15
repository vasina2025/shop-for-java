-- =====================================================
-- iShop 电商平台 - 数据库初始化脚本
--
-- 说明：
-- 1. 创建所有业务表
-- 2. 定义存储过程和函数
-- 3. 插入测试数据
--
-- 执行方式：
-- psql -U postgres -d ishop -f init.sql
-- =====================================================

-- ==================== 用户表 ====================
-- 存储平台用户信息
-- 字段：id, username, password, email, phone, nickname, address, status, created_at
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,  -- 用户名（唯一）
    password VARCHAR(255) NOT NULL,         -- 密码
    email VARCHAR(100),                     -- 邮箱
    phone VARCHAR(20),                      -- 手机号
    nickname VARCHAR(50),                   -- 昵称
    address TEXT,                           -- 地址
    status VARCHAR(20) DEFAULT 'active',    -- 状态（active正常，inactive禁用）
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP  -- 创建时间
);

-- ==================== 商品分类表 ====================
-- 存储商品分类信息，支持多级分类
-- 字段：id, name, parent_id, level, sort_order
CREATE TABLE IF NOT EXISTS categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,             -- 分类名称
    parent_id BIGINT,                       -- 父分类ID（NULL为顶级分类）
    level INTEGER DEFAULT 1,                -- 层级（1=一级，2=二级...）
    sort_order INTEGER DEFAULT 0             -- 排序顺序
);

-- ==================== 商品表 ====================
-- 存储商品基本信息
-- 字段：id, name, description, price, stock, category_id, image_url, status, sales_count, created_at
CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,             -- 商品名称
    description TEXT,                        -- 商品描述
    price DECIMAL(10, 2) NOT NULL,         -- 价格
    stock INTEGER DEFAULT 0,               -- 库存数量
    category_id BIGINT,                     -- 分类ID
    image_url VARCHAR(500),                 -- 图片URL
    status VARCHAR(20) DEFAULT 'active',    -- 状态（active在售，deleted下架）
    sales_count INTEGER DEFAULT 0,          -- 销量
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP  -- 创建时间
);

-- ==================== 购物车表 ====================
-- 存储用户购物车商品
-- 字段：id, user_id, product_id, quantity, created_at
CREATE TABLE IF NOT EXISTS cart_items (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,               -- 用户ID
    product_id BIGINT NOT NULL,            -- 商品ID
    quantity INTEGER NOT NULL DEFAULT 1,   -- 数量
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- 创建时间
    UNIQUE(user_id, product_id)            -- 同一用户对同一商品唯一
);

-- ==================== 订单表 ====================
-- 存储订单主信息
-- 字段：id, user_id, order_no, total_amount, status, address, created_at
CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,               -- 用户ID
    order_no VARCHAR(50) UNIQUE NOT NULL, -- 订单号（唯一）
    total_amount DECIMAL(10, 2) NOT NULL,  -- 订单总金额
    status VARCHAR(20) DEFAULT 'pending',   -- 状态（pending待付款，paid已付款，shipped已发货，completed已完成，cancelled已取消）
    address TEXT,                           -- 收货地址
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP  -- 创建时间
);

-- ==================== 订单明细表 ====================
-- 存储订单商品详情
-- 字段：id, order_id, product_id, product_name, price, quantity, subtotal
CREATE TABLE IF NOT EXISTS order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,              -- 订单ID
    product_id BIGINT NOT NULL,            -- 商品ID
    product_name VARCHAR(200),             -- 商品名称（下单时快照）
    price DECIMAL(10, 2) NOT NULL,         -- 购买单价
    quantity INTEGER NOT NULL,              -- 购买数量
    subtotal DECIMAL(10, 2) NOT NULL       -- 小计
);

-- =====================================================
-- 存储过程和函数定义
-- =====================================================

-- 函数: 获取商品列表（分页）
CREATE OR REPLACE FUNCTION fn_get_product_list(p_page INTEGER, p_page_size INTEGER)
RETURNS TABLE(
    id BIGINT, name VARCHAR, description TEXT, price DECIMAL,
    stock INTEGER, category_id BIGINT, image_url VARCHAR,
    status VARCHAR, sales_count INTEGER, created_at TIMESTAMP
) AS $$
BEGIN
    RETURN QUERY
    SELECT pr.id, pr.name, pr.description, pr.price, pr.stock,
           pr.category_id, pr.image_url, pr.status, pr.sales_count, pr.created_at
    FROM products pr
    WHERE pr.status = 'active'
    ORDER BY pr.created_at DESC
    LIMIT p_page_size OFFSET (p_page - 1) * p_page_size;
END;
$$ LANGUAGE plpgsql;

-- 函数: 根据ID获取商品详情
CREATE OR REPLACE FUNCTION fn_get_product_by_id(p_id BIGINT)
RETURNS TABLE(
    id BIGINT, name VARCHAR, description TEXT, price DECIMAL,
    stock INTEGER, category_id BIGINT, image_url VARCHAR, status VARCHAR, sales_count INTEGER
) AS $$
BEGIN
    RETURN QUERY SELECT p.id, p.name, p.description, p.price, p.stock,
           p.category_id, p.image_url, p.status, p.sales_count
    FROM products p WHERE p.id = p_id;
END;
$$ LANGUAGE plpgsql;

-- 函数: 搜索商品（关键词+分类）
CREATE OR REPLACE FUNCTION fn_search_products(p_keyword VARCHAR, p_category_id BIGINT, p_page_size INTEGER)
RETURNS TABLE(
    id BIGINT, name VARCHAR, description TEXT, price DECIMAL,
    stock INTEGER, category_id BIGINT, image_url VARCHAR, status VARCHAR, sales_count INTEGER
) AS $$
BEGIN
    RETURN QUERY
    SELECT p.id, p.name, p.description, p.price, p.stock,
           p.category_id, p.image_url, p.status, p.sales_count
    FROM products p
    WHERE p.status = 'active'
      AND (p_keyword IS NULL OR p_keyword = '' OR p.name ILIKE '%' || p_keyword || '%')
      AND (p_category_id IS NULL OR p.category_id = p_category_id)
    ORDER BY p.sales_count DESC
    LIMIT p_page_size;
END;
$$ LANGUAGE plpgsql;

-- 函数: 创建订单
CREATE OR REPLACE FUNCTION fn_create_order(p_user_id BIGINT, p_items VARCHAR, p_address TEXT)
RETURNS TABLE(order_id BIGINT, order_no VARCHAR, total_amount DECIMAL) AS $$
DECLARE
    v_order_id BIGINT;
    v_order_no VARCHAR(50);
    v_total DECIMAL(10, 2) := 0;
BEGIN
    -- 生成订单号：ORD + 时间戳 + 随机数
    v_order_no := 'ORD' || TO_CHAR(NOW(), 'YYYYMMDDHH24MISS') || LPAD(FLOOR(RANDOM() * 10000)::TEXT, 4, '0');
    v_total := 100.00;  -- TODO: 实际应计算商品总价

    -- 插入订单
    INSERT INTO orders (user_id, order_no, total_amount, address, status)
    VALUES (p_user_id, v_order_no, v_total, p_address, 'pending')
    RETURNING id INTO v_order_id;

    RETURN QUERY SELECT v_order_id, v_order_no, v_total;
END;
$$ LANGUAGE plpgsql;

-- 函数: 获取订单详情
CREATE OR REPLACE FUNCTION fn_get_order_by_id(p_id BIGINT)
RETURNS TABLE(id BIGINT, user_id BIGINT, order_no VARCHAR, total_amount DECIMAL, status VARCHAR, address TEXT, created_at TIMESTAMP) AS $$
BEGIN
    RETURN QUERY SELECT o.id, o.user_id, o.order_no, o.total_amount, o.status, o.address, o.created_at
    FROM orders o WHERE o.id = p_id;
END;
$$ LANGUAGE plpgsql;

-- 函数: 获取用户订单列表
CREATE OR REPLACE FUNCTION fn_get_user_orders(p_user_id BIGINT, p_status VARCHAR, p_limit INTEGER)
RETURNS TABLE(id BIGINT, user_id BIGINT, order_no VARCHAR, total_amount DECIMAL, status VARCHAR, address TEXT, created_at TIMESTAMP) AS $$
BEGIN
    RETURN QUERY
    SELECT o.id, o.user_id, o.order_no, o.total_amount, o.status, o.address, o.created_at
    FROM orders o
    WHERE o.user_id = p_user_id AND (p_status IS NULL OR o.status = p_status)
    ORDER BY o.created_at DESC
    LIMIT p_limit;
END;
$$ LANGUAGE plpgsql;

-- 函数: 用户登录验证
CREATE OR REPLACE FUNCTION fn_user_login(p_username VARCHAR, p_password VARCHAR)
RETURNS TABLE(id BIGINT, username VARCHAR, nickname VARCHAR) AS $$
BEGIN
    RETURN QUERY SELECT u.id, u.username, u.nickname FROM users u
    WHERE u.username = p_username AND u.password = p_password AND u.status = 'active';
END;
$$ LANGUAGE plpgsql;

-- 函数: 用户注册
CREATE OR REPLACE FUNCTION fn_register_user(p_username VARCHAR, p_password VARCHAR, p_email VARCHAR, p_phone VARCHAR)
RETURNS TABLE(user_id BIGINT, username VARCHAR) AS $$
DECLARE
    v_user_id BIGINT;
BEGIN
    INSERT INTO users (username, password, email, phone, nickname)
    VALUES (p_username, p_password, p_email, p_phone, p_username)
    RETURNING id INTO v_user_id;
    RETURN QUERY SELECT v_user_id, p_username;
END;
$$ LANGUAGE plpgsql;

-- 函数: 获取用户信息
CREATE OR REPLACE FUNCTION fn_get_user_by_id(p_id BIGINT)
RETURNS TABLE(id BIGINT, username VARCHAR, email VARCHAR, phone VARCHAR, nickname VARCHAR, address TEXT) AS $$
BEGIN
    RETURN QUERY SELECT u.id, u.username, u.email, u.phone, u.nickname, u.address
    FROM users u WHERE u.id = p_id;
END;
$$ LANGUAGE plpgsql;

-- 函数: 检查用户名是否存在
CREATE OR REPLACE FUNCTION fn_check_username_exists(p_username VARCHAR)
RETURNS TABLE(id BIGINT, username VARCHAR) AS $$
BEGIN
    RETURN QUERY SELECT u.id, u.username FROM users u WHERE u.username = p_username;
END;
$$ LANGUAGE plpgsql;

-- 函数: 获取用户购物车
CREATE OR REPLACE FUNCTION fn_get_user_cart(p_user_id BIGINT)
RETURNS TABLE(id BIGINT, user_id BIGINT, product_id BIGINT, product_name VARCHAR, price DECIMAL, quantity INTEGER, subtotal DECIMAL) AS $$
BEGIN
    RETURN QUERY
    SELECT c.id, c.user_id, c.product_id, p.name, p.price, c.quantity, p.price * c.quantity
    FROM cart_items c
    JOIN products p ON c.product_id = p.id
    WHERE c.user_id = p_user_id;
END;
$$ LANGUAGE plpgsql;

-- 函数: 获取用户列表（分页）
CREATE OR REPLACE FUNCTION fn_get_user_list(p_page INTEGER, p_page_size INTEGER)
RETURNS TABLE(
    id BIGINT,
    username VARCHAR,
    nickname VARCHAR,
    email VARCHAR,
    phone VARCHAR,
    address TEXT,
    status VARCHAR,
    created_at TIMESTAMP
) AS $$
BEGIN
    RETURN QUERY
    SELECT u.id, u.username, u.nickname, u.email, u.phone, u.address, u.status, u.created_at
    FROM users u
    ORDER BY u.id DESC
    LIMIT p_page_size OFFSET (p_page - 1) * p_page_size;
END;
$$ LANGUAGE plpgsql;

-- 函数: 获取分类树
CREATE OR REPLACE FUNCTION fn_get_category_tree()
RETURNS TABLE(id BIGINT, name VARCHAR, parent_id BIGINT, level INTEGER) AS $$
BEGIN
    RETURN QUERY SELECT c.id, c.name, c.parent_id, c.level FROM categories c ORDER BY c.sort_order;
END;
$$ LANGUAGE plpgsql;

-- 函数: 获取分类下的商品
CREATE OR REPLACE FUNCTION fn_get_category_products(p_category_id BIGINT, p_page INTEGER, p_page_size INTEGER)
RETURNS TABLE(id BIGINT, name VARCHAR, description TEXT, price DECIMAL, stock INTEGER, category_id BIGINT, image_url VARCHAR, status VARCHAR, sales_count INTEGER) AS $$
BEGIN
    RETURN QUERY
    SELECT p.id, p.name, p.description, p.price, p.stock, p.category_id, p.image_url, p.status, p.sales_count
    FROM products p
    WHERE p.category_id = p_category_id AND p.status = 'active'
    ORDER BY p.sales_count DESC
    LIMIT p_page_size OFFSET (p_page - 1) * p_page_size;
END;
$$ LANGUAGE plpgsql;

-- 存储过程: 添加到购物车
CREATE OR REPLACE PROCEDURE sp_add_to_cart(p_user_id BIGINT, p_product_id BIGINT, p_quantity INTEGER) AS $$
BEGIN
    INSERT INTO cart_items (user_id, product_id, quantity)
    VALUES (p_user_id, p_product_id, p_quantity)
    ON CONFLICT (user_id, product_id) DO UPDATE SET quantity = cart_items.quantity + p_quantity;
END;
$$ LANGUAGE plpgsql;

-- 存储过程: 清空购物车
CREATE OR REPLACE PROCEDURE sp_clear_cart(p_user_id BIGINT) AS $$
BEGIN
    DELETE FROM cart_items WHERE user_id = p_user_id;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- 用户导入导出函数
-- =====================================================

-- 函数: 导出所有用户
CREATE OR REPLACE FUNCTION fn_export_users()
RETURNS TABLE(
    id BIGINT,
    username VARCHAR,
    nickname VARCHAR,
    email VARCHAR,
    phone VARCHAR,
    address VARCHAR,
    status VARCHAR,
    created_at TIMESTAMP
) AS $$
BEGIN
    RETURN QUERY
    SELECT u.id, u.username, u.nickname, u.email, u.phone, u.address, u.status, u.created_at
    FROM users u
    ORDER BY u.id;
END;
$$ LANGUAGE plpgsql;

-- 函数: 导入单个用户
CREATE OR REPLACE FUNCTION fn_import_user(
    p_username VARCHAR,
    p_password VARCHAR,
    p_nickname VARCHAR,
    p_email VARCHAR,
    p_phone VARCHAR,
    p_address VARCHAR,
    p_update_existing BOOLEAN DEFAULT FALSE
) RETURNS TABLE(success BOOLEAN, message VARCHAR, user_id BIGINT) AS $$
DECLARE
    v_user_id BIGINT;
    v_exists BOOLEAN;
BEGIN
    -- 检查用户是否存在
    SELECT EXISTS(SELECT 1 FROM users WHERE username = p_username) INTO v_exists;

    IF v_exists THEN
        IF p_update_existing THEN
            -- 更新已存在用户
            UPDATE users SET
                password = COALESCE(p_password, password),
                nickname = COALESCE(p_nickname, nickname),
                email = COALESCE(p_email, email),
                phone = COALESCE(p_phone, phone),
                address = COALESCE(p_address, address)
            WHERE username = p_username
            RETURNING id INTO v_user_id;
            RETURN QUERY SELECT TRUE, '用户已更新', v_user_id;
        ELSE
            RETURN QUERY SELECT FALSE, '用户名已存在', NULL::BIGINT;
        END IF;
    ELSE
        -- 插入新用户
        INSERT INTO users (username, password, nickname, email, phone, address, status)
        VALUES (p_username, p_password, p_nickname, p_email, p_phone, p_address, 'active')
        RETURNING id INTO v_user_id;
        RETURN QUERY SELECT TRUE, '用户创建成功', v_user_id;
    END IF;
END;
$$ LANGUAGE plpgsql;

-- 函数: 批量导入用户
CREATE OR REPLACE FUNCTION fn_batch_import_users(
    p_users JSONB,
    p_update_existing BOOLEAN DEFAULT FALSE
) RETURNS TABLE(success_count INTEGER, error_count INTEGER, errors JSONB) AS $$
DECLARE
    v_user JSONB;
    v_success_count INTEGER := 0;
    v_error_count INTEGER := 0;
    v_errors JSONB := '[]'::JSONB;
    v_result RECORD;
BEGIN
    FOR v_user IN SELECT * FROM jsonb_array_elements(p_users)
    LOOP
        BEGIN
            FOR v_result IN
                SELECT * FROM fn_import_user(
                    v_user->>'username',
                    v_user->>'password',
                    v_user->>'nickname',
                    v_user->>'email',
                    v_user->>'phone',
                    v_user->>'address',
                    p_update_existing
                )
            LOOP
                IF v_result.success THEN
                    v_success_count := v_success_count + 1;
                ELSE
                    v_error_count := v_error_count + 1;
                    v_errors := v_errors || jsonb_build_object('username', v_user->>'username', 'message', v_result.message);
                END IF;
            END LOOP;
        EXCEPTION WHEN OTHERS THEN
            v_error_count := v_error_count + 1;
            v_errors := v_errors || jsonb_build_object('username', v_user->>'username', 'message', SQLERRM);
        END;
    END LOOP;

    RETURN QUERY SELECT v_success_count, v_error_count, v_errors;
END;
$$ LANGUAGE plpgsql;

-- 函数: 获取用户统计信息
CREATE OR REPLACE FUNCTION fn_get_user_stats()
RETURNS TABLE(total_count BIGINT, active_count BIGINT, inactive_count BIGINT) AS $$
BEGIN
    RETURN QUERY
    SELECT
        COUNT(*)::BIGINT AS total_count,
        COUNT(*) FILTER (WHERE status = 'active')::BIGINT AS active_count,
        COUNT(*) FILTER (WHERE status != 'active')::BIGINT AS inactive_count
    FROM users;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- 测试数据初始化
-- =====================================================

-- 初始化用户
INSERT INTO users (username, password, nickname) VALUES
('admin', 'admin123', '管理员'),
('test', 'test123', '测试用户')
ON CONFLICT (username) DO NOTHING;

-- 初始化分类（一级+二级）
INSERT INTO categories (name, parent_id, level, sort_order) VALUES
('电子产品', NULL, 1, 1),
('服装', NULL, 1, 2),
('食品', NULL, 1, 3),
('手机', 1, 2, 1),
('电脑', 1, 2, 2),
('上衣', 2, 2, 1),
('裤子', 2, 2, 2)
ON CONFLICT DO NOTHING;

-- 初始化商品
INSERT INTO products (name, description, price, stock, category_id, status, sales_count) VALUES
('iPhone 15', '苹果手机', 8999.00, 100, 4, 'active', 500),
('MacBook Pro', '苹果笔记本', 12999.00, 50, 5, 'active', 300),
('纯棉T恤', '舒适纯棉', 99.00, 500, 6, 'active', 1000),
('牛仔裤', '经典版型', 199.00, 300, 7, 'active', 800),
('小米手机', '性价比高', 1999.00, 200, 4, 'active', 1000),
('联想电脑', '办公首选', 4999.00, 80, 5, 'active', 400)
ON CONFLICT DO NOTHING;
