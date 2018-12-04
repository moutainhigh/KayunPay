/*Table structure for table `t_yistage_trace` */

DROP TABLE IF EXISTS `t_yistage_trace`;

CREATE TABLE `t_yistage_trace` (
  `traceCode` char(32) NOT NULL,
  `uid` int(11) NOT NULL AUTO_INCREMENT,
  `requestAction` char(50) DEFAULT NULL,
  `requestMessage` text,
  `responseMessage` text,
  `traceDate` char(8) DEFAULT NULL,
  `traceTime` char(6) DEFAULT NULL,
  `updateDate` char(8) DEFAULT NULL,
  `updateTime` char(6) DEFAULT NULL,
  PRIMARY KEY (`uid`,`traceCode`)
) ENGINE=InnoDB AUTO_INCREMENT=89 DEFAULT CHARSET=utf8mb4;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;