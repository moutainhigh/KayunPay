alter table t_loan_trace add overdueAmount bigint(20) DEFAULT 0 COMMENT '逾期金额';
ALTER table t_loan_trace add overdueInterest BIGINT(20) DEFAULT 0 COMMENT '逾期时利息';