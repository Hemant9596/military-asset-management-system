-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: stringstack-db-hemant-13c8.l.aivencloud.com    Database: military_asset_db
-- ------------------------------------------------------
-- Server version	8.4.8

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
SET @MYSQLDUMP_TEMP_LOG_BIN = @@SESSION.SQL_LOG_BIN;
SET @@SESSION.SQL_LOG_BIN= 0;

--
-- GTID state at the beginning of the backup 
--

SET @@GLOBAL.GTID_PURGED=/*!80000 '+'*/ '55707e92-b4f3-11f1-8df4-7ef67920e639:1-37,
754f06e5-b966-11f1-9044-221e00a744e5:1-58,
ca47e8a6-b841-11f1-807f-3a4bbd5cde82:1-15';

--
-- Table structure for table `assets`
--

DROP TABLE IF EXISTS `assets`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `assets` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `equipment_type_id` bigint NOT NULL,
  `serial_number` varchar(255) DEFAULT NULL,
  `model` varchar(255) DEFAULT NULL,
  `unit` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `active` bit(1) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_assets_equipment_type` (`equipment_type_id`),
  CONSTRAINT `fk_assets_equipment_type` FOREIGN KEY (`equipment_type_id`) REFERENCES `equipment_types` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `assets`
--

LOCK TABLES `assets` WRITE;
/*!40000 ALTER TABLE `assets` DISABLE KEYS */;
INSERT INTO `assets` VALUES (1,'INSAS Rifle 001',1,'INSAS-001','INSAS','Nos','Standard service rifle',_binary ''),(2,'INSAS Rifle 001',1,'INSAS-001','INSAS','Nos','Standard service rifle',_binary '');
/*!40000 ALTER TABLE `assets` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `assignments`
--

DROP TABLE IF EXISTS `assignments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `assignments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `base_id` bigint NOT NULL,
  `asset_id` bigint NOT NULL,
  `assigned_to_user_id` bigint NOT NULL,
  `created_by` bigint NOT NULL,
  `quantity` int NOT NULL,
  `assignment_date` date NOT NULL,
  `status` varchar(255) NOT NULL,
  `notes` varchar(500) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_assignments_asset` (`asset_id`),
  KEY `fk_assignments_assigned_user` (`assigned_to_user_id`),
  KEY `fk_assignments_created_user` (`created_by`),
  KEY `ix_assignments_base_date` (`base_id`,`assignment_date`),
  CONSTRAINT `fk_assignments_asset` FOREIGN KEY (`asset_id`) REFERENCES `assets` (`id`),
  CONSTRAINT `fk_assignments_assigned_user` FOREIGN KEY (`assigned_to_user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_assignments_base` FOREIGN KEY (`base_id`) REFERENCES `bases` (`id`),
  CONSTRAINT `fk_assignments_created_user` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`),
  CONSTRAINT `ck_assignments_quantity` CHECK ((`quantity` > 0))
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `assignments`
--

LOCK TABLES `assignments` WRITE;
/*!40000 ALTER TABLE `assignments` DISABLE KEYS */;
INSERT INTO `assignments` VALUES (1,1,1,3,1,10,'2026-09-26','ACTIVE','','2026-09-26 21:49:49.012604');
/*!40000 ALTER TABLE `assignments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `audit_logs`
--

DROP TABLE IF EXISTS `audit_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `audit_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint DEFAULT NULL,
  `action` varchar(255) NOT NULL,
  `entity_type` varchar(255) DEFAULT NULL,
  `entity_id` bigint DEFAULT NULL,
  `event_time` datetime(6) NOT NULL,
  `ip_address` varchar(255) DEFAULT NULL,
  `description` varchar(1000) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_audit_logs_user` (`user_id`),
  KEY `ix_audit_logs_event_time` (`event_time`),
  CONSTRAINT `fk_audit_logs_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `audit_logs`
--

LOCK TABLES `audit_logs` WRITE;
/*!40000 ALTER TABLE `audit_logs` DISABLE KEYS */;
INSERT INTO `audit_logs` VALUES (1,1,'LOGIN','USER',1,'2026-09-26 19:15:59.164963',NULL,'Successful user login'),(2,1,'LOGIN','USER',1,'2026-09-26 20:50:00.590640',NULL,'Successful user login'),(3,1,'LOGIN','USER',1,'2026-09-26 20:58:53.469177',NULL,'Successful user login'),(4,1,'LOGIN','USER',1,'2026-09-26 21:29:34.482611',NULL,'Successful user login'),(5,1,'CREATE_OPENING_BALANCE','OPENING_BALANCE',1,'2026-09-26 21:42:16.485174',NULL,'Recorded opening balance for INSAS Rifle 001 at Bhopal Base quantity 100'),(6,1,'CREATE_PURCHASE','PURCHASE',1,'2026-09-26 21:44:04.414221',NULL,'Created purchase for INSAS Rifle 001 at Bhopal Base quantity 50'),(7,1,'CREATE_TRANSFER','TRANSFER',1,'2026-09-26 21:45:30.250430',NULL,'Transferred 20 of INSAS Rifle 001 from Bhopal Base to Indore Base'),(8,1,'CREATE_USER','USER',3,'2026-09-26 21:48:18.274966',NULL,'Created LOGISTICS_OFFICER account for rahul@example.com'),(9,1,'CREATE_ASSIGNMENT','ASSIGNMENT',1,'2026-09-26 21:49:49.057215',NULL,'Assigned 10 of INSAS Rifle 001 to rahul@example.com'),(10,1,'CREATE_EXPENDITURE','EXPENDITURE',1,'2026-09-26 21:52:06.487989',NULL,'Expended 5 of INSAS Rifle 001 for Training exercise'),(11,1,'CREATE_USER','USER',4,'2026-09-26 21:59:45.640083',NULL,'Created BASE_COMMANDER account for commander@test.com'),(12,4,'LOGIN','USER',4,'2026-09-26 22:00:09.834085',NULL,'Successful user login'),(13,1,'LOGIN','USER',1,'2026-09-26 22:05:27.876253',NULL,'Successful user login'),(14,1,'LOGIN','USER',1,'2026-09-26 22:10:26.290228',NULL,'Successful user login'),(15,1,'CREATE_USER','USER',5,'2026-09-26 22:11:44.819058',NULL,'Created LOGISTICS_OFFICER account for logistics@test.com'),(16,5,'LOGIN','USER',5,'2026-09-26 22:12:12.880890',NULL,'Successful user login');
/*!40000 ALTER TABLE `audit_logs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bases`
--

DROP TABLE IF EXISTS `bases`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bases` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `location` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `active` bit(1) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bases_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bases`
--

LOCK TABLES `bases` WRITE;
/*!40000 ALTER TABLE `bases` DISABLE KEYS */;
INSERT INTO `bases` VALUES (1,'Bhopal Base',' Bhopal','',_binary ''),(2,'Indore Base',' Indore','',_binary '');
/*!40000 ALTER TABLE `bases` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `equipment_types`
--

DROP TABLE IF EXISTS `equipment_types`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `equipment_types` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_equipment_types_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `equipment_types`
--

LOCK TABLES `equipment_types` WRITE;
/*!40000 ALTER TABLE `equipment_types` DISABLE KEYS */;
INSERT INTO `equipment_types` VALUES (1,'Assault Rifle','Standard military assault rifle');
/*!40000 ALTER TABLE `equipment_types` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `expenditures`
--

DROP TABLE IF EXISTS `expenditures`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `expenditures` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `base_id` bigint NOT NULL,
  `asset_id` bigint NOT NULL,
  `created_by` bigint NOT NULL,
  `quantity` int NOT NULL,
  `expenditure_date` date NOT NULL,
  `reason` varchar(255) NOT NULL,
  `reference` varchar(255) DEFAULT NULL,
  `notes` varchar(500) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_expenditures_asset` (`asset_id`),
  KEY `fk_expenditures_user` (`created_by`),
  KEY `ix_expenditures_base_date` (`base_id`,`expenditure_date`),
  CONSTRAINT `fk_expenditures_asset` FOREIGN KEY (`asset_id`) REFERENCES `assets` (`id`),
  CONSTRAINT `fk_expenditures_base` FOREIGN KEY (`base_id`) REFERENCES `bases` (`id`),
  CONSTRAINT `fk_expenditures_user` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`),
  CONSTRAINT `ck_expenditures_quantity` CHECK ((`quantity` > 0))
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `expenditures`
--

LOCK TABLES `expenditures` WRITE;
/*!40000 ALTER TABLE `expenditures` DISABLE KEYS */;
INSERT INTO `expenditures` VALUES (1,1,1,1,5,'2026-09-26','Training exercise','EXP-001','Training ammunition/equipment expenditure test','2026-09-26 21:52:06.385302');
/*!40000 ALTER TABLE `expenditures` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `flyway_schema_history`
--

DROP TABLE IF EXISTS `flyway_schema_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flyway_schema_history` (
  `installed_rank` int NOT NULL,
  `version` varchar(50) DEFAULT NULL,
  `description` varchar(200) NOT NULL,
  `type` varchar(20) NOT NULL,
  `script` varchar(1000) NOT NULL,
  `checksum` int DEFAULT NULL,
  `installed_by` varchar(100) NOT NULL,
  `installed_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `execution_time` int NOT NULL,
  `success` tinyint(1) NOT NULL,
  PRIMARY KEY (`installed_rank`),
  KEY `flyway_schema_history_s_idx` (`success`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flyway_schema_history`
--

LOCK TABLES `flyway_schema_history` WRITE;
/*!40000 ALTER TABLE `flyway_schema_history` DISABLE KEYS */;
INSERT INTO `flyway_schema_history` VALUES (1,'1','initial schema','SQL','V1__initial_schema.sql',-1032255620,'avnadmin','2026-09-26 11:53:28',662,1);
/*!40000 ALTER TABLE `flyway_schema_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `inventory`
--

DROP TABLE IF EXISTS `inventory`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `inventory` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `base_id` bigint NOT NULL,
  `asset_id` bigint NOT NULL,
  `total_quantity` int NOT NULL,
  `available_quantity` int NOT NULL,
  `assigned_quantity` int NOT NULL,
  `expended_quantity` int NOT NULL,
  `last_updated` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_inventory_base_asset` (`base_id`,`asset_id`),
  KEY `fk_inventory_asset` (`asset_id`),
  CONSTRAINT `fk_inventory_asset` FOREIGN KEY (`asset_id`) REFERENCES `assets` (`id`),
  CONSTRAINT `fk_inventory_base` FOREIGN KEY (`base_id`) REFERENCES `bases` (`id`),
  CONSTRAINT `ck_inventory_nonnegative` CHECK (((`total_quantity` >= 0) and (`available_quantity` >= 0) and (`assigned_quantity` >= 0) and (`expended_quantity` >= 0))),
  CONSTRAINT `ck_inventory_quantity_balance` CHECK ((`total_quantity` = (`available_quantity` + `assigned_quantity`)))
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `inventory`
--

LOCK TABLES `inventory` WRITE;
/*!40000 ALTER TABLE `inventory` DISABLE KEYS */;
INSERT INTO `inventory` VALUES (1,1,1,125,115,10,5,'2026-09-26 21:52:06.385302'),(2,2,1,20,20,0,0,'2026-09-26 21:45:30.205611');
/*!40000 ALTER TABLE `inventory` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `opening_balances`
--

DROP TABLE IF EXISTS `opening_balances`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `opening_balances` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `base_id` bigint NOT NULL,
  `asset_id` bigint NOT NULL,
  `created_by` bigint NOT NULL,
  `quantity` int NOT NULL,
  `effective_date` date NOT NULL,
  `created_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_opening_balances_base_asset` (`base_id`,`asset_id`),
  KEY `fk_opening_balances_asset` (`asset_id`),
  KEY `fk_opening_balances_user` (`created_by`),
  CONSTRAINT `fk_opening_balances_asset` FOREIGN KEY (`asset_id`) REFERENCES `assets` (`id`),
  CONSTRAINT `fk_opening_balances_base` FOREIGN KEY (`base_id`) REFERENCES `bases` (`id`),
  CONSTRAINT `fk_opening_balances_user` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`),
  CONSTRAINT `ck_opening_balances_quantity` CHECK ((`quantity` > 0))
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `opening_balances`
--

LOCK TABLES `opening_balances` WRITE;
/*!40000 ALTER TABLE `opening_balances` DISABLE KEYS */;
INSERT INTO `opening_balances` VALUES (1,1,1,1,100,'2026-09-26','2026-09-26 21:42:16.411644');
/*!40000 ALTER TABLE `opening_balances` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `purchases`
--

DROP TABLE IF EXISTS `purchases`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `purchases` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `base_id` bigint NOT NULL,
  `asset_id` bigint NOT NULL,
  `created_by` bigint NOT NULL,
  `quantity` int NOT NULL,
  `purchase_date` date NOT NULL,
  `reference_number` varchar(255) DEFAULT NULL,
  `vendor` varchar(255) DEFAULT NULL,
  `unit_cost` decimal(15,2) NOT NULL,
  `total_cost` decimal(15,2) NOT NULL,
  `notes` varchar(500) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_purchases_asset` (`asset_id`),
  KEY `fk_purchases_user` (`created_by`),
  KEY `ix_purchases_base_date` (`base_id`,`purchase_date`),
  CONSTRAINT `fk_purchases_asset` FOREIGN KEY (`asset_id`) REFERENCES `assets` (`id`),
  CONSTRAINT `fk_purchases_base` FOREIGN KEY (`base_id`) REFERENCES `bases` (`id`),
  CONSTRAINT `fk_purchases_user` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`),
  CONSTRAINT `ck_purchases_cost` CHECK (((`unit_cost` >= 0) and (`total_cost` >= 0))),
  CONSTRAINT `ck_purchases_quantity` CHECK ((`quantity` > 0))
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `purchases`
--

LOCK TABLES `purchases` WRITE;
/*!40000 ALTER TABLE `purchases` DISABLE KEYS */;
INSERT INTO `purchases` VALUES (1,1,1,1,50,'2026-09-26','','Bharat Defence Supplies',5000.00,250000.00,'','2026-09-26 21:44:04.299671');
/*!40000 ALTER TABLE `purchases` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `roles`
--

DROP TABLE IF EXISTS `roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `roles` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(40) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_roles_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `roles`
--

LOCK TABLES `roles` WRITE;
/*!40000 ALTER TABLE `roles` DISABLE KEYS */;
INSERT INTO `roles` VALUES (1,'ADMIN'),(2,'BASE_COMMANDER'),(3,'LOGISTICS_OFFICER');
/*!40000 ALTER TABLE `roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `transfers`
--

DROP TABLE IF EXISTS `transfers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `transfers` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `source_base_id` bigint NOT NULL,
  `destination_base_id` bigint NOT NULL,
  `asset_id` bigint NOT NULL,
  `created_by` bigint NOT NULL,
  `quantity` int NOT NULL,
  `transfer_date` date NOT NULL,
  `reference_number` varchar(255) DEFAULT NULL,
  `notes` varchar(500) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_transfers_asset` (`asset_id`),
  KEY `fk_transfers_user` (`created_by`),
  KEY `ix_transfers_source_date` (`source_base_id`,`transfer_date`),
  KEY `ix_transfers_destination_date` (`destination_base_id`,`transfer_date`),
  CONSTRAINT `fk_transfers_asset` FOREIGN KEY (`asset_id`) REFERENCES `assets` (`id`),
  CONSTRAINT `fk_transfers_destination_base` FOREIGN KEY (`destination_base_id`) REFERENCES `bases` (`id`),
  CONSTRAINT `fk_transfers_source_base` FOREIGN KEY (`source_base_id`) REFERENCES `bases` (`id`),
  CONSTRAINT `fk_transfers_user` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`),
  CONSTRAINT `ck_transfers_quantity` CHECK ((`quantity` > 0))
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `transfers`
--

LOCK TABLES `transfers` WRITE;
/*!40000 ALTER TABLE `transfers` DISABLE KEYS */;
INSERT INTO `transfers` VALUES (1,1,2,1,1,20,'2026-09-26','','','2026-09-26 21:45:30.206619');
/*!40000 ALTER TABLE `transfers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `first_name` varchar(255) NOT NULL,
  `last_name` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role_id` bigint NOT NULL,
  `base_id` bigint DEFAULT NULL,
  `enabled` bit(1) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_users_email` (`email`),
  KEY `fk_users_role` (`role_id`),
  KEY `fk_users_base` (`base_id`),
  CONSTRAINT `fk_users_base` FOREIGN KEY (`base_id`) REFERENCES `bases` (`id`),
  CONSTRAINT `fk_users_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'System','Administrator','admin@militaryasset.com','$2a$10$jTKhT9ta6FjzhjDYWGqNWulRoJKNCT1RSY5/Blup.YV2.gN2WXs2O',1,NULL,_binary ''),(2,'System','Administrator','admin@example.com','$2a$10$wansH7bYBZfjxNy4sD03m.p0QlL5JY5JVU6jIbNp0uFPjMSxme32K',1,NULL,_binary ''),(3,'Rahul ','Sharma','rahul@example.com','$2a$10$7GzKePYql5CgZ5HIH2utH.ypOrmBxfYxBIGS1Za58azzK8PN5PqE2',3,1,_binary ''),(4,'  Base ','Commander','commander@test.com','$2a$10$swoFGK9ebsruaN57j2Shg.juXuIOHIZJ3dhRls4a17gwWuKwD0/Ay',2,1,_binary ''),(5,'Logistics','Tester','logistics@test.com','$2a$10$zpYc3sV3pr2kcS2J7/pUGeA2fFljpH5XWXTZ/fg1a/p7.uTQAG2u2',3,1,_binary '');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
SET @@SESSION.SQL_LOG_BIN = @MYSQLDUMP_TEMP_LOG_BIN;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-09-26 23:15:43
