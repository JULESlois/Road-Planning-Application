-- 数据库初始化脚本
-- 执行表结构创建脚本
\i schema.sql

-- 执行测试数据插入脚本
\i data.sql

-- 初始化完成提示
SELECT 'Database initialization completed successfully' AS result;