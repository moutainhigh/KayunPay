/*
Navicat MySQL Data Transfer

Source Server         : standard
Source Server Version : 50623
Source Host           : sh-cdb-n0fi192c.sql.tencentcdb.com:63998
Source Database       : yrhxdb

Target Server Type    : MYSQL
Target Server Version : 50623
File Encoding         : 65001

Date: 2018-02-05 13:58:00
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for t_questionnaire_records
-- ----------------------------
DROP TABLE IF EXISTS `t_questionnaire_records`;
CREATE TABLE `t_questionnaire_records` (
  `tid` int(11) NOT NULL AUTO_INCREMENT,
  `userCode` char(32) NOT NULL,
  `userName` char(48) DEFAULT NULL,
  `surveyRecord` varchar(1000) DEFAULT NULL,
  `surveyResult` char(1) DEFAULT NULL,
  `addDateTime` char(14) DEFAULT NULL,
  PRIMARY KEY (`tid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
