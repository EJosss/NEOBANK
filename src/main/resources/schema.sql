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

CREATE TABLE IF NOT EXISTS movimiento (
                                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                          id_cuenta BIGINT,
                                          tipo_movimiento VARCHAR(50),
    monto DECIMAL(19, 4),
    fecha TIMESTAMP,
    descripcion VARCHAR(255),
    FOREIGN KEY (id_cuenta) REFERENCES cuenta_bancaria(id)
    );

CREATE TABLE IF NOT EXISTS tarjeta (
                                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       numero_tarjeta VARCHAR(20),
    id_cuenta BIGINT,
    tipo_tarjeta VARCHAR(20),
    limite DECIMAL(19, 4),
    fecha_vencimiento VARCHAR(10),
    ccv VARCHAR(4),
    estado VARCHAR(20),
    FOREIGN KEY (id_cuenta) REFERENCES cuenta_bancaria(id)
    );
CREATE TABLE IF NOT EXISTS facturas (
                                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        numero_factura VARCHAR(50),
    id_cliente BIGINT,
    concepto VARCHAR(255),
    fecha_emision TIMESTAMP,
    subtotal DECIMAL(19, 4),
    igv DECIMAL(19, 4),
    total DECIMAL(19, 4),
    estado VARCHAR(50)
    );