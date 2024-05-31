ALTER TABLE `md_data`
    ADD COLUMN `enable_history` int(0) NULL DEFAULT 0 COMMENT '是否启用历史记录：0-不启用、1-启用';