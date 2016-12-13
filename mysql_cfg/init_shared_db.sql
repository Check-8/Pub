START TRANSACTION;
CREATE DATABASE IF NOT EXISTS sharedDatabase;
USE sharedDatabase;
CREATE TABLE IF NOT EXISTS menu (
	menu_number INT,
	description VARCHAR(255),
	price DOUBLE,
	is_drink BIT
);
CREATE TABLE IF NOT EXISTS tabs (
	id BIGINT,
	table_number INT,
	open BIT,
	served_values DOUBLE,
	waiter VARCHAR(64),
	paid DOUBLE,
	tip DOUBLE,
	PRIMARY KEY (id)
);
CREATE TABLE IF NOT EXISTS ordereditem (
	tab_id BIGINT,
	menu_number INT,
	description VARCHAR(255),
	is_drink BIT,
	price DOUBLE,
	list_type VARCHAR(32)
);
DROP USER IF EXISTS
	'shared'@'localhost',
	'shared'@'%';
FLUSH PRIVILEGES;
CREATE USER IF NOT EXISTS
	'shared'@'localhost' IDENTIFIED BY 'pwd_shared',
	'shared'@'%' IDENTIFIED BY 'pwd_shared';
FLUSH PRIVILEGES;
GRANT INSERT, DELETE, UPDATE, SELECT ON sharedDatabase.* TO 'shared'@'localhost';
GRANT INSERT, DELETE, UPDATE, SELECT ON sharedDatabase.* TO 'shared'@'%';
FLUSH PRIVILEGES;
COMMIT;