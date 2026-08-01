-- ============================================
-- 校园图书管理系统 数据库初始化脚本
-- 数据库名称: library_db
-- 字符编码: utf8mb4
-- 版本: 1.0.0
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS library_db
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;
USE library_db;
-- ============================================
-- 1. 权限模块数据表
-- ============================================

-- 1.1 角色表
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id          BIGINT          NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    role_name   VARCHAR(30)     NOT NULL COMMENT '角色名称',
    role_code   VARCHAR(30)     NOT NULL COMMENT '角色标识(admin/library/user)',
    status      TINYINT         DEFAULT 1 COMMENT '角色状态(0禁用,1正常)',
    remark      VARCHAR(255)    DEFAULT NULL COMMENT '角色描述',
    is_delete   TINYINT         DEFAULT 0 COMMENT '逻辑删除标记(0未删除,1已删除)',
    create_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME        DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_name (role_name),
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 1.2 用户表
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id           BIGINT          NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    user_account VARCHAR(50)     NOT NULL COMMENT '登录账号(学号/工号)',
    user_name    VARCHAR(30)     NOT NULL COMMENT '用户姓名',
    password     VARCHAR(100)    NOT NULL COMMENT '加密密码(MD5)',
    role_id      BIGINT          NOT NULL COMMENT '关联角色ID',
    user_type    TINYINT         NOT NULL COMMENT '用户类型(1学生,2教师,3管理员)',
    phone        VARCHAR(20)     DEFAULT NULL COMMENT '联系电话',
    email        VARCHAR(50)     DEFAULT NULL COMMENT '邮箱',
    avatar       VARCHAR(255)    DEFAULT NULL COMMENT '头像地址',
    borrow_num   INT             DEFAULT 0 COMMENT '当前已借图书数量',
    max_borrow   INT             NOT NULL DEFAULT 10 COMMENT '最大可借数量',
    status       TINYINT         DEFAULT 1 COMMENT '账号状态(0禁用,1正常)',
    is_delete    TINYINT         DEFAULT 0 COMMENT '逻辑删除标记',
    create_time  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME        DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_account (user_account)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 1.3 权限表
