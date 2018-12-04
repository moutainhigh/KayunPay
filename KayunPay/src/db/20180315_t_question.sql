/*
Navicat MySQL Data Transfer

Source Server         : myfirst
Source Server Version : 50714
Source Host           : localhost:3306
Source Database       : yrhx20160405

Target Server Type    : MYSQL
Target Server Version : 50714
File Encoding         : 65001

Date: 2018-03-14 14:09:52
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `t_question`
-- ----------------------------
DROP TABLE IF EXISTS `t_question`;
CREATE TABLE `t_question` (
  `qid` bigint(20) NOT NULL AUTO_INCREMENT,
  `qCode` char(32) NOT NULL,
  `qType` char(2) DEFAULT NULL,
  `keyWord` varchar(100) DEFAULT NULL,
  `question` varchar(200) DEFAULT NULL,
  `answer` varchar(2000) DEFAULT NULL,
  `creatDateTime` char(14) DEFAULT NULL,
  `updateDateTime` char(14) DEFAULT NULL,
  `tipNum` bigint(20) DEFAULT NULL,
  `solveNum` bigint(20) DEFAULT NULL,
  `creatUserName` varchar(20) DEFAULT NULL,
  `creatUserCode` char(32) DEFAULT NULL,
  `isUsually` char(1) DEFAULT NULL,
  `qMark` char(200) DEFAULT NULL,
  PRIMARY KEY (`qid`,`qCode`)
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_question
-- ----------------------------
INSERT INTO `t_question` VALUES ('1', '481c894452f543618d603793781de880', 'F', '提现 费用', '提现费用如何收取？', '<p class=\"MsoNormal\">\n	<span>根据用户级别不同，每月有相应的免费提现次数。超过免费提现次数的，银行收费</span>2元/笔（200可用积分可抵扣提现一次）。\n</p>', '20180305124349', '20180305124349', '0', '0', '王舜', '2a61b57cc96543918f81f3bb7b144c56', 'Y', null);
INSERT INTO `t_question` VALUES ('2', '26a16e23aee2495c97a4cc3f6d37c38c', 'F', '提现 额度 提现额度', '什么是免费提现额度？提现什么时候到账？', '<p class=\"MsoNormal\">\n	<span>免费提现额度</span>=已回收本息+待回收本金 -提现总额；充值未投标的金额，提现将收取1%服务费。提现实时到账（实际到账时间以银行为准）。\n</p>', '20180305124559', '20180305124559', '0', '0', '王舜', '2a61b57cc96543918f81f3bb7b144c56', 'Y', null);
INSERT INTO `t_question` VALUES ('3', 'faced858f0c342a08bedee06934a25dc', 'F', '充值 失败 充值失败', '为什么会充值失败？', '<p class=\"MsoNormal\" style=\"margin-left:22pt;text-indent:-22pt;\">\n	1.是否安装安全控件，安装成功后需重启浏览器\n</p>\n<p class=\"MsoNormal\" style=\"margin-left:22pt;text-indent:-22pt;\">\n	2.目前平台只支持储蓄卡充值\n</p>\n<p class=\"MsoNormal\" style=\"margin-left:22pt;text-indent:-22pt;\">\n	3.网上支付限额或者银行卡余额不足\n</p>\n<p class=\"MsoNormal\" style=\"margin-left:22pt;text-indent:-22pt;\">\n	4.您可以重启电脑或者重新登陆APP再次尝试，如有问题通过人工客服发送操作\n</p>', '20180305124833', '20180305124833', '0', '0', '王舜', '2a61b57cc96543918f81f3bb7b144c56', 'Y', null);
INSERT INTO `t_question` VALUES ('4', 'cf56ce3d3bfe46fd841970fa4b250959', 'F', '充值 提现 验证码', '充值/提现收不到验证码？', '<p class=\"MsoNormal\">\n	<span>请确认手机是否安装短信拦截或过滤软件；请确认手机是否能够正常接收短信（信号问题、欠费、停机等）；短信收发过程中可能会存在延迟，请耐心等待；验证码获取后</span>10分钟内输入都有效，请勿频繁点击。您还可以联系客服400-027-0707。\n</p>', '20180305124909', '20180305124909', '0', '0', '王舜', '2a61b57cc96543918f81f3bb7b144c56', 'Y', null);
INSERT INTO `t_question` VALUES ('5', '8ecc880d755144cd95b9671d044d55ea', 'A', '注册', '注册有哪些条件？', '<p class=\"MsoNormal\">\n	<span>中华人民共和国公民（不包括中国香港、澳门及台湾地区）</span> <span>年龄</span>18-75周岁、具有完全民事行为能力的自然人。\n</p>', '20180305124956', '20180305124956', '0', '0', '王舜', '2a61b57cc96543918f81f3bb7b144c56', 'N', null);
INSERT INTO `t_question` VALUES ('6', 'db0c0e2c181c48e1ba1a6023deb99a63', 'A', '验证码', '手机收不到验证码怎么办？', '<p class=\"MsoNormal\">\n	<span>请确认手机是否安装短信拦截或过滤软件；请确认手机是否能够正常接收短信（信号问题、欠费、停机等）；短信收发过程中可能会存在延迟，请耐心等待；验证码获取后</span>10分钟内输入都有效，请勿频繁点击。您还可以联系客服400-027-0707。\n</p>', '20180305125124', '20180305125124', '0', '0', '王舜', '2a61b57cc96543918f81f3bb7b144c56', 'N', null);
INSERT INTO `t_question` VALUES ('7', 'f4d16e27bd8a4eafba56c01ddd620076', 'A', '注册 证件号 手机号', '为什么注册时提示证件号或手机号已存在？', '<p class=\"MsoNormal\">\n	<span>身份证号或手机号已被注册，不能重复使用。如已注册但忘记了注册信息，请联系客服</span>400-027-0707核实身份信息后可找回账号。\n</p>', '20180305125205', '20180305125219', '0', '0', '王舜', '2a61b57cc96543918f81f3bb7b144c56', 'N', null);
INSERT INTO `t_question` VALUES ('8', '1411c98a9933431b805a3b68b0a0ceb5', 'B', '存管账户 投资', '不开通银行存管账户能进行投资吗？', '<p class=\"MsoNormal\">\n	不可以。银行存管子账户用于分账管理用户的交易与资金信息，如不开户将无法进行后续的操作。\n</p>', '20180305125324', '20180305125324', '0', '0', '王舜', '2a61b57cc96543918f81f3bb7b144c56', 'N', null);
INSERT INTO `t_question` VALUES ('9', '9d3f58498b5941b38f801589693fbf45', 'B', '存管账户 密码', '银行存管账户交易密码和银行卡交易密码有关系吗？', '<p class=\"MsoNormal\">\n	<span>没有关系。您通过</span>www.yrhx.com平台进入银行存管系统设置的银行存管账户交易密码为您在www.yrhx.com平台进行投资时的资金交易密码，用于您在使用银行存管子账户内资金时的鉴权，而您的银行卡，是您独立至银行开立的实体银行账户，与您在平台的存管子账户并非同一账户。\n</p>', '20180305125404', '20180305125642', '0', '0', '王舜', '2a61b57cc96543918f81f3bb7b144c56', 'N', null);
INSERT INTO `t_question` VALUES ('10', '77cf359caf2a4b55a688b8f2172a1dfc', 'B', '开通 存管', '如何开通银行存管账户？', '<p class=\"MsoNormal\">\n	注册成功后需开通银行存管账户，存管账户开通需提供真实姓名、身份证号，并设置银行存管账户交易密码。\n</p>\n<p class=\"MsoNormal\">\n	<span>前往</span>“我的账户”——“账户管理”——“安全中心”开通\n</p>\n<p class=\"MsoNormal\">\n	<p class=\"MsoNormal\" style=\"background:#FCFCFC;\">\n		<span>注意事项：</span>1.一个身份证、手机信息只能注册一个账户，请勿重复注册。\n	</p>\n	<p class=\"MsoNormal\" style=\"text-indent:55pt;background:#FCFCFC;\">\n		2.绑定银行卡必须是与身份认证一致户名的储蓄卡。\n	</p>\n	<p class=\"MsoNormal\" style=\"text-indent:55pt;background:#FCFCFC;\">\n		3.开通存管账户手机号与绑定银行卡预留手机号必须一致。\n	</p>\n	<p class=\"MsoNormal\" style=\"text-indent:55pt;background:#FCFCFC;\">\n		4.认证身份证年龄须在18-75岁才能有效开通存管账户。\n	</p>\n</p>', '20180305125751', '20180305125751', '0', '0', '王舜', '2a61b57cc96543918f81f3bb7b144c56', 'N', null);
INSERT INTO `t_question` VALUES ('11', '756415b67b534ca0a262b26c3c2e5e33', 'C', '借款标 起投金额', '借款标的起投金额？', '<p class=\"MsoNormal\" style=\"background:#FCFCFC;\">\n	<span>新手标</span>100起投，其它标50元起投。\n</p>', '20180305125919', '20180305125919', '0', '0', '王舜', '2a61b57cc96543918f81f3bb7b144c56', 'N', null);
INSERT INTO `t_question` VALUES ('12', '2ba72401cbe242b59b42699329c3207f', 'C', '新手标', '新手标投资规则？', '<p class=\"MsoNormal\">\n	1-6月标随机每天发出，活跃积分低于30的用户可参与，仅限手动，同一个标内可分多次投资，100起投，限额10000元。（当天新手标未满的情况下，晚上9点半对所有用户开放。）\n</p>', '20180305125956', '20180305125956', '0', '0', '王舜', '2a61b57cc96543918f81f3bb7b144c56', 'N', null);
INSERT INTO `t_question` VALUES ('13', '645e49fc1950444fa096edcfd60dd64d', 'C', '发标时间', '平台发标时间？', '<p class=\"MsoNormal\">\n	<span>早上</span>9点到晚上8点之间整点发标，一般会提前1个小时进行预告。\n</p>', '20180305130030', '20180305130030', '0', '0', '王舜', '2a61b57cc96543918f81f3bb7b144c56', 'N', null);
INSERT INTO `t_question` VALUES ('14', 'ba86498a6932411ba61a15c91156a20d', 'C', '自动 手动 占比 投标', '自动和手动的占比是多少？', '<p class=\"MsoNormal\">\n	<span>除新手标外，每个借款标自动投标占比</span>80%，手动投标占比20%，开启自动投标后，仍可采用手动投标操作投资同一标或不同标。\n</p>', '20180305130104', '20180305130104', '0', '0', '王舜', '2a61b57cc96543918f81f3bb7b144c56', 'N', null);
INSERT INTO `t_question` VALUES ('15', 'ffa2b4a004654fc8af6832a0b7d27122', 'C', '自动投标 限额', '自动投标限额方式？', '<p class=\"MsoNormal\">\n	a.标的金额 ≤ 10万 限额 1 万；\n</p>\n<p class=\"MsoNormal\">\n	b.标的金额＞10万 限额 2 万；\n</p>\n<p class=\"MsoNormal\">\n	&nbsp;<span>备注：单标手动投标不限额。</span>\n</p>', '20180305130135', '20180305130135', '0', '0', '王舜', '2a61b57cc96543918f81f3bb7b144c56', 'N', null);
INSERT INTO `t_question` VALUES ('16', '0aaaccb4cf764509b0f777ed70fed6e8', 'C', '自动投标 设置', '自动投标金额设置？', '<p class=\"MsoNormal\">\n	<span>如果想要账户有多少金额投多少，建议设置最小投标金额</span>50.（剩余可投金额小于账户余额，会有只投出部分金额的可能）\n</p>\n<p class=\"MsoNormal\">\n	<span>如果想要使用现金券建议最小投标金额设置为</span>5000，或者10000等满足现金券条件的金额.\n</p>\n<p class=\"MsoNormal\">\n	如果想要每笔投资固定的金额，建议设置最小投标金额和最大投标金额一致！\n</p>', '20180305130212', '20180305130532', '0', '0', '王舜', '2a61b57cc96543918f81f3bb7b144c56', 'N', null);
INSERT INTO `t_question` VALUES ('17', '10f2c7c011b44a8d97a5b13fe39a2cba', 'C', '自动投标 时间', '自动投标开启时间？', '<p class=\"MsoNormal\">\n	<span>自动投标在开标前</span>15分钟开启，当开标十分钟后标未满将开启第二轮自动。\n</p>', '20180305130245', '20180305130245', '0', '0', '王舜', '2a61b57cc96543918f81f3bb7b144c56', 'N', null);
INSERT INTO `t_question` VALUES ('18', '35c1701f723c4fe8843bb9c236b1131a', 'C', '自动投标 排名', '自动投标设置如何保留排名？', '<p class=\"MsoNormal\">\n	<span>账户余额</span>≥50元，期限设置和还款方式不匹配时，该借款标会顺延向下寻找符合设置条件的用户，您的排名保留。\n</p>\n<p class=\"MsoNormal\">\n	<p class=\"MsoNormal\">\n		<span>借款标设有投资金额上限，自动投标满足设置成功投入后，则无论账户余额是否大于</span>50元，都将重新排队。<span style=\"line-height:1.5;\"></span>\n	</p>\n</p>', '20180305130313', '20180305130333', '0', '0', '王舜', '2a61b57cc96543918f81f3bb7b144c56', 'N', null);
INSERT INTO `t_question` VALUES ('19', '5d382e72b5d8459da3b13ba6dad113f8', 'C', '自动投标 设置修改 重新排队', '自动投标设置修改是否需要重新排队？', '<p class=\"MsoNormal\">\n	修改期限、还款方式将重新排名，修改金额、投资券设置不重新排名。\n</p>', '20180305130513', '20180305130513', '0', '0', '王舜', '2a61b57cc96543918f81f3bb7b144c56', 'N', null);
INSERT INTO `t_question` VALUES ('20', '84b4eb7ef33441499a8a52d98b02d646', 'D', '债权转让', '什么时候可以债权转让？', '<p class=\"MsoNormal\">\n	<span>满标开始计息后就可以进行债权转让（可二次转让）。自提交申请成功后，若</span>72小时内无人承接，则此次债权转让失败。24小时之后，债权人可再次提交债权转让申请。\n</p>', '20180305130614', '20180305130614', '0', '0', '王舜', '2a61b57cc96543918f81f3bb7b144c56', 'N', null);
INSERT INTO `t_question` VALUES ('21', 'f8d76eebb87c4224a3442de716b3694a', 'D', '债权转让 积分', '债权转让的积分如何算？', '<p class=\"MsoNormal\">\n	按天计算，发布债权转让日之后的积分归承接人所有，若债权人积分不足，则无法转让；\n</p>', '20180305130648', '20180305130648', '0', '0', '王舜', '2a61b57cc96543918f81f3bb7b144c56', 'N', null);
INSERT INTO `t_question` VALUES ('22', '28f0a3e1154b43a1aa2ff72974219010', 'D', '债权转让 让利金额 承接收益', '债权转让的让利金额/承接收益是什么意思？', '<p class=\"MsoNormal\">\n	<span>转让人可依据自愿原则，自行决定是否设置承接奖励。让利金额由转让人自行承担。若设置承接奖励，额度不可超过债权剩余本金的</span>10%（即让利额度范围为0%~10%）。\n</p>', '20180305130921', '20180305130921', '0', '0', '王舜', '2a61b57cc96543918f81f3bb7b144c56', 'N', null);
INSERT INTO `t_question` VALUES ('23', '5fd5441626ee4a228f88934f586a4ebb', 'D', '债权转让 收费', '债权转让如何收费？', '<p class=\"MsoNormal\">\n	<span>每达成一笔债权转让操作，平台将收取转让人剩余本金的</span>0.5%作为服务费。\n</p>', '20180305130959', '20180305130959', '0', '0', '王舜', '2a61b57cc96543918f81f3bb7b144c56', 'N', null);
INSERT INTO `t_question` VALUES ('24', '788ca674f4fd4b408969641720f88f55', 'D', '债权转让 承接价格 剩余本金', '为什么债权转让的承接价格高出剩余本金？', '<p class=\"MsoNormal\">\n	因为获得的利息是按照持有的天数算的，所以由承接人把转让人持有那几天应得的利息进行了先行垫付，到了相应的回款时间，承接人收到一整个月的利息，转让当天的利息是归承接人所有的。\n</p>', '20180305131047', '20180305131047', '0', '0', '王舜', '2a61b57cc96543918f81f3bb7b144c56', 'N', null);
INSERT INTO `t_question` VALUES ('25', '6e7bd13b4b91467da5fd9764e16c9169', 'D', '债权转让 加息券 现金券', '当发生债权转让时，加息券，现金券是否扣除？', '<p class=\"MsoNormal\">\n	&nbsp; ①.加息券投资发生债权转让时，转让人当期回款月起不再享受加息，债权承接人亦不享受加息部分。（正在进行中的双倍额度加息1%活动同理，如发生债转，则当期回款月起不再享受加息）。<br />\n&nbsp;&nbsp;②.平台活动所赠送的现金券，在发生债权转让时，将被扣除。（积分兑换的现金券不扣除）。\n</p>', '20180305131124', '20180305131124', '0', '0', '王舜', '2a61b57cc96543918f81f3bb7b144c56', 'N', null);
INSERT INTO `t_question` VALUES ('26', 'c28cea377edb4e6e90eab5fbad79f6f3', 'E', '修改 银行卡', '如何修改银行卡？', '<p class=\"MsoNormal\">\n	未开通银行存管<span>的用户，用您绑定的邮箱号发送：真实姓名，用户名，手机号，新卡号，新卡开户行（新手机号）和原银行卡号，更改原因，发送邮件至客服邮箱：</span>kf@yrhx.com，客服收到核实后会与您更改。温馨提示您:客户需要更改信息，我们会给您电话核实之后才能修改，所以还请注意接听电话！\n</p>\n<p class=\"MsoNormal\">\n	<p class=\"MsoNormal\">\n		已开通银行存管<span>的用户，可点击</span>“我的账户”-“账户管理”-“安全中心”，自行更换绑定银行卡（点击未签约提供相关材料T+1更换成功，如未成功联系客服查询原因）。\n	</p>\n</p>', '20180305131723', '20180305131738', '0', '0', '王舜', '2a61b57cc96543918f81f3bb7b144c56', 'N', null);
INSERT INTO `t_question` VALUES ('27', '73eafd6b08094c98916635b403665226', 'E', '修改 手机号', '如何修改手机号？', '<p class=\"MsoNormal\">\n	未开通银行存管<span>的用户，可点击</span>“我的账户”-“账户管理”-“安全中心”-“修改平台手机号”自行修改。\n</p>\n<p class=\"MsoNormal\">\n	已开通银行存管<span>的用户，可点击</span>“我的账户”-“账户管理”-“安全中心”-“修改存管手机号”自行修改。\n</p>', '20180305132105', '20180305132105', '0', '0', '王舜', '2a61b57cc96543918f81f3bb7b144c56', 'N', null);
INSERT INTO `t_question` VALUES ('28', 'bd98846e1dba45edbf5a0fed0e63660e', 'E', '支付密码', '支付密码的区别？', '<p class=\"MsoNormal\">\n	1.存管支付密码，提现时需要；（首次提现默认手机号后6位，重新设置由不少于8位的字母和数字混合组成）\n</p>\n<p class=\"MsoNormal\">\n	2.平台支付密码，债权转让会需要\n</p>\n<p class=\"MsoNormal\">\n	<span>注：如密码锁定可凌晨自动解锁或者可点击</span>“我的账户”-“账户管理”-“安全中心”，自行修改存管支付密码与平台支付密码。\n</p>', '20180305132132', '20180305132132', '0', '0', '王舜', '2a61b57cc96543918f81f3bb7b144c56', 'N', null);
INSERT INTO `t_question` VALUES ('29', '026d5407d1904533900f62c542a1facf', 'E', '可用积分 活跃积分', '平台可用积分与活跃积分的区别？', '<p class=\"MsoNormal\">\n	活跃积分：用于提升账户等级，不可使用；\n</p>\n<p class=\"MsoNormal\">\n	<span>可用积分：顾名思义，可以使用，使用后将扣除相应的可用积分。目前可以抵扣提现手续费</span>200积分抵扣2元提现手续费；可以在积分商城兑换商品。\n</p>', '20180305132223', '20180305132223', '0', '0', '王舜', '2a61b57cc96543918f81f3bb7b144c56', 'N', null);
INSERT INTO `t_question` VALUES ('30', '3eb31bfcd20b41c7a28133e71aaeb44f', 'E', '获取 积分', '如何获得积分？', '<p class=\"MsoNormal\">\n	1.有效投资100元获得1积分，低于100元的投资部分不能得到积分。<br />\n2.投标积分是根据投标期限，还款方式的不同，有所不同的，具体请查看帮助中心-会员积分-Q2如何获得积分？有详细列表\n</p>', '20180305132841', '20180305133334', '0', '0', '王舜', '2a61b57cc96543918f81f3bb7b144c56', 'N', null);
INSERT INTO `t_question` VALUES ('31', 'dacce1372efe4f108a8743a643d6cc91', 'E', '利息管理费', '利息管理费如何收取？', '<p class=\"MsoNormal\">\n	平台会根据您回款时的会员等级收取收益部分的一定比例作为管理费。具体请查看帮助中心-会员积分-Q3活跃积分和帐号的等级关系\n</p>', '20180305133208', '20180305133208', '0', '0', '王舜', '2a61b57cc96543918f81f3bb7b144c56', 'N', null);
