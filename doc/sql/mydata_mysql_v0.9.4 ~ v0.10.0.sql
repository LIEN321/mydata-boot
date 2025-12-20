ALTER TABLE `md_pipeline`
    ADD COLUMN `retry` int NULL DEFAULT 0 COMMENT '失败重试次数，默认0：不重试';

ALTER TABLE `md_pipeline_history`
    ADD COLUMN `execution_count` int NULL DEFAULT 0 COMMENT '执行次数（重试会增加）';
update `md_pipeline_history` set execution_count = 1;

ALTER TABLE `md_pipeline_log`
    ADD COLUMN `execution_count` int NULL DEFAULT 0 COMMENT '执行次数（重试会增加）';
update `md_pipeline_log` set execution_count = 1;

ALTER TABLE `md_pipeline_task`
    ADD COLUMN `retry` int NULL DEFAULT 0 COMMENT '失败重试次数，默认0：不重试';