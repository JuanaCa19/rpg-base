CREATE DATABASE IF NOT EXISTS rpg_arena_db;
USE rpg_arena_db;

CREATE TABLE IF NOT EXISTS jugadores (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario VARCHAR(50) UNIQUE NOT NULL,
    clave VARCHAR(100) NOT NULL,
    rol VARCHAR(20) NOT NULL DEFAULT 'jugador'
);

CREATE TABLE IF NOT EXISTS monstruos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) UNIQUE NOT NULL,
    vida INT NOT NULL,
    ataque INT NOT NULL
);

CREATE TABLE IF NOT EXISTS victorias (
    id INT AUTO_INCREMENT PRIMARY KEY,
    jugador_id INT NOT NULL,
    monstruo_id INT NOT NULL,
    fecha DATETIME NOT NULL DEFAULT NOW(),
    FOREIGN KEY (jugador_id) REFERENCES jugadores(id),
    FOREIGN KEY (monstruo_id) REFERENCES monstruos(id)
);

INSERT INTO jugadores (usuario, clave, rol) VALUES ('admin', 'admin123', 'admin');
INSERT INTO jugadores (usuario, clave, rol) VALUES ('jugador1', 'clave123', 'jugador');

INSERT INTO monstruos (nombre, vida, ataque) VALUES ('Goblin', 50, 8);
INSERT INTO monstruos (nombre, vida, ataque) VALUES ('Arana Gigante', 60, 10);
INSERT INTO monstruos (nombre, vida, ataque) VALUES ('Ogro', 100, 14);
INSERT INTO monstruos (nombre, vida, ataque) VALUES ('Dragon Joven', 160, 20);
