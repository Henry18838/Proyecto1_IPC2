CREATE DATABASE  IF NOT EXISTS `horizontes_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `horizontes_db`;
-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: localhost    Database: horizontes_db
-- ------------------------------------------------------
-- Server version	8.0.45

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `cancelaciones`
--

DROP TABLE IF EXISTS `cancelaciones`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cancelaciones` (
  `id` int NOT NULL AUTO_INCREMENT,
  `reservacion_id` int NOT NULL,
  `fecha_cancelacion` datetime DEFAULT CURRENT_TIMESTAMP,
  `monto_reembolsado` decimal(10,2) NOT NULL,
  `porcentaje_reembolso` decimal(5,2) NOT NULL,
  `perdida_agencia` decimal(10,2) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `reservacion_id` (`reservacion_id`),
  CONSTRAINT `cancelaciones_ibfk_1` FOREIGN KEY (`reservacion_id`) REFERENCES `reservaciones` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cancelaciones`
--

LOCK TABLES `cancelaciones` WRITE;
/*!40000 ALTER TABLE `cancelaciones` DISABLE KEYS */;
/*!40000 ALTER TABLE `cancelaciones` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `clientes`
--

DROP TABLE IF EXISTS `clientes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `clientes` (
  `id` int NOT NULL AUTO_INCREMENT,
  `dpi` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `nombre` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `fecha_nacimiento` date DEFAULT NULL,
  `telefono` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `nacionalidad` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `fecha_creacion` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `dpi` (`dpi`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `clientes`
--

LOCK TABLES `clientes` WRITE;
/*!40000 ALTER TABLE `clientes` DISABLE KEYS */;
INSERT INTO `clientes` VALUES (1,'31303751508','Henry Josue Argueta Champet','2002-11-06','38889773','champet258@gmail.com','Guatemalteco','2026-04-09 23:02:23');
/*!40000 ALTER TABLE `clientes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `destinos`
--

DROP TABLE IF EXISTS `destinos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `destinos` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `pais` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `descripcion` text COLLATE utf8mb4_unicode_ci,
  `clima` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `imagen_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `fecha_creacion` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `nombre` (`nombre`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `destinos`
--

LOCK TABLES `destinos` WRITE;
/*!40000 ALTER TABLE `destinos` DISABLE KEYS */;
INSERT INTO `destinos` VALUES (1,'Cancun','Mexico','Playa Cancun Dos Noches','Verano','https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRHPjG9IfsoerEiixe5G2ugQJVb4zDRrawAGw&s','2026-04-10 14:15:04'),(2,'Madrid Barcelona','Espana','Vive el clasico de futbol como nunca antes.','Verano','https://www.instagram.com/p/DTQ6Y6YCWfl/','2026-04-13 11:44:14');
/*!40000 ALTER TABLE `destinos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pagos`
--

DROP TABLE IF EXISTS `pagos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pagos` (
  `id` int NOT NULL AUTO_INCREMENT,
  `reservacion_id` int NOT NULL,
  `monto` decimal(10,2) NOT NULL,
  `metodo` tinyint NOT NULL COMMENT '1=Efectivo, 2=Tarjeta, 3=Transferencia',
  `fecha_pago` date NOT NULL,
  `fecha_registro` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `reservacion_id` (`reservacion_id`),
  CONSTRAINT `pagos_ibfk_1` FOREIGN KEY (`reservacion_id`) REFERENCES `reservaciones` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pagos`
--

LOCK TABLES `pagos` WRITE;
/*!40000 ALTER TABLE `pagos` DISABLE KEYS */;
INSERT INTO `pagos` VALUES (1,1,3000.00,1,'2026-04-10','2026-04-10 14:20:20'),(2,1,1500.00,2,'2026-04-10','2026-04-10 14:24:59'),(3,1,500.00,1,'2026-04-13','2026-04-13 12:12:43');
/*!40000 ALTER TABLE `pagos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `paquetes`
--

DROP TABLE IF EXISTS `paquetes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `paquetes` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `destino_id` int NOT NULL,
  `duracion_dias` int NOT NULL,
  `descripcion` text COLLATE utf8mb4_unicode_ci,
  `precio_venta` decimal(10,2) NOT NULL,
  `capacidad_maxima` int NOT NULL,
  `activo` tinyint(1) DEFAULT '1',
  `fecha_creacion` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `nombre` (`nombre`),
  KEY `destino_id` (`destino_id`),
  CONSTRAINT `paquetes_ibfk_1` FOREIGN KEY (`destino_id`) REFERENCES `destinos` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `paquetes`
--

LOCK TABLES `paquetes` WRITE;
/*!40000 ALTER TABLE `paquetes` DISABLE KEYS */;
INSERT INTO `paquetes` VALUES (1,'Viaje Inolvidable',1,2,'Viaje de dos dias a cancun y una noche para ver el concierto de Coachella',5000.00,10,1,'2026-04-10 14:16:00'),(2,'Clasico EspaÃ±ol 2026',2,7,'Vive el clasico espaÃ±ol 2026 como nunca antes.',25000.00,10,1,'2026-04-13 12:11:06');
/*!40000 ALTER TABLE `paquetes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `proveedores`
--

DROP TABLE IF EXISTS `proveedores`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `proveedores` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `tipo` tinyint NOT NULL COMMENT '1=Aerolinea, 2=Hotel, 3=Tour, 4=Traslado, 5=Otro',
  `pais` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `contacto` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `fecha_creacion` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `nombre` (`nombre`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `proveedores`
--

LOCK TABLES `proveedores` WRITE;
/*!40000 ALTER TABLE `proveedores` DISABLE KEYS */;
INSERT INTO `proveedores` VALUES (1,'Avianca',1,'Mexico','avianca@gmail.com','2026-04-10 14:16:38'),(2,'Hotel Cancun',2,'Mexico','hotelcancun@gmail.com','2026-04-10 14:17:00'),(3,'Cancun Tour',3,'Mexico','tourcancun@gmail.com','2026-04-10 14:17:23'),(4,'AirBus',4,'Mexico','airbus@gmail.com','2026-04-10 14:17:43');
/*!40000 ALTER TABLE `proveedores` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reservacion_pasajeros`
--

DROP TABLE IF EXISTS `reservacion_pasajeros`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reservacion_pasajeros` (
  `reservacion_id` int NOT NULL,
  `cliente_id` int NOT NULL,
  PRIMARY KEY (`reservacion_id`,`cliente_id`),
  KEY `cliente_id` (`cliente_id`),
  CONSTRAINT `reservacion_pasajeros_ibfk_1` FOREIGN KEY (`reservacion_id`) REFERENCES `reservaciones` (`id`),
  CONSTRAINT `reservacion_pasajeros_ibfk_2` FOREIGN KEY (`cliente_id`) REFERENCES `clientes` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reservacion_pasajeros`
--

LOCK TABLES `reservacion_pasajeros` WRITE;
/*!40000 ALTER TABLE `reservacion_pasajeros` DISABLE KEYS */;
INSERT INTO `reservacion_pasajeros` VALUES (1,1);
/*!40000 ALTER TABLE `reservacion_pasajeros` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reservaciones`
--

DROP TABLE IF EXISTS `reservaciones`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reservaciones` (
  `id` int NOT NULL AUTO_INCREMENT,
  `numero_reservacion` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `paquete_id` int NOT NULL,
  `agente_id` int NOT NULL,
  `fecha_creacion` datetime DEFAULT CURRENT_TIMESTAMP,
  `fecha_viaje` date NOT NULL,
  `cantidad_pasajeros` int NOT NULL,
  `costo_total` decimal(10,2) NOT NULL,
  `estado` tinyint DEFAULT '1' COMMENT '1=Pendiente, 2=Confirmada, 3=Cancelada, 4=Completada',
  PRIMARY KEY (`id`),
  UNIQUE KEY `numero_reservacion` (`numero_reservacion`),
  KEY `paquete_id` (`paquete_id`),
  KEY `agente_id` (`agente_id`),
  CONSTRAINT `reservaciones_ibfk_1` FOREIGN KEY (`paquete_id`) REFERENCES `paquetes` (`id`),
  CONSTRAINT `reservaciones_ibfk_2` FOREIGN KEY (`agente_id`) REFERENCES `usuarios` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reservaciones`
--

LOCK TABLES `reservaciones` WRITE;
/*!40000 ALTER TABLE `reservaciones` DISABLE KEYS */;
INSERT INTO `reservaciones` VALUES (1,'RES-00001',1,1,'2026-04-10 14:19:35','2026-04-14',1,5000.00,2);
/*!40000 ALTER TABLE `reservaciones` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `servicios_paquete`
--

DROP TABLE IF EXISTS `servicios_paquete`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `servicios_paquete` (
  `id` int NOT NULL AUTO_INCREMENT,
  `paquete_id` int NOT NULL,
  `proveedor_id` int NOT NULL,
  `descripcion` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `costo` decimal(10,2) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `paquete_id` (`paquete_id`),
  KEY `proveedor_id` (`proveedor_id`),
  CONSTRAINT `servicios_paquete_ibfk_1` FOREIGN KEY (`paquete_id`) REFERENCES `paquetes` (`id`),
  CONSTRAINT `servicios_paquete_ibfk_2` FOREIGN KEY (`proveedor_id`) REFERENCES `proveedores` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `servicios_paquete`
--

LOCK TABLES `servicios_paquete` WRITE;
/*!40000 ALTER TABLE `servicios_paquete` DISABLE KEYS */;
INSERT INTO `servicios_paquete` VALUES (1,1,4,'Traslado de Aeropuerto a Hotel',300.00),(2,1,1,'Vuelo ida y vuelta',1000.00),(3,1,2,'Una noche de hotel',500.00);
/*!40000 ALTER TABLE `servicios_paquete` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuarios`
--

DROP TABLE IF EXISTS `usuarios`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuarios` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `rol` tinyint NOT NULL COMMENT '1=Atencion Cliente, 2=Operaciones, 3=Administrador',
  `activo` tinyint(1) DEFAULT '1',
  `fecha_creacion` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `nombre` (`nombre`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuarios`
--

LOCK TABLES `usuarios` WRITE;
/*!40000 ALTER TABLE `usuarios` DISABLE KEYS */;
INSERT INTO `usuarios` VALUES (1,'admin','admin12',3,1,'2026-04-06 20:58:38'),(2,'henry18838','123456',2,0,'2026-04-10 14:10:09'),(3,'josue123','123456',1,1,'2026-04-10 14:22:37'),(4,'champet123','123456',2,1,'2026-04-10 14:23:40');
/*!40000 ALTER TABLE `usuarios` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-13 12:34:13
