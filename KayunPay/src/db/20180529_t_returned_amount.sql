CREATE TABLE `t_returned_amount` (
  `userCode` char(32) NOT NULL,
  `uid` int(11) NOT NULL AUTO_INCREMENT,
  `userName` varchar(48) DEFAULT NULL,
  `amount` bigint(11) NOT NULL,
  `updateTime` char(14) NOT NULL,
  `createDateTime` char(14) NOT NULL,
  `traceCode` char(32) DEFAULT NULL,
  `state` int(1) DEFAULT NULL,
  `type` int(1) DEFAULT NULL,
  PRIMARY KEY (`uid`,`userCode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

