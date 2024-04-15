ALTER TABLE `md_task`
    ADD COLUMN `produce_mode` int NULL DEFAULT 1 COMMENT '提供数据模式，默认1，1-API、2-接收推送',
    ADD COLUMN `auth_type` int NULL DEFAULT 0 COMMENT '提供数据的 认证方式',
    ADD COLUMN `auth_params` text NULL COMMENT '提供数据的 认证参数',
    MODIFY COLUMN `consume_mode` int(0) NULL COMMENT '消费数据模式，默认1，1-API、2-发邮件',
    MODIFY COLUMN `produce_mode` int(0) NULL COMMENT '提供数据模式，默认1，1-API、2-接收推送',
    ADD COLUMN `single_mode` int NULL COMMENT '单条记录消费模式，1-对象、2-集合'
    ADD COLUMN `subscribe_task_id` bigint NULL COMMENT '订阅任务id';

update `md_task` set subscribe_task_id = 0 where is_subscribed = 1 and subscribe_task_id is null;