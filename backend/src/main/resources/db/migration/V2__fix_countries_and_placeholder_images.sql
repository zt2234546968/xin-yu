SET @country_sort_order_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'country'
      AND COLUMN_NAME = 'sort_order'
);

SET @country_sort_order_sql = IF(
    @country_sort_order_exists = 0,
    'ALTER TABLE country ADD COLUMN sort_order INT NOT NULL DEFAULT 999 COMMENT ''Sort order, USA first'' AFTER flag_image',
    'SELECT 1'
);

PREPARE country_sort_order_stmt FROM @country_sort_order_sql;
EXECUTE country_sort_order_stmt;
DEALLOCATE PREPARE country_sort_order_stmt;

INSERT INTO country (id, country_name, flag_image, sort_order, create_time, update_time, deleted)
VALUES
    ('10000000-0000-0000-0000-000000000001', '美国', '/test.jpg', 1, NOW(), NOW(), 0),
    ('10000000-0000-0000-0000-000000000002', '英国', '/test.jpg', 2, NOW(), NOW(), 0),
    ('10000000-0000-0000-0000-000000000003', '德国', '/test.jpg', 3, NOW(), NOW(), 0),
    ('10000000-0000-0000-0000-000000000004', '法国', '/test.jpg', 4, NOW(), NOW(), 0),
    ('10000000-0000-0000-0000-000000000005', '意大利', '/test.jpg', 5, NOW(), NOW(), 0),
    ('10000000-0000-0000-0000-000000000006', '西班牙', '/test.jpg', 6, NOW(), NOW(), 0),
    ('10000000-0000-0000-0000-000000000007', '加拿大', '/test.jpg', 7, NOW(), NOW(), 0),
    ('10000000-0000-0000-0000-000000000008', '日本', '/test.jpg', 8, NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE
    flag_image = VALUES(flag_image),
    sort_order = VALUES(sort_order),
    update_time = VALUES(update_time),
    deleted = VALUES(deleted);

UPDATE country
SET deleted = 1
WHERE country_name NOT IN ('美国', '英国', '德国', '法国', '意大利', '西班牙', '加拿大', '日本');

UPDATE zhiping
SET task_image = '/test.jpg'
WHERE task_image IS NULL OR task_image = '' OR task_image = '/src/assets/test.jpg';

UPDATE ceping
SET product_image = '/test.jpg'
WHERE product_image IS NULL OR product_image = '' OR product_image = '/src/assets/test.jpg';
