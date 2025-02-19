ALTER TABLE `md_pipeline_history`
    ADD COLUMN `trigger_param` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '触发时的入参' AFTER `trigger_type`;