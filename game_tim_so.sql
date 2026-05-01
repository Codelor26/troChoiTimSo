-- MariaDB dump 10.19  Distrib 10.4.32-MariaDB, for Win64 (AMD64)
--
-- Host: localhost    Database: game_tim_so
-- ------------------------------------------------------
-- Server version	10.4.32-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `full_name` varchar(100) DEFAULT NULL,
  `gender` varchar(10) DEFAULT NULL,
  `dob` date DEFAULT NULL,
  `gold` int(11) DEFAULT 0,
  `avatar` varchar(255) DEFAULT NULL,
  `player_name` varchar(255) DEFAULT NULL,
  `light_skill` int(11) DEFAULT 0,
  `dark_skill` int(11) DEFAULT 0,
  `freeze_skill` int(11) DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'ytran@gmail.com','$2a$10$Dfo8jFEq59u7ZyM4uHBAWu55lzgtjmbaEmE.Syapj18G95tksGaXO','Tran Thi Nhu Y','Nu','2004-06-02',1000,NULL,NULL,0,0,0),(2,'admin123@gmail.com','$2a$10$OJkaQNBMbf0NF5HZ6l10gucIiEWUfci912JCE4GY5umt15y9Crhve','Admin','Nu','2002-04-05',9999,'/icon/Martin-Berube-Character-Devil.256.png',NULL,0,0,0),(3,'nhuy40794@gmail.com','$2a$10$jzvZs1ywWthwYf78uKQe1OuUXAG6K2QiRoEwq7b6FwQrTAAbHo8UO','NhuY','Female','2026-04-09',0,'/icon/Diversity-Avatars-Avatars-Trinity.512.png',NULL,0,0,0),(4,'codelor2604@gmail.com','$2a$10$uxL4JjG4qzAOI2z9s4chW.s42N/h1wfLl6NmUL8ayn/7Wt0VusJ52','Codelor','Female','2026-04-08',0,'/icon/Hopstarter-Superhero-Avatar-Avengers-Thor.256.png',NULL,0,0,0),(7,'a1234@gmail.com','$2a$10$ivMZLlDKhKTesX2JrPSBwOnYhHs8lhC2uDUJ0SY8y7Xsvki2LESG6','mori','Male','2026-04-01',0,'/icon/Diversity-Avatars-Avatars-Nikola-tesla.512.png',NULL,0,0,0),(8,'codelor260404@gmail.com','$2a$10$4iTGUJ1NRpOR1Dammuir4OSBrVG.wO2W4Mx/34UtAGrb6ySWrsd/K','Codelordethuong','Female','2004-06-02',0,'/icon/Diversity-Avatars-Avatars-Native-woman.512.png',NULL,0,0,0),(14,'bonhau123@gmail.com','$2a$10$1H3nzBnJxln1NrD42jQTxeDloFAS10F/5zVX9OetA1XhcfQ9RlNZe','Ta Chinh','Male','2026-04-01',2900,'/icon/Hopstarter-Superhero-Avatar-Avengers-Nick-Fury.256.png','Ngon Chinh',1,2,2),(15,'binhAn170302@gmail.com','$2a$10$.aj3dRrzd8cEzVjMFxYgEuzvspm0gLpi6m2Da2F8D1PnQ8K2eES9W','Duong Binh An','female','2002-03-17',4400,'/icon/Martin-Berube-Character-Devil.256.png','Hen',0,0,1),(16,'teboncung123@gmail.com','$2a$10$S04HgVHW2AhvziYxljgiH.7R2Vp3HRRarHJNDkl398IsrIn7T6jmG','Te Bon Cung','female','2026-04-04',0,'/icon/Diversity-Avatars-Avatars-Dave-grohl.512.png','BonCung',0,0,0),(17,'bonCung123@gmail.com','$2a$10$YaSSGZteTCgRqAnSNeKoROqtGgVhq5D8CNV/31tknBGQ0XLjKab4e','Te Bon Cung','female','2026-04-08',0,'/icon/Hopstarter-Superhero-Avatar-Avengers-Thor.256.png','Te Xu',0,0,0),(18,'tephien123@gmail.com','$2a$10$89/uvCPWpwbbhzEpwjoRKu0kEk04FyQBIC8v/63/Rf2vKYnyjyudy','Te Phien','male','2026-04-06',0,'/icon/Hopstarter-Superhero-Avatar-Avengers-Giant-Man.256.png','TePhien',0,0,0),(19,'hongngoc123@gmail.com','$2a$10$6siPTuX5ljxvdGJXlKzmPuXbGqcDUnIOnLhkSzb1b5DmoOdx9FZAu','Ta Hong Ngoc','female','2026-04-05',0,'/icon/Iconarchive-Incognito-Animals-Raccoon-Avatar.512.png','NgocCuTe',0,0,0),(20,'tulong123@gmail.com','$2a$10$rAvojCVe5C8UNTV/2KRDoOf1hsWGJIf1h/s.ylcdrSQxMCi8yQ7LC','Trieu Tu Long','Male','2026-03-03',0,'/icon/Hopstarter-Superhero-Avatar-Avengers-Giant-Man.256.png','Trieu Van',0,0,0);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-01 13:09:37
