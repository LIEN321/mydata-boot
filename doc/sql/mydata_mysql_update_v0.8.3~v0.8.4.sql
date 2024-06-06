ALTER TABLE `md_data`
    ADD COLUMN `enable_history` int(0) NULL DEFAULT 0 COMMENT '是否启用历史记录：0-不启用、1-启用';

ALTER TABLE `md_data_field`
    ADD COLUMN `display_mode` int(0) NULL DEFAULT 1 COMMENT '字段显示模式：0-不显示、1-显示';