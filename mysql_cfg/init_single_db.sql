CREATE DATABASE IF NOT EXISTS single;
CREATE TABLE IF NOT EXISTS single.log (
	server_name VARCHAR(32),
	log_type VARCHAR(32),
	timestamp BIGINT,
	additional JSON
);
CREATE TABLE IF NOT EXISTS single.login (
	username VARCHAR(64),
	timestamp BIGINT
);
CREATE TABLE IF NOT EXISTS single.gioco (
	id_gioco BIGINT AUTO_INCREMENT PRIMARY KEY,
	n_giocatori INT,
	giocatori_per_team INT,
	nome_gioco VARCHAR(64) UNIQUE
);
CREATE TABLE IF NOT EXISTS single.giocatore (
	username VARCHAR(64) PRIMARY KEY,
	password VARBINARY(256),
	nome VARCHAR(64)	
);
CREATE TABLE IF NOT EXISTS single.partita(
	id_partita BIGINT AUTO_INCREMENT,
	id_gioco BIGINT,
	blob_partita BLOB,
	PRIMARY KEY (id_partita,id_gioco)
	)
PARTITION BY KEY()
PARTITIONS 100;
CREATE TABLE IF NOT EXISTS single.partita_giocatore (
	username VARCHAR(64),
	id_gioco BIGINT,
	id_partita BIGINT,
	conclusa BIT,
	PRIMARY KEY (username, id_gioco, id_partita)
);
CREATE TABLE IF NOT EXISTS single.classifica(
	id BIGINT PRIMARY KEY AUTO_INCREMENT,
	id_gioco BIGINT,
	username VARCHAR(64),
	point INT,
	additional JSON,
	FOREIGN KEY (id_gioco) REFERENCES single.gioco(id_gioco),
	FOREIGN KEY (username) REFERENCES single.giocatore(username)
);
CREATE TABLE IF NOT EXISTS single.matcher(
	username VARCHAR(64),
	id_gioco BIGINT,
	timestamp_sec BIGINT,
	timestamp_nano INT,
	FOREIGN KEY (id_gioco) REFERENCES single.gioco(id_gioco),
	FOREIGN KEY (username) REFERENCES single.giocatore(username)
);
CREATE OR REPLACE VIEW single.giocatore_pub AS SELECT username, nome FROM single.giocatore;

DROP USER IF EXISTS
	'savelog'@'localhost',
	'savelog'@'%';
FLUSH PRIVILEGES;
CREATE USER IF NOT EXISTS
	'savelog'@'localhost' IDENTIFIED BY 'pwd_savelog',
	'savelog'@'%' IDENTIFIED BY 'pwd_savelog';
FLUSH PRIVILEGES;
GRANT INSERT ON single.log TO 'savelog'@'localhost';
GRANT INSERT ON single.log TO 'savelog'@'%';
FLUSH PRIVILEGES;
DROP USER IF EXISTS
	'player'@'localhost',
	'player'@'%';
FLUSH PRIVILEGES;
CREATE USER IF NOT EXISTS 
	'player'@'localhost' IDENTIFIED BY 'pwd_player',
	'player'@'%' IDENTIFIED BY 'pwd_player';
FLUSH PRIVILEGES;
GRANT SELECT ON single.gioco TO 'player'@'localhost';
GRANT SELECT ON single.gioco TO 'player'@'%';
GRANT SELECT, INSERT, UPDATE ON single.giocatore TO  'player'@'localhost';
GRANT SELECT, INSERT, UPDATE ON single.giocatore TO  'player'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON single.partita_giocatore TO  'player'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON single.partita_giocatore TO  'player'@'%';
FLUSH PRIVILEGES;
GRANT SELECT, INSERT, DELETE, UPDATE ON single.login TO  'player'@'localhost';
GRANT SELECT, INSERT, DELETE, UPDATE ON single.login TO  'player'@'%';
FLUSH PRIVILEGES;
DROP USER IF EXISTS
	'briscola'@'localhost',
	'briscola'@'%';
FLUSH PRIVILEGES;
CREATE USER IF NOT EXISTS 
	'briscola'@'localhost' IDENTIFIED BY 'pwd_briscola',
	'briscola'@'%' IDENTIFIED BY 'pwd_briscola';
FLUSH PRIVILEGES;
GRANT SELECT ON single.gioco TO 'briscola'@'localhost';
GRANT SELECT ON single.gioco TO 'briscola'@'%';
GRANT SELECT, INSERT, UPDATE ON single.partita TO  'briscola'@'localhost';
GRANT SELECT, INSERT, UPDATE ON single.partita TO  'briscola'@'%';
FLUSH PRIVILEGES;
DROP USER IF EXISTS
	'classifica'@'localhost',
	'classifica'@'%';
FLUSH PRIVILEGES;
CREATE USER IF NOT EXISTS 
	'classifica'@'localhost' IDENTIFIED BY 'pwd_classifica',
	'classifica'@'%' IDENTIFIED BY 'pwd_classifica';
FLUSH PRIVILEGES;
GRANT SELECT, INSERT, UPDATE ON single.classifica TO  'classifica'@'localhost';
GRANT SELECT, INSERT, UPDATE ON single.classifica TO  'classifica'@'%';
FLUSH PRIVILEGES;
DROP USER IF EXISTS
	'matcher'@'localhost',
	'matcher'@'%';
FLUSH PRIVILEGES;
CREATE USER IF NOT EXISTS 
	'matcher'@'localhost' IDENTIFIED BY 'pwd_matcher',
	'matcher'@'%' IDENTIFIED BY 'pwd_matcher';
FLUSH PRIVILEGES;
GRANT SELECT ON single.gioco TO 'matcher'@'localhost';
GRANT SELECT ON single.gioco TO 'matcher'@'%';
GRANT SELECT, INSERT, DELETE ON single.matcher TO  'matcher'@'localhost';
GRANT SELECT, INSERT, DELETE ON single.matcher TO  'matcher'@'%';
FLUSH PRIVILEGES;