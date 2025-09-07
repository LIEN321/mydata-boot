ALTER TABLE `md_pipeline`
    MODIFY COLUMN `email_strategy` varchar (255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '邮件通知策略';

ALTER TABLE `md_pipeline_history`
    MODIFY COLUMN `trigger_param` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '触发时的入参';

ALTER TABLE `md_pipeline`
    ADD COLUMN `consecutive_failures` int(0) NULL DEFAULT 0 COMMENT '连续失败次数';

ALTER TABLE `md_pipeline_task`
    ADD COLUMN `pre_condition` int(0) NULL DEFAULT 1 COMMENT '后续的前提条件：0-ALWAYS，1-SUCCESS';

update md_app a
    join ( select app.id, (select count(1) from md_api where app_id = app.id and is_deleted = 0) as api_count
    from md_app app ) t
on a.id = t.id
    set a.api_count = t.api_count
;

ALTER TABLE `md_app`
    ADD COLUMN `auth_type` varchar(64) NULL COMMENT DEFAULT '' COMMENT '认证类型' AFTER `req_headers`,
    ADD COLUMN `auth_config` text NULL COMMENT '认证配置' AFTER `auth_type`;