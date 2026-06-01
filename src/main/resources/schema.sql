CREATE TABLE IF NOT EXISTS usuario (
                                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       nombre VARCHAR(255),
    dni VARCHAR(20),
    telefono VARCHAR(20),
    correo VARCHAR(255),
    username VARCHAR(255),
    password VARCHAR(255),
    rol VARCHAR(50),
    activo BOOLEAN
    );

CREATE TABLE IF NOT EXISTS cliente (
                                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       nombre VARCHAR(255),
    dni VARCHAR(20),
    telefono VARCHAR(20),
    correo VARCHAR(255),
    direccion VARCHAR(255),
    estado VARCHAR(50)
    );

CREATE TABLE IF NOT EXISTS cuenta_bancaria (
                                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                               numero_cuenta VARCHAR(50),
    id_cliente BIGINT,
    tipo_cuenta VARCHAR(50),
    saldo DECIMAL(19, 4),
    estado VARCHAR(50),
    FOREIGN KEY (id_cliente) REFERENCES cliente(id)
    );