DROP TABLE IF EXISTS sys_permission;
CREATE TABLE sys_permission (
    id          BIGINT          NOT NULL AUTO_INCREMENT COMMENT '权限ID',
    perm_name   VARCHAR(50)     NOT NULL COMMENT '权限名称',
    perm_code   VARCHAR(100)    NOT NULL COMMENT '权限标识(如book:add、borrow:list)',
    perm_type   TINYINT         NOT NULL COMMENT '权限类型(1菜单,2接口)',
    parent_id   BIGINT          DEFAULT 0 COMMENT '父级权限ID',
    is_delete   TINYINT         DEFAULT 0 COMMENT '逻辑删除标记',
    create_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME        DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_perm_code (perm_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 1.4 角色权限关联表
DROP TABLE IF EXISTS sys_role_perm;
CREATE TABLE sys_role_perm (
    id          BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID',
    role_id     BIGINT NOT NULL COMMENT '角色ID',
    perm_id     BIGINT NOT NULL COMMENT '权限ID',
    is_delete   TINYINT         DEFAULT 0 COMMENT '逻辑删除标记',
    create_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME        DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_role_id (role_id),
    KEY idx_perm_id (perm_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- ============================================
-- 2. 图书基础数据表
-- ============================================

-- 2.1 图书分类表
DROP TABLE IF EXISTS book_category;
CREATE TABLE book_category (
    id            BIGINT          NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    category_name VARCHAR(50)     NOT NULL COMMENT '分类名称',
    parent_id     BIGINT          DEFAULT 0 COMMENT '父分类ID(0为一级分类)',
    sort          INT             DEFAULT 0 COMMENT '排序权重',
    status        TINYINT         DEFAULT 1 COMMENT '状态(0禁用,1正常)',
    is_delete     TINYINT         DEFAULT 0 COMMENT '逻辑删除标记',
    create_time   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME        DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书分类表';

-- 2.2 图书信息表
DROP TABLE IF EXISTS book_info;
CREATE TABLE book_info (
    id           BIGINT          NOT NULL AUTO_INCREMENT COMMENT '图书ID',
    book_no      VARCHAR(50)     NOT NULL COMMENT '图书编号',
    isbn         VARCHAR(50)     NOT NULL COMMENT '图书ISBN编号',
    book_name    VARCHAR(100)    NOT NULL COMMENT '图书名称',
    author       VARCHAR(50)     NOT NULL COMMENT '作者',
    publisher    VARCHAR(50)     NOT NULL COMMENT '出版社',
    publish_time DATE            DEFAULT NULL COMMENT '出版时间',
    category_id  BIGINT          NOT NULL COMMENT '关联分类ID',
    price        DECIMAL(10,2)   DEFAULT NULL COMMENT '图书价格',
    total_num    INT             NOT NULL DEFAULT 0 COMMENT '图书总数量',
    remain_num   INT             NOT NULL DEFAULT 0 COMMENT '剩余可借库存',
    location     VARCHAR(50)     DEFAULT NULL COMMENT '馆藏位置',
    cover        VARCHAR(255)    DEFAULT NULL COMMENT '封面图片地址',
    book_desc    TEXT            DEFAULT NULL COMMENT '图书简介',
    status       TINYINT         DEFAULT 1 COMMENT '状态(0下架,1正常)',
    is_delete    TINYINT         DEFAULT 0 COMMENT '逻辑删除标记',
    create_time  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME        DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_book_no (book_no),
    UNIQUE KEY uk_isbn (isbn),
    KEY idx_book_name (book_name),
    KEY idx_author (author),
    KEY idx_category_id (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书信息表';

-- ============================================
-- 3. 借阅业务数据表
-- ============================================

-- 3.1 图书借阅记录表
DROP TABLE IF EXISTS book_borrow;
CREATE TABLE book_borrow (
    id            BIGINT      NOT NULL AUTO_INCREMENT COMMENT '借阅订单ID',
    user_id       BIGINT      NOT NULL COMMENT '借阅用户ID',
    book_id       BIGINT      NOT NULL COMMENT '图书ID',
    borrow_time   DATETIME    NOT NULL COMMENT '借阅时间',
    expire_time   DATETIME    NOT NULL COMMENT '到期时间',
    return_time   DATETIME    DEFAULT NULL COMMENT '归还时间(为空表示未归还)',
    renew_count   INT         DEFAULT 0 COMMENT '续借次数',
    borrow_status TINYINT     NOT NULL COMMENT '订单状态(1借阅中,2已归还,3已逾期)',
    is_delete     TINYINT     DEFAULT 0 COMMENT '逻辑删除标记',
    create_time   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME    DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_book_id (book_id),
    KEY idx_status (borrow_status),
    KEY idx_expire_time (expire_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书借阅记录表';

-- 3.2 逾期记录表
DROP TABLE IF EXISTS book_overdue;
CREATE TABLE book_overdue (
    id           BIGINT          NOT NULL AUTO_INCREMENT COMMENT '逾期记录ID',
    borrow_id    BIGINT          NOT NULL COMMENT '关联借阅订单ID',
    user_id      BIGINT          NOT NULL COMMENT '用户ID',
    book_id      BIGINT          NOT NULL COMMENT '图书ID',
    overdue_days INT             DEFAULT 0 COMMENT '逾期天数',
    fine_money   DECIMAL(10,2)   DEFAULT 0.00 COMMENT '逾期罚款金额',
    pay_status   TINYINT         DEFAULT 0 COMMENT '缴费状态(0未缴费,1已缴费)',
    pay_time     DATETIME        DEFAULT NULL COMMENT '缴费时间',
    is_delete    TINYINT         DEFAULT 0 COMMENT '逻辑删除标记',
    create_time  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME        DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_borrow_id (borrow_id),
    KEY idx_pay_status (pay_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='逾期记录表';

-- 3.3 图书预约记录表
DROP TABLE IF EXISTS book_reserve;
CREATE TABLE book_reserve (
    id             BIGINT      NOT NULL AUTO_INCREMENT COMMENT '预约ID',
    user_id        BIGINT      NOT NULL COMMENT '预约用户ID',
    book_id        BIGINT      NOT NULL COMMENT '预约图书ID',
    reserve_time   DATETIME    NOT NULL COMMENT '预约时间',
    expire_time    DATETIME    NOT NULL COMMENT '预约失效时间',
    reserve_status TINYINT     NOT NULL COMMENT '状态(1待生效,2已完成,3已失效)',
    is_delete      TINYINT     DEFAULT 0 COMMENT '逻辑删除标记',
    create_time    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time    DATETIME    DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_book_id (book_id),
    KEY idx_status (reserve_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书预约记录表';

-- ============================================
-- 4. 库存与损耗数据表
-- ============================================

-- 4.1 图书盘点记录表
DROP TABLE IF EXISTS book_inventory;
CREATE TABLE book_inventory (
    id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '盘点ID',
    book_id        BIGINT       NOT NULL COMMENT '图书ID',
    system_num     INT          NOT NULL COMMENT '系统库存数量',
    actual_num     INT          NOT NULL COMMENT '实际盘点数量',
    diff_num       INT          NOT NULL COMMENT '差异数量(正数盘盈,负数盘亏)',
    inventory_user BIGINT       NOT NULL COMMENT '盘点人ID',
    inventory_time DATETIME     NOT NULL COMMENT '盘点时间',
    remark         VARCHAR(255) DEFAULT NULL COMMENT '盘点备注',
    is_delete      TINYINT      DEFAULT 0 COMMENT '逻辑删除标记',
    create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time    DATETIME     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_book_id (book_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书盘点记录表';

-- 4.2 图书损耗记录表
DROP TABLE IF EXISTS book_loss;
CREATE TABLE book_loss (
    id          BIGINT          NOT NULL AUTO_INCREMENT COMMENT '损耗记录ID',
    book_id     BIGINT          NOT NULL COMMENT '图书ID',
    loss_num    INT             NOT NULL COMMENT '损耗数量',
    loss_type   TINYINT         NOT NULL COMMENT '损耗类型(1破损,2丢失,3报废)',
    loss_reason VARCHAR(255)    DEFAULT NULL COMMENT '损耗原因',
    record_user BIGINT          NOT NULL COMMENT '登记人ID',
    record_time DATETIME        NOT NULL COMMENT '登记时间',
    is_delete   TINYINT         DEFAULT 0 COMMENT '逻辑删除标记',
    create_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME        DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_book_id (book_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书损耗记录表';

-- ============================================
-- 5. 系统通用数据表
-- ============================================

-- 5.1 系统配置表
DROP TABLE IF EXISTS sys_config;
CREATE TABLE sys_config (
    id           BIGINT          NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    config_key   VARCHAR(50)     NOT NULL COMMENT '配置键名',
    config_value VARCHAR(255)    NOT NULL COMMENT '配置值',
    config_name  VARCHAR(50)     NOT NULL COMMENT '配置名称',
    is_delete     TINYINT         DEFAULT 0 COMMENT '逻辑删除标记',
    remark       VARCHAR(255)    DEFAULT NULL COMMENT '配置描述',
    create_time  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME        DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 5.2 系统日志表
DROP TABLE IF EXISTS sys_log;
CREATE TABLE sys_log (
    id             BIGINT          NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    user_id        BIGINT          DEFAULT NULL COMMENT '操作人ID(系统异常为空)',
    log_type       TINYINT         NOT NULL COMMENT '日志类型(1操作日志,2业务日志,3异常日志)',
    operation      VARCHAR(255)    DEFAULT NULL COMMENT '操作描述',
    request_url    VARCHAR(255)    DEFAULT NULL COMMENT '请求接口地址',
    request_method VARCHAR(20)     DEFAULT NULL COMMENT '请求方式',
    ip_addr        VARCHAR(50)     DEFAULT NULL COMMENT '操作IP地址',
    error_msg      TEXT            DEFAULT NULL COMMENT '异常信息(异常日志专用)',
    is_delete      TINYINT         DEFAULT 0 COMMENT '逻辑删除标记',
    create_time    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '日志生成时间',
    update_time    DATETIME        DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_log_type (log_type),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统日志表';

-- ============================================
-- 初始数据
-- ============================================

-- 初始化角色
INSERT INTO sys_role (role_name, role_code, remark) VALUES
('超级管理员', 'admin', '系统最高权限，管理所有功能'),
('图书管理员', 'library', '图书管理、借阅审核、逾期管理权限'),
('普通用户', 'user', '图书查询、借阅、归还、续借权限');

-- 初始化权限
INSERT INTO sys_permission (perm_name, perm_code, perm_type, parent_id) VALUES
('用户管理', 'user:manage', 1, 0),
('新增用户', 'user:add', 2, 1),
('修改用户', 'user:update', 2, 1),
('删除用户', 'user:delete', 2, 1),
('图书管理', 'book:manage', 1, 0),
('新增图书', 'book:add', 2, 5),
('修改图书', 'book:update', 2, 5),
('删除图书', 'book:delete', 2, 5),
('借阅管理', 'borrow:manage', 1, 0),
('借阅列表', 'borrow:list', 2, 9),
('系统管理', 'sys:manage', 1, 0),
('系统配置', 'sys:config', 2, 11),
('日志管理', 'sys:log', 2, 11);

-- 初始化角色权限关联（管理员拥有所有权限）
INSERT INTO sys_role_perm (role_id, perm_id)
SELECT 1, id FROM sys_permission;

-- 图书管理员权限
INSERT INTO sys_role_perm (role_id, perm_id)
SELECT 2, id FROM sys_permission WHERE perm_code IN
('user:manage', 'user:add', 'user:update',
 'book:manage', 'book:add', 'book:update',
 'borrow:manage', 'borrow:list', 'sys:log');

-- 普通用户无后台权限

-- 初始化管理员账号（密码明文: admin123）
INSERT INTO sys_user (user_account, user_name, password, role_id, user_type, max_borrow, borrow_num, status) VALUES
('admin', '系统管理员', 'admin123', 1, 3, 0, 0, 1);

-- 初始化测试图书管理员
INSERT INTO sys_user (user_account, user_name, password, role_id, user_type, max_borrow, borrow_num, status) VALUES
('lib001', '张管理', 'admin123', 2, 3, 0, 0, 1);

-- 初始化测试学生用户
INSERT INTO sys_user (user_account, user_name, password, role_id, user_type, max_borrow, borrow_num, status) VALUES
('2024001', '李明', 'admin123', 3, 1, 10, 0, 1),
('2024002', '王红', 'admin123', 3, 1, 10, 0, 1);

-- 初始化测试教师用户
INSERT INTO sys_user (user_account, user_name, password, role_id, user_type, max_borrow, borrow_num, status) VALUES
('T2024001', '王教授', 'admin123', 3, 2, 20, 0, 1);

-- 初始化图书分类
INSERT INTO book_category (category_name, parent_id, sort) VALUES
('文学', 0, 1),
('计算机', 0, 2),
('自然科学', 0, 3),
('社会科学', 0, 4),
('哲学', 0, 5),
('艺术', 0, 6),
('历史', 0, 7),
('外语', 0, 8),
('当代文学', 1, 1),
('古典文学', 1, 2),
('外国文学', 1, 3),
('编程语言', 2, 1),
('数据库', 2, 2),
('人工智能', 2, 3),
('网络技术', 2, 4);

-- 初始化示例图书
INSERT INTO book_info (book_no, isbn, book_name, author, publisher, category_id, price, total_num, remain_num, location, book_desc) VALUES
('B001', '978-7-111-11111-1', 'Java编程思想', 'Bruce Eckel', '机械工业出版社', 12, 99.00, 10, 10, 'A区-3排-1号', 'Java编程经典入门书籍'),
('B002', '978-7-111-22222-2', 'Spring Boot实战', 'Craig Walls', '人民邮电出版社', 12, 79.00, 8, 8, 'A区-3排-2号', 'Spring Boot快速开发实战指南'),
('B003', '978-7-111-33333-3', '活着', '余华', '作家出版社', 9, 29.00, 15, 15, 'B区-1排-5号', '讲述了一个人一生的故事'),
('B004', '978-7-111-44444-4', '三体', '刘慈欣', '重庆出版社', 1, 89.00, 20, 20, 'B区-2排-3号', '中国科幻文学的里程碑之作'),
('B005', '978-7-111-55555-5', 'MySQL从入门到精通', '明日科技', '清华大学出版社', 13, 69.00, 5, 5, 'A区-4排-1号', 'MySQL数据库学习经典教程'),
('B006', '978-7-111-66666-6', '百年孤独', '马尔克斯', '南海出版公司', 11, 39.50, 12, 12, 'B区-1排-10号', '拉丁美洲魔幻现实主义文学代表作'),
('B007', '978-7-111-77777-7', 'Python深度学习', 'François Chollet', '人民邮电出版社', 14, 109.00, 6, 6, 'A区-5排-2号', '深度学习入门与实践'),
('B008', '978-7-111-88888-8', '平凡的世界', '路遥', '北京十月文艺出版社', 9, 36.00, 18, 18, 'B区-2排-1号', '中国当代城乡社会生活的全景式画卷');

-- 初始化系统配置
INSERT INTO sys_config (config_key, config_value, config_name, remark) VALUES
('student_max_borrow', '10', '学生最大借阅数量', '学生可同时借阅的最大图书数量'),
('teacher_max_borrow', '20', '教师最大借阅数量', '教师可同时借阅的最大图书数量'),
('default_borrow_days', '30', '默认借阅时长(天)', '图书默认借阅天数'),
('max_renew_count', '1', '最大续借次数', '单本图书最大续借次数'),
('renew_days', '30', '续借时长(天)', '续借一次延长的天数'),
('overdue_fine_per_day', '0.1', '逾期每日罚款金额(元)', '逾期每天每本罚款金额'),
('reserve_expire_days', '7', '预约有效期(天)', '图书预约自动失效天数'),
('login_expire_seconds', '86400', '登录有效期(秒)', 'Token有效时间'),
('site_name', '校园图书管理系统', '网站名称', '系统显示名称'),
('copyright', 'Copyright © 2024 校园图书馆', '版权信息', '网站底部版权信息');

-- ============================================
-- 初始化完成
-- ============================================
UPDATE sys_user SET password = 'admin123';