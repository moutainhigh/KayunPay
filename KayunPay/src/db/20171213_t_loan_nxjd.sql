SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for t_loan_nxjd
-- ----------------------------
DROP TABLE IF EXISTS `t_loan_nxjd`;
CREATE TABLE `t_loan_nxjd` (
  `uid` int(11) NOT NULL AUTO_INCREMENT,
  `loanCode` char(32) NOT NULL DEFAULT '',
  `ztyLoanCode` varchar(50) NOT NULL DEFAULT '',
  `loanAmount` bigint(11) DEFAULT NULL,
  `term` int(5) DEFAULT NULL,
  `status` char(1) DEFAULT NULL,
  `addDate` char(8) DEFAULT NULL,
  `addTime` char(6) DEFAULT NULL,
  `userName` varchar(48) DEFAULT '',
  `userCardId` char(48) DEFAULT NULL,
  `updateDate` char(8) DEFAULT NULL,
  `updateTime` char(6) DEFAULT NULL,
  PRIMARY KEY (`uid`,`ztyLoanCode`)
) ENGINE=InnoDB AUTO_INCREMENT=323 DEFAULT CHARSET=utf8mb4;
