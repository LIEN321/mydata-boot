-- TableName:流水线变量
-- Description:流水线变量
DROP TABLE if exists `mydata`.`md_pipeline_var`;
CREATE TABLE `mydata`.`md_pipeline_var`(
    `id` BIGINT(20) NOT NULL comment '主键，自增',
    `create_user` BIGINT(20) comment '创建人id',
    `create_time` DATETIME comment '创建时间',
    `update_user` BIGINT(20) comment '更新人',
    `update_time` DATETIME comment '更新时间',
    `status` INT comment '有效状态：0-无效，1-有效',
    `is_deleted` INT DEFAULT 0 comment '删除状态：0-未删除，1-已删除',
    `create_department` BIGINT comment '创建部门id',
    `update_department` BIGINT comment '更新部门id',
    `tenant_id` VARCHAR(20) comment '租户id',
    `pipeline_id` BIGINT comment '所属流水线',
    `var_code` VARCHAR(64) comment '变量编号',
    `var_value` VARCHAR(512) comment '变量值',
    `var_type` VARCHAR(64) comment '变量值类型',
    `var_desc` VARCHAR(64) comment '变量描述',
    `is_hide` INT DEFAULT 0 comment '是否隐藏变量值，0-不隐藏、1-隐藏',
    PRIMARY Key(`id`)
) DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci comment '流水线变量';