-- Create Role table
CREATE TABLE IF NOT EXISTS `role` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL UNIQUE,
    `description` VARCHAR(255),
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create User table
CREATE TABLE IF NOT EXISTS `user` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `username` VARCHAR(100) NOT NULL UNIQUE,
    `email` VARCHAR(100) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `full_name` VARCHAR(100),
    `role_id` INT NOT NULL,
    `is_active` BOOLEAN DEFAULT TRUE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`role_id`) REFERENCES `role`(`id`) ON DELETE RESTRICT
);

-- Create index for faster queries
CREATE INDEX idx_user_username ON `user`(`username`);
CREATE INDEX idx_user_email ON `user`(`email`);

-- Insert default roles
INSERT INTO `role` (`name`, `description`) VALUES 
('ADMIN', 'Administrator - Full Access'),
('USER', 'Regular User - View Only');

-- Insert default admin user (password: admin123 - CHANGE THIS IN PRODUCTION)
-- Password hash: $2a$10$... (bcrypt)
INSERT INTO `user` (`username`, `email`, `password`, `full_name`, `role_id`, `is_active`) VALUES 
('admin', 'admin@afes.local', '$2a$10$x3Vl6e/vU.ENPD5.46qYz.f7Y7wYu8QF5X7PfQqKQb8T6z5JZNEVa', 'Administrator', 1, TRUE),
('user1', 'user1@afes.local', '$2a$10$Qs0Lx5q8R4.KQ9E2PxM7Q.ENPD5.46qYz.f7Y7wYu8QF5X7PfQqY', 'Test User', 2, TRUE);
