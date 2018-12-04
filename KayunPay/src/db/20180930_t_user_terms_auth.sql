/*
Navicat MySQL Data Transfer

Source Server         : 192.168.2.53_3306
Source Server Version : 50719
Source Host           : 192.168.2.53:3306
Source Database       : yrhx20170905

Target Server Type    : MYSQL
Target Server Version : 50719
File Encoding         : 65001

Date: 2018-09-30 14:23:25
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for t_user_terms_auth
-- ----------------------------
DROP TABLE IF EXISTS `t_user_terms_auth`;
CREATE TABLE `t_user_terms_auth` (
  `uid` int(11) NOT NULL AUTO_INCREMENT,
  `userCode` char(32) NOT NULL COMMENT '用户编号',
  `userName` char(48) DEFAULT NULL COMMENT '用户名',
  `jxAccountId` char(20) DEFAULT NULL COMMENT '江西银行存管账号',
  `autoBid` char(1) DEFAULT NULL COMMENT '自动投标签约 0-未开通；1-已开通',
  `autoCredit` char(1) DEFAULT NULL COMMENT '自动债转签约 0-未开通；1-已开通',
  `paymentAuth` char(1) DEFAULT NULL COMMENT '缴费授权签约 0-未开通；1-已开通',
  `repayAuth` char(1) DEFAULT NULL COMMENT '还款授权签约 0-未开通；1-已开通',
  `autoBidMaxAmt` bigint(11) DEFAULT NULL COMMENT '自动投标签约最高金额 单位：分',
  `autoBidDeadline` char(8) DEFAULT NULL COMMENT '自动投标签约到期日 yyyyMMdd',
  `autoCreditMaxAmt` bigint(11) DEFAULT NULL COMMENT '自动债转签约最高金额 单位：分',
  `autoCreditDeadline` char(8) DEFAULT NULL COMMENT '自动债转签约到期日 yyyyMMdd',
  `paymentMaxAmt` bigint(11) DEFAULT NULL COMMENT '缴费授权签约最高金额 单位：分',
  `paymentDeadline` char(8) DEFAULT NULL COMMENT '缴费授权签约到期日 yyyyMMdd',
  `repayMaxAmt` bigint(11) DEFAULT NULL COMMENT '还款授权签约最高金额 单位：分',
  `repayDeadline` char(8) DEFAULT NULL COMMENT '还款授权签约到期日 yyyyMMdd',
  `authDateTime` char(14) DEFAULT NULL COMMENT '设置时间 yyyyMMddHHmmss',
  `updateDateTime` char(14) DEFAULT NULL COMMENT '更新时间 yyyyMMddHHmmss',
  `channel` char(6) DEFAULT NULL COMMENT '设置渠道 000001-移动端；000002-PC端',
  PRIMARY KEY (`uid`,`userCode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
