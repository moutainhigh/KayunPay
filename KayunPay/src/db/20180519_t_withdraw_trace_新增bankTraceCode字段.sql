/*新增银行交易代码字段，初始值与withdrwaCode相同，接收到提现响应之后修改成银行交易流水，与t_jx_trace表中的jxTraceCode对应*/
ALTER TABLE t_withdraw_trace ADD COLUMN bankTraceCode CHAR(32) AFTER withdrawCode;