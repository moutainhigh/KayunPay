alter table t_jx_trace add jxTraceStatus char(1) default '' comment 'jxTrace流水状态' after acqRes;
alter table t_jx_trace add createDate char(8) default '00000000' comment '添加日期' after jxTraceStatus;
alter table t_jx_trace add createTime char(6) default '000000' comment '添加时间' after createDate;
alter table t_jx_trace add updateDate char(8) default '00000000' comment '更新日期' after createTime;
alter table t_jx_trace add updateTime char(6) default '000000' comment '更新时间' after updateDate;