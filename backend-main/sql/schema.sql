-- 创建用户表
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE
);

-- 创建节点表
CREATE TABLE nodes (
    id SERIAL PRIMARY KEY,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    node_name VARCHAR(100),
    node_type VARCHAR(50)
);

-- 创建边表
CREATE TABLE edges (
    id SERIAL PRIMARY KEY,
    source_node_id BIGINT NOT NULL,
    target_node_id BIGINT NOT NULL,
    distance DOUBLE PRECISION NOT NULL,
    road_name VARCHAR(100),
    road_type VARCHAR(50),
    capacity INTEGER,
    is_bidirectional BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (source_node_id) REFERENCES nodes(id),
    FOREIGN KEY (target_node_id) REFERENCES nodes(id)
);

-- 创建交通数据表
CREATE TABLE traffic_data (
    id SERIAL PRIMARY KEY,
    edge_id BIGINT NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    traffic_volume INTEGER,
    average_speed DOUBLE PRECISION,
    congestion_level VARCHAR(20),
    data_source VARCHAR(50),
    FOREIGN KEY (edge_id) REFERENCES edges(id)
);