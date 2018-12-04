CREATE TABLE `t_jx_trace` (
  `jxTraceCode` char(20) NOT NULL COMMENT '江西银行交易编号',
  `version` char(2) NOT NULL COMMENT '版本号',
  `instCode` char(8) NOT NULL COMMENT '机构代码',
  `bankCode` char(8) NOT NULL COMMENT '银行代码',
  `txDate` char(8) NOT NULL COMMENT '交易日期',
  `txTime` char(6) NOT NULL COMMENT '交易时间',
  `seqNo` char(6) NOT NULL COMMENT '交易流水号',
  `txCode` varchar(50) NOT NULL COMMENT '交易代码',
  `requestChannel` char(6) DEFAULT NULL COMMENT '请求交易渠道(000001手机APP,000002网页,000003微信,000004柜面)',
  `responseChannel` char(6) DEFAULT NULL COMMENT '响应交易渠道(000001手机APP,000002网页,000003微信,000004柜面)',
  `requestSign` varchar(600) DEFAULT NULL COMMENT '请求签名',
  `responseSign` varchar(600) DEFAULT NULL COMMENT '响应签名',
  `retCode` varchar(20) DEFAULT NULL COMMENT '响应代码',
  `retMsg` varchar(60) DEFAULT NULL COMMENT '响应描述',
  `requestMessage` text COMMENT '请求报文',
  `responseMessage` text COMMENT '响应报文',
  `acqRes` varchar(200) DEFAULT NULL COMMENT '请求方保留(注：查询接口无此字段)',
  `remark` varchar(1000) DEFAULT NULL COMMENT '备注信息',
  PRIMARY KEY (`jxTraceCode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

