/*
Navicat MySQL Data Transfer

Source Server         : myfirst
Source Server Version : 50714
Source Host           : localhost:3306
Source Database       : yrhx20160405

Target Server Type    : MYSQL
Target Server Version : 50714
File Encoding         : 65001

Date: 2017-11-28 17:07:01
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `t_redeem`
-- ----------------------------
DROP TABLE IF EXISTS `t_redeem`;
CREATE TABLE `t_redeem` (
  `uid` int(11) NOT NULL AUTO_INCREMENT,
  `redeemCode` char(12) NOT NULL,
  `type` char(4) NOT NULL,
  `rName` char(20) DEFAULT NULL,
  `createDateTime` char(14) DEFAULT NULL,
  `expDate` char(8) DEFAULT NULL,
  `rState` char(4) NOT NULL,
  `useDateTime` char(14) DEFAULT NULL,
  `userCode` char(40) DEFAULT NULL,
  `remark` char(100) DEFAULT NULL,
  PRIMARY KEY (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_redeem
-- ----------------------------
