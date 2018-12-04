-- 添加加息额度字段
ALTER table t_funds add rewardRateAmount BIGINT(20);

-- 更新加息额度
UPDATE (
	t_funds t1
	LEFT JOIN (
		SELECT
			COALESCE (sum(traceAmount) * 2, 0) Amount,
			userCode
		FROM
			t_funds_trace
		WHERE
			traceDate BETWEEN 20171118
		AND 20171217
		AND traceType = 'P'
		GROUP BY
			userCode
	) t2 ON t1.userCode = t2.userCode
)
SET rewardRateAmount = COALESCE (Amount, 0);