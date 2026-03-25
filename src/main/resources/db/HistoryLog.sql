DROP TABLE IF EXISTS history_log;

CREATE TABLE history_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    temperature FLOAT NOT NULL,       -- Lưu dữ liệu từ DHT20
    gas_level FLOAT NOT NULL,        -- Lưu dữ liệu từ MQ-2
    fire_detected BIT(1) NOT NULL,    -- Lưu trạng thái boolean (0 hoặc 1)
    timestamp DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO history_log (temperature, gas_level, fire_detected, timestamp)
VALUES (45.2, 550.0, 1, NOW());