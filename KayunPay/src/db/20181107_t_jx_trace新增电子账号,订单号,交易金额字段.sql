alter table t_jx_trace add txAmount decimal(20,2) default '0' comment '交易金额' after batchNo;
alter table t_jx_trace add accountId text(0) comment '发起方电子账号(分隔符号:&&)' after txAmount;
alter table t_jx_trace add forAccountId text(0) comment '对手电子账号(分隔符号:&&)' after accountId;
alter table t_jx_trace add orderId text(0) comment '订单号(分隔符号:&&)' after forAccountId;