START TRANSACTION;
CREATE DATABASE IF NOT EXISTS chefmono;
CREATE TABLE IF NOT EXISTS chefmono.todoitem (
	tab BIGINT,
	menu_number INT,
	description VARCHAR(255)
);
DROP USER IF EXISTS
	'chef'@'localhost',
	'chef'@'%';
FLUSH PRIVILEGES;
CREATE USER IF NOT EXISTS
	'chef'@'localhost' IDENTIFIED BY 'pwd_chef',
	'chef'@'%' IDENTIFIED BY 'pwd_chef';
FLUSH PRIVILEGES;
GRANT INSERT, DELETE, SELECT ON chefmono.todoitem TO 'chef'@'localhost';
GRANT INSERT, DELETE, SELECT ON chefmono.todoitem TO 'chef'@'%';
FLUSH PRIVILEGES;
CREATE DATABASE IF NOT EXISTS opentabs;
CREATE TABLE IF NOT EXISTS opentabs.tabletodo (
	tab_id BIGINT,
	table_number INT,
	waiter VARCHAR(64),
	amount_paid DOUBLE,
	to_pay DOUBLE,
	tip DOUBLE,
	closed BIT
);
CREATE TABLE IF NOT EXISTS opentabs.itemtodo (
	tab_id BIGINT,
	list_name VARCHAR(32),
	menu_number INT,
	description VARCHAR(255),
	price DOUBLE
);
DROP USER IF EXISTS
	'opentabs'@'localhost',
	'opentabs'@'%';
FLUSH PRIVILEGES;
CREATE USER IF NOT EXISTS 
	'opentabs'@'localhost' IDENTIFIED BY 'pwd_opentabs',
	'opentabs'@'%' IDENTIFIED BY 'pwd_opentabs';
FLUSH PRIVILEGES;
GRANT SELECT, INSERT, UPDATE ON opentabs.tabletodo TO 'opentabs'@'localhost';
GRANT SELECT, INSERT, UPDATE ON opentabs.tabletodo TO 'opentabs'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON opentabs.itemtodo TO 'opentabs'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON opentabs.itemtodo TO 'opentabs'@'%';
FLUSH PRIVILEGES;
CREATE DATABASE IF NOT EXISTS tabmono;
CREATE TABLE IF NOT EXISTS tabmono.tabs (
	id BIGINT,
	table_number INT,
	open BIT,
	served_values DOUBLE
);
CREATE TABLE IF NOT EXISTS tabmono.ordereditem (
	tab_id BIGINT,
	menu_number INT,
	description VARCHAR(255),
	is_drink BIT,
	price DOUBLE,
	list_type VARCHAR(32)
);
DROP USER IF EXISTS
	'tab'@'localhost',
	'tab'@'%';
FLUSH PRIVILEGES;
CREATE USER IF NOT EXISTS 
	'tab'@'localhost' IDENTIFIED BY 'pwd_tab',
	'tab'@'%' IDENTIFIED BY 'pwd_tab';
FLUSH PRIVILEGES;
GRANT SELECT, INSERT, UPDATE ON tabmono.tabs TO 'tab'@'localhost';
GRANT SELECT, INSERT, UPDATE ON tabmono.tabs TO 'tab'@'%';
GRANT SELECT, INSERT, UPDATE ON tabmono.ordereditem TO 'tab'@'localhost';
GRANT SELECT, INSERT, UPDATE ON tabmono.ordereditem TO 'tab'@'%';
FLUSH PRIVILEGES;
COMMIT;