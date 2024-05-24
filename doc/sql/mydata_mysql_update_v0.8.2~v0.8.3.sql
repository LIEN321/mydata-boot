ALTER TABLE `md_project`
    MODIFY COLUMN `project_desc` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '项目描述';

ALTER TABLE `md_task`
    ADD COLUMN `same_batch` int(0) NULL DEFAULT 1 COMMENT '是否复用父任务批次数据，0-不复用、1-复用',
    MODIFY COLUMN `consume_mode` int(0) NULL DEFAULT 0 COMMENT '消费数据模式，默认1，1-API、2-发邮件',
    MODIFY COLUMN `produce_mode` int(0) NULL DEFAULT 0 COMMENT '提供数据模式，默认1，1-API、2-接收推送';

update md_task set produce_mode = 0 where produce_mode is null;
update md_task set consume_mode = 0 where consume_mode is null;