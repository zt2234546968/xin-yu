CREATE TABLE IF NOT EXISTS role (
    id VARCHAR(36) NOT NULL COMMENT 'Role ID',
    role_name VARCHAR(50) NOT NULL COMMENT 'Role name',
    description VARCHAR(255) NULL COMMENT 'Role description',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1 enabled, 0 disabled',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_role_name (role_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='System roles';

CREATE TABLE IF NOT EXISTS country (
    id VARCHAR(36) NOT NULL COMMENT 'Country ID',
    country_name VARCHAR(50) NOT NULL COMMENT 'Country name',
    flag_image VARCHAR(255) NOT NULL COMMENT 'Flag image URL',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_country_country_name (country_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Country dictionary';

CREATE TABLE IF NOT EXISTS `user` (
    id VARCHAR(36) NOT NULL COMMENT 'User ID',
    password VARCHAR(100) NOT NULL COMMENT 'Login password',
    real_name VARCHAR(50) NOT NULL COMMENT 'Real name',
    phone VARCHAR(20) NOT NULL COMMENT 'Mobile phone',
    wechat VARCHAR(50) NOT NULL COMMENT 'Wechat account',
    invite_code VARCHAR(50) NOT NULL COMMENT 'Invitation code used at registration',
    avatar VARCHAR(255) NULL COMMENT 'Avatar URL',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1 enabled, 0 disabled',
    role_id VARCHAR(36) NULL COMMENT 'Role ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_phone (phone),
    KEY idx_user_role_id (role_id),
    CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES role (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='System users';

CREATE TABLE IF NOT EXISTS invitation_code (
    id VARCHAR(36) NOT NULL COMMENT 'Invitation code ID',
    code VARCHAR(50) NOT NULL COMMENT 'Invitation code',
    used TINYINT NOT NULL DEFAULT 0 COMMENT '0 unused, 1 used',
    user_id VARCHAR(36) NULL COMMENT 'Bound user ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    remark VARCHAR(500) NULL COMMENT 'Remark',
    PRIMARY KEY (id),
    UNIQUE KEY uk_invitation_code_code (code),
    KEY idx_invitation_code_user_id (user_id),
    CONSTRAINT fk_invitation_code_user FOREIGN KEY (user_id) REFERENCES `user` (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Invitation codes';

CREATE TABLE IF NOT EXISTS zhiping (
    id VARCHAR(36) NOT NULL COMMENT 'Task ID',
    code VARCHAR(20) NOT NULL COMMENT 'Task code',
    asin VARCHAR(50) NOT NULL COMMENT 'ASIN',
    review_title VARCHAR(255) NOT NULL COMMENT 'Review title',
    review_content TEXT NOT NULL COMMENT 'Review content',
    task_image VARCHAR(255) NOT NULL COMMENT 'Task image',
    star_rating INT NOT NULL COMMENT 'Star rating',
    country_id VARCHAR(36) NULL COMMENT 'Country ID',
    channel VARCHAR(255) NULL COMMENT 'Channel',
    keyword VARCHAR(255) NULL COMMENT 'Keyword',
    daily_quantity INT NULL COMMENT 'Daily quantity',
    warranty_time VARCHAR(50) NOT NULL DEFAULT '0' COMMENT 'Warranty time',
    status VARCHAR(50) NOT NULL DEFAULT '0' COMMENT 'Task status',
    feedback_link VARCHAR(255) NULL COMMENT 'Feedback link',
    feedback_image VARCHAR(255) NULL COMMENT 'Feedback image',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_zhiping_code (code),
    KEY idx_zhiping_country_id (country_id),
    KEY idx_zhiping_status (status),
    CONSTRAINT fk_zhiping_country FOREIGN KEY (country_id) REFERENCES country (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Direct review tasks';

CREATE TABLE IF NOT EXISTS ceping (
    id VARCHAR(36) NOT NULL COMMENT 'Task ID',
    code VARCHAR(20) NOT NULL COMMENT 'Task code',
    product_image VARCHAR(255) NULL COMMENT 'Product image',
    product_name VARCHAR(255) NOT NULL COMMENT 'Product name',
    product_link VARCHAR(255) NULL COMMENT 'Product link',
    country_id VARCHAR(36) NULL COMMENT 'Country ID',
    asin VARCHAR(50) NULL COMMENT 'ASIN',
    review_title VARCHAR(255) NULL COMMENT 'Review title',
    review_content TEXT NULL COMMENT 'Review content',
    shop VARCHAR(255) NULL COMMENT 'Shop',
    free_review INT NOT NULL DEFAULT 0,
    star_review INT NOT NULL DEFAULT 0,
    text_review INT NOT NULL DEFAULT 0,
    image_review INT NOT NULL DEFAULT 0,
    video_review INT NOT NULL DEFAULT 0,
    feedback_review INT NOT NULL DEFAULT 0,
    total_quantity INT NOT NULL DEFAULT 0,
    is_positive TINYINT NOT NULL DEFAULT 1,
    price DECIMAL(10, 2) NULL,
    keyword VARCHAR(255) NULL,
    daily_quantity INT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT '0',
    budget DECIMAL(10, 2) NULL,
    admin_budget DECIMAL(10, 2) NULL,
    user_budget DECIMAL(10, 2) NULL,
    admin_message TINYINT NOT NULL DEFAULT 0,
    user_message TINYINT NOT NULL DEFAULT 0,
    remark TEXT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_ceping_code (code),
    KEY idx_ceping_country_id (country_id),
    KEY idx_ceping_status (status),
    CONSTRAINT fk_ceping_country FOREIGN KEY (country_id) REFERENCES country (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Evaluation tasks';

CREATE TABLE IF NOT EXISTS order_list (
    id VARCHAR(36) NOT NULL COMMENT 'Order ID',
    ceping_id VARCHAR(36) NULL COMMENT 'Evaluation task ID',
    order_number VARCHAR(255) NULL COMMENT 'Order number',
    order_screenshot VARCHAR(255) NULL COMMENT 'Order screenshot',
    review_link VARCHAR(255) NULL COMMENT 'Review link',
    review_screenshot VARCHAR(255) NULL COMMENT 'Review screenshot',
    expense_detail TEXT NULL COMMENT 'Expense detail',
    principal DECIMAL(10, 2) NULL,
    pp_multiplier DECIMAL(5, 4) NULL,
    exchange_rate DECIMAL(10, 4) NULL,
    exchange_rate_add DECIMAL(10, 2) NULL,
    commission DECIMAL(10, 2) NULL,
    pp_price DECIMAL(10, 2) NULL,
    sum DECIMAL(10, 2) NULL,
    status VARCHAR(50) NULL DEFAULT '0',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_order_list_ceping_id (ceping_id),
    KEY idx_order_list_status (status),
    CONSTRAINT fk_order_list_ceping FOREIGN KEY (ceping_id) REFERENCES ceping (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Evaluation orders';

INSERT INTO role (id, role_name, description, status)
VALUES
    ('00000000-0000-0000-0000-000000000001', 'SUPER_ADMIN', 'Super administrator', 1),
    ('00000000-0000-0000-0000-000000000002', 'ADMIN', 'Administrator', 1),
    ('00000000-0000-0000-0000-000000000003', 'USER', 'Default user', 1)
ON DUPLICATE KEY UPDATE description = VALUES(description), status = VALUES(status);

INSERT INTO country (id, country_name, flag_image, deleted)
VALUES
    ('10000000-0000-0000-0000-000000000001', '美国', '/src/assets/test.jpg', 0),
    ('10000000-0000-0000-0000-000000000002', '德国', '/src/assets/test.jpg', 0),
    ('10000000-0000-0000-0000-000000000003', '英国', '/src/assets/test.jpg', 0),
    ('10000000-0000-0000-0000-000000000004', '法国', '/src/assets/test.jpg', 0),
    ('10000000-0000-0000-0000-000000000005', '意大利', '/src/assets/test.jpg', 0),
    ('10000000-0000-0000-0000-000000000006', '西班牙', '/src/assets/test.jpg', 0),
    ('10000000-0000-0000-0000-000000000007', '日本', '/src/assets/test.jpg', 0),
    ('10000000-0000-0000-0000-000000000008', '加拿大', '/src/assets/test.jpg', 0)
ON DUPLICATE KEY UPDATE
    flag_image = VALUES(flag_image),
    deleted = VALUES(deleted);
