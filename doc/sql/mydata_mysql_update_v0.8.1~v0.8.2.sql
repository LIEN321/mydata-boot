DROP TABLE if exists `md_biz_data`;
CREATE TABLE `md_biz_data`
(
    `id`          bigint NOT NULL COMMENT '主键',
    `status`      tinyint unsigned DEFAULT NULL COMMENT '业务状态',
    `is_deleted`  tinyint unsigned DEFAULT '0' COMMENT '删除状态：0-未删除，1-已删除',
    `create_user` bigint                                 DEFAULT NULL COMMENT '创建人',
    `create_dept` bigint                                 DEFAULT NULL COMMENT '创建部门',
    `create_time` datetime                               DEFAULT NULL COMMENT '创建时间',
    `update_user` bigint                                 DEFAULT NULL COMMENT '更新人',
    `update_time` datetime                               DEFAULT NULL COMMENT '更新时间',
    `tenant_id`   varchar(12) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '所属租户',
    `project_id`  bigint                                 DEFAULT NULL COMMENT '项目id',
    `env_id`      bigint                                 DEFAULT NULL COMMENT '环境id',
    `data_id`     bigint                                 DEFAULT NULL COMMENT '数据项编号',
    `data_count`  bigint                                 DEFAULT NULL COMMENT '数据量',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='业务数据概况';

ALTER TABLE `md_task`
    CHANGE COLUMN `single_mode` `data_mode` int (0) NULL DEFAULT NULL COMMENT '单条记录消费模式，1-对象、2-集合',
    ADD COLUMN `data_process` text NULL COMMENT '数据处理' AFTER `next_run_time`,
    ADD COLUMN `req_body` text NULL COMMENT '请求体';

ALTER TABLE `md_api`
    ADD COLUMN `req_body` text NULL COMMENT '请求体';