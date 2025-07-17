-- 插入测试用户数据
INSERT INTO users (username, password, email) VALUES
('user1', '$2a$10$GcLQxZJqX9JZJZJZJZJZJZJZJZJZJZJZJZJZJZJZJZJZJZJZJZJZJZJ', 'user1@example.com'),
('user2', '$2a$10$GcLQxZJqX9JZJZJZJZJZJZJZJZJZJZJZJZJZJZJZJZJZJZJZJZJZJZJ', 'user2@example.com'),
('admin', '$2a$10$GcLQxZJqX9JZJZJZJZJZJZJZJZJZJZJZJZJZJZJZJZJZJZJZJZJZJZJ', 'admin@example.com');

-- 插入测试节点数据
INSERT INTO nodes (latitude, longitude, node_name, node_type) VALUES
(39.9042, 116.4074, '天安门', 'intersection'),
(39.9975, 116.3376, '北京大学', 'landmark'),
(39.9151, 116.4038, '故宫', 'landmark'),
(39.9834, 116.3159, '清华大学', 'landmark'),
(39.9299, 116.3879, '人民大会堂', 'landmark');

-- 插入测试边数据
INSERT INTO edges (source_node_id, target_node_id, distance, road_name, road_type, capacity, is_bidirectional) VALUES
(1, 3, 1.2, '东长安街', 'main_road', 1000, true),
(3, 5, 0.8, '西长安街', 'main_road', 800, true),
(1, 2, 8.5, '北三环路', 'expressway', 2000, true),
(2, 4, 1.5, '成府路', 'secondary_road', 500, true),
(4, 3, 7.2, '中关村大街', 'main_road', 1500, true);

-- 插入测试交通数据
INSERT INTO traffic_data (edge_id, timestamp, traffic_volume, average_speed, congestion_level, data_source) VALUES
(1, CURRENT_TIMESTAMP - INTERVAL '30 minutes', 800, 30.5, 'medium', 'traffic_camera'),
(1, CURRENT_TIMESTAMP - INTERVAL '15 minutes', 950, 25.2, 'high', 'traffic_camera'),
(2, CURRENT_TIMESTAMP - INTERVAL '20 minutes', 400, 45.0, 'low', 'traffic_camera'),
(3, CURRENT_TIMESTAMP - INTERVAL '10 minutes', 1500, 60.0, 'medium', 'gps_tracking'),
(4, CURRENT_TIMESTAMP - INTERVAL '5 minutes', 300, 35.8, 'low', 'gps_tracking');

-- 插入索引以提高查询性能
CREATE INDEX idx_edges_source ON edges(source_node_id);
CREATE INDEX idx_edges_target ON edges(target_node_id);
CREATE INDEX idx_traffic_data_edge ON traffic_data(edge_id);
CREATE INDEX idx_traffic_data_timestamp ON traffic_data(timestamp);