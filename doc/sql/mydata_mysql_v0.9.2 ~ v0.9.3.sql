ALTER TABLE `md_pipeline`
    MODIFY COLUMN `email_strategy` varchar (255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '邮件通知策略';

ALTER TABLE `md_pipeline_history`
    MODIFY COLUMN `trigger_param` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '触发时的入参';

ALTER TABLE `md_pipeline`
    ADD COLUMN `consecutive_failures` int(0) NULL DEFAULT 0 COMMENT '连续失败次数';