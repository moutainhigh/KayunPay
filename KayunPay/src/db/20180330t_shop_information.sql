/*
SQLyog 企业版 - MySQL GUI v8.14 
MySQL - 5.7.19 : Database - yrhx20170905
*********************************************************************
*/


/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

/*Table structure for table `t_shop_information` */

DROP TABLE IF EXISTS `t_shop_information`;

CREATE TABLE `t_shop_information` (
  `shopId` int(8) NOT NULL AUTO_INCREMENT,
  `shopNum` varchar(24) NOT NULL COMMENT '门店代码',
  `shopContent` varchar(2048) DEFAULT NULL COMMENT 'json门店的相关信息',
  `addDateTime` varchar(14) DEFAULT NULL,
  `upDateTime` varchar(14) DEFAULT NULL,
  PRIMARY KEY (`shopId`),
  UNIQUE KEY `shopNum` (`shopNum`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
