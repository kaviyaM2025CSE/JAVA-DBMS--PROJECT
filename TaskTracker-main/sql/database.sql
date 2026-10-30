CREATE DATABASE IF NOT EXISTS VSCodesTaskDB;
USE VSCodeTaskDB;


DROP TABLE IF EXISTS tasks;
DROP TABLE IF EXISTS users;


CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    role_type VARCHAR(50) NOT NULL DEFAULT 'DEVELOPER'
);


CREATE TABLE tasks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    assigned_user_id INT,
    FOREIGN KEY (assigned_user_id) REFERENCES users(user_id) ON DELETE CASCADE
);


INSERT INTO users (role_type) VALUES ('LEAD_ENGINEER');
INSERT INTO tasks (title, status, priority, assigned_user_id) VALUES ('OOPs and DBMS Core Integration', 'COMPLETED', 'HIGH', 1);


SELECT * FROM tasks;
