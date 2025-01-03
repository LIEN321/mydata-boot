-- ${entityName}
DROP TABLE if exists `${tableName}`;
CREATE TABLE `${tableName}`(
  `id` BIGINT(20) NOT NULL comment '主键，自增',
<#if extendMode == "base" || extendMode == "tree" || extendMode == "tenant" || extendMode == "tree_tenant">
  `create_user` BIGINT(20) comment '创建人id',
  `create_time` DATETIME comment '创建时间',
  `update_user` BIGINT(20) comment '更新人',
  `update_time` DATETIME comment '更新时间',
  `create_department` BIGINT comment '创建部门id',
  `update_department` BIGINT comment '更新部门id',
  `status` INT comment '业务状态',
  `is_deleted` INT DEFAULT 0 comment '删除状态：0-未删除，1-已删除',
</#if>
<#if extendMode == "tree">
  `parent_id` BIGINT comment '父记录id',
  `id_tree_path` VARCHAR(2000) comment 'id层级路径',
  `is_leaf` INT DEFAULT 1 comment '是否叶子节点',
</#if>
<#if extendMode == "tenant" || extendMode == "tree_tenant">
  `tenant_id` varchar(20) comment '租户id',
</#if>
<#list properties as p>
  `${p.propertyCodeUnderlineCase}` ${p.tableFieldType} comment '${p.propertyName}',
</#list>
  PRIMARY Key(`id`)
) DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci comment '${entityName}';
