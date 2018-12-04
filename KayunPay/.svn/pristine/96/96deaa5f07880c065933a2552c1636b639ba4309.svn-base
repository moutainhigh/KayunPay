/*
Navicat MySQL Data Transfer

Source Server         : 53
Source Server Version : 50719
Source Host           : 192.168.2.53:3306
Source Database       : yrhx20170905

Target Server Type    : MYSQL
Target Server Version : 50719
File Encoding         : 65001

Date: 2018-10-26 13:30:43
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `t_transfer_way`
-- ----------------------------
DROP TABLE IF EXISTS `t_transfer_way`;
CREATE TABLE `t_transfer_way` (
  `uid` int(11) NOT NULL AUTO_INCREMENT,
  `userCode` char(40) NOT NULL,
  `month` char(1) NOT NULL,
  `qtr` char(1) NOT NULL,
  `half` char(1) NOT NULL,
  `offline` char(1) NOT NULL,
  `normal` char(1) NOT NULL,
  `setDateTime` char(14) NOT NULL,
  `updateDateTime` char(14) DEFAULT NULL,
  `remark` text,
  PRIMARY KEY (`uid`,`userCode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Records of t_transfer_way
-- ----------------------------
