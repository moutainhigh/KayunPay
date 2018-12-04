/*
Navicat MySQL Data Transfer

Source Server         : myfirst
Source Server Version : 50714
Source Host           : localhost:3306
Source Database       : yrhx20160405

Target Server Type    : MYSQL
Target Server Version : 50714
File Encoding         : 65001

Date: 2018-03-14 14:10:43
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `t_leave_note`
-- ----------------------------
DROP TABLE IF EXISTS `t_leave_note`;
CREATE TABLE `t_leave_note` (
  `lid` bigint(20) NOT NULL AUTO_INCREMENT,
  `leaveNoteCode` char(32) NOT NULL,
  `userCode` char(32) NOT NULL,
  `userName` varchar(40) DEFAULT NULL,
  `userTrueName` varchar(40) DEFAULT NULL,
  `userMobile` char(40) NOT NULL,
  `leaveNote` varchar(1000) DEFAULT NULL,
  `addDateTime` char(14) DEFAULT NULL,
  `rMark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`lid`,`leaveNoteCode`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_leave_note
-- ----------------------------
INSERT INTO `t_leave_note` VALUES ('1', 'ebcc512aa5174eec93abdc9eb8e345d7', '411caa0eecb14ea5b8378201d558501a', '沐雨清颦', '王舜', '994d950b32eeb91f8abbe85dfe2ecdee', '啦啦啦德玛西亚', '20180308120119', null);
