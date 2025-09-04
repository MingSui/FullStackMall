package com.fullstackmall.config;

import com.fullstackmall.entity.Product;
import com.fullstackmall.entity.User;
import com.fullstackmall.repository.ProductRepository;
import com.fullstackmall.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Statement;

/**
 * 数据初始化器
 * 在应用启动时添加测试数据
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(String... args) throws Exception {
        // 首先修复数据库字符集
        // fixDatabaseCharset(); // 暂时注释，避免Hibernate启动问题

        // 初始化用户数据
        initializeUsers();

        // 初始化分类数据
        initializeCategories();

        // 初始化商品数据
        initializeProducts();
    }

    /**
     * 修复数据库字符集配置
     */
    private void fixDatabaseCharset() {
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {

            // 检查当前字符集配置
            var rs = statement.executeQuery("SHOW VARIABLES LIKE 'character_set_database'");
            String currentCharset = "";
            if (rs.next()) {
                currentCharset = rs.getString("Value");
            }
            rs.close();

            // 如果字符集已经是utf8mb4，则跳过修复
            if ("utf8mb4".equals(currentCharset)) {
                System.out.println("数据库字符集已正确配置为utf8mb4，跳过修复");
                return;
            }

            System.out.println("当前数据库字符集: " + currentCharset + "，开始修复为utf8mb4...");

            // 设置会话字符集
            statement.execute("SET NAMES utf8mb4");
            statement.execute("SET character_set_client = utf8mb4");
            statement.execute("SET character_set_connection = utf8mb4");
            statement.execute("SET character_set_results = utf8mb4");

            // 修改数据库字符集
            statement.execute("ALTER DATABASE fullstackmall CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");

            // 修改表字符集
            statement.execute("ALTER TABLE products CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            statement.execute("ALTER TABLE users CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");

            System.out.println("数据库字符集修复完成");

        } catch (Exception e) {
            System.err.println("修复数据库字符集失败: " + e.getMessage());
            // 不抛出异常，让程序继续运行
        }
    }

    private void initializeUsers() {
        // 检查是否已有管理员用户
        if (userRepository.count() == 0) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@fullstackmall.com");
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setRole(User.Role.ADMIN);
            userRepository.save(admin);

            User user = new User();
            user.setUsername("testuser");
            user.setEmail("user@fullstackmall.com");
            user.setPassword(passwordEncoder.encode("123456"));
            user.setRole(User.Role.USER);
            userRepository.save(user);

            System.out.println("初始化用户数据完成");
            System.out.println("管理员账号: admin@fullstackmall.com / 123456");
            System.out.println("测试用户: user@fullstackmall.com / 123456");
        }
    }

    private void initializeProducts() {
        // 如果没有商品数据，则初始化商品数据
        if (productRepository.count() == 0) {
            // 初始化商品数据
            createInitialProducts();
            System.out.println("初始化商品数据完成，共创建 " + productRepository.count() + " 个商品");
        } else {
            System.out.println("商品数据已存在，跳过初始化");
        }
    }

    private void initializeCategories() {
        // 初始化商品分类数据
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {

            // 检查categories表是否存在且有数据
            boolean hasCategories = false;
            try {
                var rs = statement.executeQuery("SELECT COUNT(*) FROM categories");
                if (rs.next() && rs.getInt(1) > 0) {
                    hasCategories = true;
                }
                rs.close();
            } catch (Exception e) {
                // categories表可能不存在，忽略
            }

            if (!hasCategories) {
                try {
                    // 插入分类数据
                    statement.execute(
                            "INSERT INTO categories (name, description) VALUES " +
                                    "('电子产品', '各类电子设备和配件'), " +
                                    "('服装', '男女服装、鞋帽等'), " +
                                    "('图书', '图书、音乐、影视等'), " +
                                    "('家居用品', '家具、装饰品、生活用品')");
                    System.out.println("初始化商品分类数据完成");
                } catch (Exception e) {
                    System.err.println("初始化分类数据失败: " + e.getMessage());
                }
            } else {
                System.out.println("分类数据已存在，跳过初始化");
            }

        } catch (Exception e) {
            System.err.println("初始化分类数据时出错: " + e.getMessage());
        }
    }

    private void createInitialProducts() {
        // 电子产品分类
        createProduct("iPhone 15 Pro", "苹果最新旗舰手机，搭载A17 Pro芯片", new BigDecimal("7999"), 50, "电子产品",
                "/images/products/iphone15pro.svg");
        createProduct("MacBook Air M3", "轻薄便携的笔记本电脑，适合办公和学习", new BigDecimal("8999"), 30, "电子产品",
                "/images/products/macbook.svg");
        createProduct("AirPods Pro", "主动降噪无线耳机，音质卓越", new BigDecimal("1899"), 100, "电子产品",
                "/images/products/airpods.svg");
        createProduct("iPad Air", "多功能平板电脑，支持Apple Pencil", new BigDecimal("4399"), 40, "电子产品",
                "/images/products/ipad.svg");

        // 服装分类
        createProduct("经典白衬衫", "100%纯棉，商务休闲两相宜", new BigDecimal("299"), 200, "服装", "/images/products/shirt.svg");
        createProduct("牛仔裤", "经典蓝色牛仔裤，舒适耐穿", new BigDecimal("399"), 150, "服装", "/images/products/jeans.svg");
        createProduct("运动鞋", "透气舒适的跑步鞋，适合日常运动", new BigDecimal("699"), 80, "服装", "/images/products/shoes.svg");
        createProduct("针织毛衣", "柔软保暖的羊毛毛衣，多色可选", new BigDecimal("599"), 120, "服装",
                "/images/products/sweater.svg");

        // 图书分类
        createProduct("Java编程思想", "Java编程经典教材，程序员必读", new BigDecimal("89"), 60, "图书",
                "/images/products/java-book.svg");
        createProduct("Spring Boot实战", "Spring Boot框架实战指南", new BigDecimal("79"), 45, "图书",
                "/images/products/spring-book.svg");
        createProduct("算法导论", "计算机科学经典教材", new BigDecimal("128"), 35, "图书",
                "/images/products/algorithm-book.svg");
        createProduct("设计模式", "软件设计模式详解", new BigDecimal("98"), 50, "图书",
                "/images/products/design-pattern-book.svg");

        // 家居用品分类
        createProduct("北欧风台灯", "简约设计台灯，护眼LED光源", new BigDecimal("299"), 90, "家居用品", "/images/products/lamp.svg");
        createProduct("懒人沙发", "舒适的单人沙发，适合小户型", new BigDecimal("899"), 25, "家居用品", "/images/products/sofa.svg");
        createProduct("收纳盒套装", "多功能收纳盒，整理收纳好帮手", new BigDecimal("159"), 200, "家居用品",
                "/images/products/storage.svg");
        createProduct("香薰蜡烛", "天然大豆蜡香薰蜡烛，多种香型", new BigDecimal("68"), 300, "家居用品",
                "/images/products/candle.svg");
    }

    private void createProduct(String name, String description, BigDecimal price, int stock, String category,
            String imageUrl) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setStock(stock);
        product.setCategory(category);
        product.setImageUrl(imageUrl);
        productRepository.save(product);
    }
}