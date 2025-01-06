SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for dev_entity
-- ----------------------------
DROP TABLE IF EXISTS `dev_entity`;
CREATE TABLE `dev_entity`  (
  `id` bigint(0) NOT NULL COMMENT '主键，自增',
  `create_user` bigint(0) NULL DEFAULT NULL COMMENT '创建人id',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_user` bigint(0) NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `status` int(0) NULL DEFAULT NULL COMMENT '业务状态',
  `is_deleted` int(0) NULL DEFAULT 0 COMMENT '删除状态：0-未删除，1-已删除',
  `create_department` bigint(0) NULL DEFAULT NULL COMMENT '创建部门id',
  `update_department` bigint(0) NULL DEFAULT NULL COMMENT '更新部门id',
  `tenant_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '租户id',
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '实体编号',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '实体名称',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '接口备注',
  `package_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '后端包名',
  `extend_mode` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '继承父类的模式：id、base、tree、tenant、tree_tenant',
  `table_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '数据库表名',
  `auth_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '最后一次生成的作者名',
  `java_code_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '最后一次生成java代码的路径',
  `ui_code_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '最后一次生成ui代码的路径',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '业务实体' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of dev_entity
-- ----------------------------

-- ----------------------------
-- Table structure for dev_entity_property
-- ----------------------------
DROP TABLE IF EXISTS `dev_entity_property`;
CREATE TABLE `dev_entity_property`  (
  `id` bigint(0) NOT NULL COMMENT '主键，自增',
  `entity_id` bigint(0) NULL DEFAULT NULL COMMENT '所属实体id',
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '字段编号',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '字段名称',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '字段类型',
  `is_list` int(0) NULL DEFAULT 0 COMMENT '是否显示在列表：0-不显示，1-显示',
  `is_search` int(0) NULL DEFAULT 0 COMMENT '是否作为查询条件：0-不是，1-是',
  `search_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '查询类型：equals、like…',
  `is_form` int(0) NULL DEFAULT 0 COMMENT '是否显示在表单：0-不显示，1-显示',
  `form_component` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '表单组件：input、number、date…',
  `is_required` int(0) NULL DEFAULT 0 COMMENT '是否必填：0-不必填，1-必填',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '业务实体属性' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of dev_entity_property
-- ----------------------------

-- ----------------------------
-- Table structure for md_api
-- ----------------------------
DROP TABLE IF EXISTS `md_api`;
CREATE TABLE `md_api`  (
  `id` bigint(0) NOT NULL COMMENT '主键，自增',
  `create_user` bigint(0) NULL DEFAULT NULL COMMENT '创建人id',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_user` bigint(0) NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `create_department` bigint(0) NULL DEFAULT NULL COMMENT '创建部门id',
  `update_department` bigint(0) NULL DEFAULT NULL COMMENT '更新部门id',
  `status` int(0) NULL DEFAULT NULL COMMENT '业务状态',
  `is_deleted` int(0) NULL DEFAULT 0 COMMENT '删除状态：0-未删除，1-已删除',
  `tenant_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '租户id',
  `app_id` bigint(0) NULL DEFAULT NULL COMMENT '所属应用',
  `api_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '接口名称',
  `op_type` int(0) NULL DEFAULT NULL COMMENT '操作类型',
  `api_method` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '请求方法',
  `api_uri` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '接口路径',
  `data_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '数据类型',
  `field_prefix` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '数据层级',
  `req_headers` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '请求Header',
  `req_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '请求参数',
  `req_body_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '请求体类型',
  `req_body_form` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '请求体，form格式',
  `req_body_raw` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '请求体，raw格式',
  `resp_example` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '响应示例',
  `data_mode` int(0) NULL DEFAULT NULL COMMENT '数据结构模式，1-对象、2-集合',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '应用接口' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of md_api
-- ----------------------------

-- ----------------------------
-- Table structure for md_app
-- ----------------------------
DROP TABLE IF EXISTS `md_app`;
CREATE TABLE `md_app`  (
  `id` bigint(0) NOT NULL COMMENT '主键，自增',
  `create_user` bigint(0) NULL DEFAULT NULL COMMENT '创建人id',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_user` bigint(0) NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `create_department` bigint(0) NULL DEFAULT NULL COMMENT '创建部门id',
  `update_department` bigint(0) NULL DEFAULT NULL COMMENT '更新部门id',
  `status` int(0) NULL DEFAULT NULL COMMENT '业务状态',
  `is_deleted` int(0) NULL DEFAULT 0 COMMENT '删除状态：0-未删除，1-已删除',
  `tenant_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '租户id',
  `app_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '应用编号',
  `app_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '应用名称',
  `app_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '访问地址',
  `api_prefix` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '接口前缀地址',
  `app_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '应用描述',
  `api_count` int(0) NULL DEFAULT NULL COMMENT '接口数量',
  `req_headers` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '请求Header',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '应用' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of md_app
-- ----------------------------

-- ----------------------------
-- Table structure for md_data
-- ----------------------------
DROP TABLE IF EXISTS `md_data`;
CREATE TABLE `md_data`  (
  `id` bigint(0) NOT NULL COMMENT '主键，自增',
  `create_user` bigint(0) NULL DEFAULT NULL COMMENT '创建人id',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_user` bigint(0) NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `create_department` bigint(0) NULL DEFAULT NULL COMMENT '创建部门id',
  `update_department` bigint(0) NULL DEFAULT NULL COMMENT '更新部门id',
  `status` int(0) NULL DEFAULT NULL COMMENT '业务状态',
  `is_deleted` int(0) NULL DEFAULT 0 COMMENT '删除状态：0-未删除，1-已删除',
  `tenant_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '租户id',
  `project_id` bigint(0) NULL DEFAULT NULL COMMENT '所属项目',
  `data_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '数据编号',
  `data_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '数据名称',
  `data_count` bigint(0) NULL DEFAULT 0 COMMENT '业务数据量',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '标准数据' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of md_data
-- ----------------------------

-- ----------------------------
-- Table structure for md_data_field
-- ----------------------------
DROP TABLE IF EXISTS `md_data_field`;
CREATE TABLE `md_data_field`  (
  `id` bigint(0) NOT NULL COMMENT '主键，自增',
  `data_id` bigint(0) NULL DEFAULT NULL COMMENT '所属数据',
  `field_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '字段编号',
  `field_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '字段名称',
  `field_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '字段类型',
  `default_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '字段默认值',
  `is_id` int(0) NULL DEFAULT NULL COMMENT '是否标识',
  `display_mode` int(0) NULL DEFAULT NULL COMMENT '显示模式',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '标准数据字段' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of md_data_field
-- ----------------------------

-- ----------------------------
-- Table structure for md_pipeline
-- ----------------------------
DROP TABLE IF EXISTS `md_pipeline`;
CREATE TABLE `md_pipeline`  (
  `id` bigint(0) NOT NULL COMMENT '主键，自增',
  `create_user` bigint(0) NULL DEFAULT NULL COMMENT '创建人id',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_user` bigint(0) NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `create_department` bigint(0) NULL DEFAULT NULL COMMENT '创建部门id',
  `update_department` bigint(0) NULL DEFAULT NULL COMMENT '更新部门id',
  `status` int(0) NULL DEFAULT NULL COMMENT '业务状态',
  `is_deleted` int(0) NULL DEFAULT 0 COMMENT '删除状态：0-未删除，1-已删除',
  `tenant_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '租户id',
  `project_id` bigint(0) NULL DEFAULT NULL COMMENT '所属项目',
  `group_id` bigint(0) NULL DEFAULT NULL COMMENT '所属分组',
  `pipeline_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '流水线名称',
  `pipeline_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '流水线描述',
  `is_schedule` int(0) NULL DEFAULT 0 COMMENT '是否启用定时',
  `day_of_week` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '执行日',
  `start_time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '结束时间',
  `interval_time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '时间间隔，HH:mm:ss',
  `time_zone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '时区',
  `is_webhook` int(0) NULL DEFAULT 0 COMMENT '是否启用webhook',
  `webhook_auth_type` int(0) NULL DEFAULT 0 COMMENT 'webhook认证方式',
  `webhook_auth_params` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'webhook认证参数',
  `webhook_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'webhook标识编号',
  `is_email` int(0) NULL DEFAULT 0 COMMENT '是否启用邮件',
  `email_strategy` int(0) NULL DEFAULT NULL COMMENT '邮件通知策略',
  `email_receiver` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '接收人',
  `latest_history_id` bigint(0) NULL DEFAULT NULL COMMENT '最新的执行记录id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '流水线' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of md_pipeline
-- ----------------------------

-- ----------------------------
-- Table structure for md_pipeline_group
-- ----------------------------
DROP TABLE IF EXISTS `md_pipeline_group`;
CREATE TABLE `md_pipeline_group`  (
  `id` bigint(0) NOT NULL COMMENT '主键，自增',
  `create_user` bigint(0) NULL DEFAULT NULL COMMENT '创建人id',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_user` bigint(0) NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `create_department` bigint(0) NULL DEFAULT NULL COMMENT '创建部门id',
  `update_department` bigint(0) NULL DEFAULT NULL COMMENT '更新部门id',
  `status` int(0) NULL DEFAULT NULL COMMENT '业务状态',
  `is_deleted` int(0) NULL DEFAULT 0 COMMENT '删除状态：0-未删除，1-已删除',
  `tenant_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '租户id',
  `project_id` bigint(0) NULL DEFAULT NULL COMMENT '所属项目',
  `group_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '分组名称',
  `group_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '分组描述',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '流水线分组' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of md_pipeline_group
-- ----------------------------

-- ----------------------------
-- Table structure for md_pipeline_history
-- ----------------------------
DROP TABLE IF EXISTS `md_pipeline_history`;
CREATE TABLE `md_pipeline_history`  (
  `id` bigint(0) NOT NULL COMMENT '主键，自增',
  `create_user` bigint(0) NULL DEFAULT NULL COMMENT '创建人id',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_user` bigint(0) NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `create_department` bigint(0) NULL DEFAULT NULL COMMENT '创建部门id',
  `update_department` bigint(0) NULL DEFAULT NULL COMMENT '更新部门id',
  `status` int(0) NULL DEFAULT NULL COMMENT '业务状态',
  `is_deleted` int(0) NULL DEFAULT 0 COMMENT '删除状态：0-未删除，1-已删除',
  `tenant_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '租户id',
  `pipeline_id` bigint(0) NULL DEFAULT NULL COMMENT '所属流水线',
  `start_time` datetime(0) NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT '结束时间',
  `execution_time` bigint(0) NULL DEFAULT NULL COMMENT '耗时秒数',
  `trigger_type` int(0) NULL DEFAULT NULL COMMENT '触发方式',
  `pipeline_vars` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '流水线参数',
  `execution_status` int(0) NULL DEFAULT NULL COMMENT '执行状态，1-运行中、2-中止、3-成功、4-失败',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '流水线执行记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of md_pipeline_history
-- ----------------------------

-- ----------------------------
-- Table structure for md_pipeline_log
-- ----------------------------
DROP TABLE IF EXISTS `md_pipeline_log`;
CREATE TABLE `md_pipeline_log`  (
  `id` bigint(0) NOT NULL COMMENT '主键，自增',
  `pipeline_id` bigint(0) NULL DEFAULT NULL COMMENT '所属流水线',
  `history_id` bigint(0) NULL DEFAULT NULL COMMENT '所属执行记录',
  `task_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '任务类型',
  `task_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '任务名称',
  `task_log` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '日志内容',
  `start_time` datetime(0) NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT '结束时间',
  `execution_time` bigint(0) NULL DEFAULT NULL COMMENT '耗时',
  `execution_status` int(0) NULL DEFAULT NULL COMMENT '执行状态',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '流水线执行日志' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of md_pipeline_log
-- ----------------------------

-- ----------------------------
-- Table structure for md_pipeline_task
-- ----------------------------
DROP TABLE IF EXISTS `md_pipeline_task`;
CREATE TABLE `md_pipeline_task`  (
  `id` bigint(0) NOT NULL COMMENT '主键，自增',
  `create_user` bigint(0) NULL DEFAULT NULL COMMENT '创建人id',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_user` bigint(0) NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `create_department` bigint(0) NULL DEFAULT NULL COMMENT '创建部门id',
  `update_department` bigint(0) NULL DEFAULT NULL COMMENT '更新部门id',
  `status` int(0) NULL DEFAULT NULL COMMENT '业务状态',
  `is_deleted` int(0) NULL DEFAULT 0 COMMENT '删除状态：0-未删除，1-已删除',
  `tenant_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '租户id',
  `project_id` bigint(0) NULL DEFAULT NULL COMMENT '所属项目',
  `pipeline_id` bigint(0) NULL DEFAULT NULL COMMENT '所属流水线',
  `task_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '任务类型',
  `task_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '任务名称',
  `app_id` bigint(0) NULL DEFAULT NULL COMMENT '关联应用',
  `api_id` bigint(0) NULL DEFAULT NULL COMMENT '关联API',
  `data_id` bigint(0) NULL DEFAULT NULL COMMENT '关联数据',
  `task_config` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '任务配置',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '流水线任务' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of md_pipeline_task
-- ----------------------------

-- ----------------------------
-- Table structure for md_project
-- ----------------------------
DROP TABLE IF EXISTS `md_project`;
CREATE TABLE `md_project`  (
  `id` bigint(0) NOT NULL COMMENT '主键，自增',
  `create_user` bigint(0) NULL DEFAULT NULL COMMENT '创建人id',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_user` bigint(0) NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `create_department` bigint(0) NULL DEFAULT NULL COMMENT '创建部门id',
  `update_department` bigint(0) NULL DEFAULT NULL COMMENT '更新部门id',
  `status` int(0) NULL DEFAULT NULL COMMENT '业务状态',
  `is_deleted` int(0) NULL DEFAULT 0 COMMENT '删除状态：0-未删除，1-已删除',
  `tenant_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '租户id',
  `project_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '项目编号',
  `project_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '项目名称',
  `project_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '项目描述',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '项目' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of md_project
-- ----------------------------

-- ----------------------------
-- Table structure for md_user_config
-- ----------------------------
DROP TABLE IF EXISTS `md_user_config`;
CREATE TABLE `md_user_config`  (
  `id` bigint(0) NOT NULL COMMENT '主键，自增',
  `user_id` bigint(0) NULL DEFAULT NULL COMMENT '用户id',
  `latest_project_id` bigint(0) NULL DEFAULT NULL COMMENT '最新管理的项目id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户的集成配置' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of md_user_config
-- ----------------------------

-- ----------------------------
-- Table structure for sys_api
-- ----------------------------
DROP TABLE IF EXISTS `sys_api`;
CREATE TABLE `sys_api`  (
  `id` bigint(0) NOT NULL COMMENT '主键，自增',
  `create_user` bigint(0) NULL DEFAULT NULL COMMENT '创建人id',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_user` bigint(0) NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `status` int(0) NULL DEFAULT NULL COMMENT '业务状态',
  `is_deleted` int(0) NULL DEFAULT 0 COMMENT '删除状态：0-未删除，1-已删除',
  `create_department` bigint(0) NULL DEFAULT NULL COMMENT '创建部门id',
  `update_department` bigint(0) NULL DEFAULT NULL COMMENT '更新部门id',
  `parent_id` bigint(0) NULL DEFAULT NULL COMMENT '父记录id',
  `id_tree_path` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'id层级路径',
  `menu_id` bigint(0) NULL DEFAULT NULL COMMENT '所属菜单id',
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '接口编号',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '接口名称',
  `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '接口地址',
  `method` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '请求方式：GET、POST、DELETE',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '接口备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统接口' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_api
-- ----------------------------

-- ----------------------------
-- Table structure for sys_department
-- ----------------------------
DROP TABLE IF EXISTS `sys_department`;
CREATE TABLE `sys_department`  (
  `id` bigint(0) NOT NULL COMMENT '主键，自增',
  `create_user` bigint(0) NULL DEFAULT NULL COMMENT '创建人id',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_user` bigint(0) NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `status` int(0) NULL DEFAULT NULL COMMENT '业务状态',
  `is_deleted` int(0) NULL DEFAULT 0 COMMENT '删除状态：0-未删除，1-已删除',
  `create_department` bigint(0) NULL DEFAULT NULL COMMENT '创建部门id',
  `update_department` bigint(0) NULL DEFAULT NULL COMMENT '更新部门id',
  `tenant_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '租户id',
  `parent_id` bigint(0) NULL DEFAULT NULL COMMENT '父记录id',
  `id_tree_path` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'id层级路径',
  `is_leaf` int(0) NULL DEFAULT 1 COMMENT '是否叶子节点',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '机构名称',
  `type` int(0) NULL DEFAULT NULL COMMENT '机构类型，1-公司，2-部门，3-小组',
  `sort` int(0) NULL DEFAULT NULL COMMENT '机构排序',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '机构备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '机构部门' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_department
-- ----------------------------
INSERT INTO `sys_department` VALUES (1828330175358709761, 1827994143199465474, '2025-01-01 00:00:00', 1827994143199465474, '2025-01-01 00:00:00', 1, 0, NULL, NULL, '0000000000', NULL, NULL, 0, '总公司', 1, 1, NULL);

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `id` bigint(0) NOT NULL COMMENT '主键，自增',
  `create_user` bigint(0) NULL DEFAULT NULL COMMENT '创建人id',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_user` bigint(0) NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `status` int(0) NULL DEFAULT NULL COMMENT '业务状态',
  `is_deleted` int(0) NULL DEFAULT 0 COMMENT '删除状态：0-未删除，1-已删除',
  `create_department` bigint(0) NULL DEFAULT NULL COMMENT '创建部门id',
  `update_department` bigint(0) NULL DEFAULT NULL COMMENT '更新部门id',
  `parent_id` bigint(0) NULL DEFAULT NULL COMMENT '父记录id',
  `id_tree_path` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'id层级路径',
  `is_leaf` int(0) NULL DEFAULT 1 COMMENT '是否叶子节点',
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '菜单编号',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '菜单名称',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '菜单图标',
  `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '菜单路径',
  `sort` int(0) NULL DEFAULT NULL COMMENT '菜单排序',
  `type` int(0) NULL DEFAULT NULL COMMENT '菜单类型，1-菜单、2-按钮',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '菜单备注',
  `api_count` int(0) NULL DEFAULT 0 COMMENT '接口数量',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '菜单' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES (1828640150114119681, 1827994143199465474, '2025-01-01 00:00:00', 1827994143199465474, '2025-01-01 00:00:00', 1, 0, NULL, NULL, NULL, NULL, 0, 'system', '系统管理', 'Setting', '/system', 3, 1, '', 0);
INSERT INTO `sys_menu` VALUES (1828707807387561985, 1827994143199465474, '2025-01-01 00:00:00', 1827994143199465474, '2025-01-01 00:00:00', 1, 0, NULL, NULL, 1828640150114119681, '1828640150114119681', 1, 'menu', '菜单管理', NULL, '/system/menu', 2, 1, NULL, 0);
INSERT INTO `sys_menu` VALUES (1828986836215676930, 1827994143199465474, '2025-01-01 00:00:00', 1827994143199465474, '2025-01-01 00:00:00', 1, 0, NULL, NULL, NULL, NULL, 0, 'organization', '组织管理', 'Cluster', '/organization', 2, 1, NULL, 0);
INSERT INTO `sys_menu` VALUES (1828986984698232834, 1827994143199465474, '2025-01-01 00:00:00', 1827994143199465474, '2025-01-01 00:00:00', 1, 0, NULL, NULL, 1828986836215676930, '1828986836215676930', 1, 'department', '组织结构', NULL, '/organization/department', 1, 1, NULL, 0);
INSERT INTO `sys_menu` VALUES (1829034428966461442, 1827994143199465474, '2025-01-01 00:00:00', 1827994143199465474, '2025-01-01 00:00:00', 1, 0, NULL, NULL, 1828986836215676930, '1828986836215676930', 1, 'role', '角色管理', NULL, '/organization/role', 2, 1, NULL, 0);
INSERT INTO `sys_menu` VALUES (1829034537884147713, 1827994143199465474, '2025-01-01 00:00:00', 1827994143199465474, '2025-01-01 00:00:00', 1, 0, NULL, NULL, 1828986836215676930, '1828986836215676930', 1, 'user', '用户管理', NULL, '/organization/user', 3, 1, NULL, 0);
INSERT INTO `sys_menu` VALUES (1830105458632835074, 1827994143199465474, '2025-01-01 00:00:00', 1827994143199465474, '2025-01-01 00:00:00', 1, 0, NULL, NULL, 1828986836215676930, '1828986836215676930', 1, 'account', '个人设置', NULL, '/organization/account', 4, 1, NULL, 0);
INSERT INTO `sys_menu` VALUES (1830177810913792002, 1827994143199465474, '2025-01-01 00:00:00', 1827994143199465474, '2025-01-01 00:00:00', 1, 0, NULL, NULL, 1828640150114119681, '1828640150114119681', 1, 'parameter', '参数管理', NULL, '/system/parameter', 4, 1, NULL, 0);
INSERT INTO `sys_menu` VALUES (1835668263881719809, 1827994143199465474, '2025-01-01 00:00:00', 1827994143199465474, '2025-01-01 00:00:00', 1, 0, NULL, NULL, 1828640150114119681, '1828640150114119681', 1, 'sys_api', '接口管理', NULL, '/system/api', 3, 1, NULL, 0);
INSERT INTO `sys_menu` VALUES (1839864384060493825, 1827994143199465474, '2025-01-01 00:00:00', 1827994143199465474, '2025-01-01 00:00:00', 1, 0, NULL, NULL, NULL, NULL, 0, 'dev', '开发工具', 'Tool', '/dev', 4, 1, NULL, 0);
INSERT INTO `sys_menu` VALUES (1839864664042868738, 1827994143199465474, '2025-01-01 00:00:00', 1827994143199465474, '2025-01-01 00:00:00', 1, 0, NULL, NULL, 1839864384060493825, '1839864384060493825', 1, 'dev_entity', '生成代码', NULL, '/dev/dev_entity', 1, 1, NULL, 0);
INSERT INTO `sys_menu` VALUES (1842041885029392386, 1827994143199465474, '2025-01-01 00:00:00', 1827994143199465474, '2025-01-01 00:00:00', 1, 0, NULL, NULL, NULL, NULL, 0, 'demo', '示例模块', 'Smile', '/demo', 5, 1, NULL, 0);
INSERT INTO `sys_menu` VALUES (1842042120749277186, 1827994143199465474, '2025-01-01 00:00:00', 1827994143199465474, '2025-01-01 00:00:00', 1, 0, NULL, NULL, 1842041885029392386, '1842041885029392386', 1, 'demo', '代码生成示例', NULL, '/demo/demo', 1, 1, NULL, 0);
INSERT INTO `sys_menu` VALUES (1852714017730387970, 1827994143199465474, '2025-01-01 00:00:00', 1827994143199465474, '2025-01-01 00:00:00', 1, 0, NULL, NULL, 1828640150114119681, '1828640150114119681', 1, 'tenant', '租户管理', NULL, '/system/tenant', 1, 1, NULL, 0);
INSERT INTO `sys_menu` VALUES (1855115766317285378, 1827994143199465474, '2025-01-01 00:00:00', 1827994143199465474, '2025-01-01 00:00:00', 1, 0, NULL, NULL, NULL, NULL, 0, 'mydata', '数据融合', 'Swap', '/mydata', 1, 1, NULL, 0);
INSERT INTO `sys_menu` VALUES (1855116043795660801, 1827994143199465474, '2025-01-01 00:00:00', 1827994143199465474, '2025-01-01 00:00:00', 1, 0, NULL, NULL, 1855115766317285378, '1855115766317285378', 1, 'project', '项目管理', NULL, '/mydata/project', 2, 1, NULL, 0);
INSERT INTO `sys_menu` VALUES (1855125216440623106, 1827994143199465474, '2025-01-01 00:00:00', 1827994143199465474, '2025-01-01 00:00:00', 1, 0, NULL, NULL, 1855115766317285378, '1855115766317285378', 1, 'data', '数据管理', NULL, '/mydata/data', 3, 1, NULL, 0);
INSERT INTO `sys_menu` VALUES (1855809972139515906, 1827994143199465474, '2025-01-01 00:00:00', 1827994143199465474, '2025-01-01 00:00:00', 1, 0, NULL, NULL, 1855115766317285378, '1855115766317285378', 1, 'app', '应用管理', NULL, '/mydata/app', 4, 1, NULL, 0);
INSERT INTO `sys_menu` VALUES (1855869732599746562, 1827994143199465474, '2025-01-01 00:00:00', 1827994143199465474, '2025-01-01 00:00:00', 1, 0, NULL, NULL, 1855115766317285378, '1855115766317285378', 1, 'api', 'API管理', NULL, '/mydata/api', 5, 1, NULL, 0);
INSERT INTO `sys_menu` VALUES (1856560152005672962, 1827994143199465474, '2025-01-01 00:00:00', 1827994143199465474, '2025-01-01 00:00:00', 1, 0, NULL, NULL, 1855115766317285378, '1855115766317285378', 1, 'warehouse', '集成管理', NULL, '/mydata/warehouse', 1, 1, NULL, 0);

-- ----------------------------
-- Table structure for sys_parameter
-- ----------------------------
DROP TABLE IF EXISTS `sys_parameter`;
CREATE TABLE `sys_parameter`  (
  `id` bigint(0) NOT NULL COMMENT '主键，自增',
  `create_user` bigint(0) NULL DEFAULT NULL COMMENT '创建人id',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_user` bigint(0) NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `status` int(0) NULL DEFAULT NULL COMMENT '业务状态',
  `is_deleted` int(0) NULL DEFAULT 0 COMMENT '删除状态：0-未删除，1-已删除',
  `create_department` bigint(0) NULL DEFAULT NULL COMMENT '创建部门id',
  `update_department` bigint(0) NULL DEFAULT NULL COMMENT '更新部门id',
  `parent_id` bigint(0) NULL DEFAULT NULL COMMENT '父记录id',
  `id_tree_path` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'id层级路径',
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '参数编号',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '参数名称',
  `value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '参数值',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '参数备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统参数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_parameter
-- ----------------------------

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint(0) NOT NULL COMMENT '主键，自增',
  `create_user` bigint(0) NULL DEFAULT NULL COMMENT '创建人id',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_user` bigint(0) NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `status` int(0) NULL DEFAULT NULL COMMENT '业务状态',
  `is_deleted` int(0) NULL DEFAULT 0 COMMENT '删除状态：0-未删除，1-已删除',
  `create_department` bigint(0) NULL DEFAULT NULL COMMENT '创建部门id',
  `update_department` bigint(0) NULL DEFAULT NULL COMMENT '更新部门id',
  `tenant_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '租户id',
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '角色编号',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '角色名称',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '角色备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1828617675955326978, 1827994143199465474, '2025-01-01 00:00:00', 1827994143199465474, '2025-01-01 00:00:00', 1, 0, NULL, NULL, '0000000000', 'admin', '管理员', NULL);

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `id` bigint(0) NOT NULL COMMENT '主键，自增',
  `role_id` bigint(0) NULL DEFAULT NULL COMMENT '角色id',
  `menu_id` bigint(0) NULL DEFAULT NULL COMMENT '菜单id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色菜单' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------
INSERT INTO `sys_role_menu` VALUES (1856560334143324161, 1828617675955326978, 1828707807387561985);
INSERT INTO `sys_role_menu` VALUES (1856560334164295681, 1828617675955326978, 1828986984698232834);
INSERT INTO `sys_role_menu` VALUES (1856560334164295682, 1828617675955326978, 1829034428966461442);
INSERT INTO `sys_role_menu` VALUES (1856560334164295683, 1828617675955326978, 1829034537884147713);
INSERT INTO `sys_role_menu` VALUES (1856560334164295684, 1828617675955326978, 1830105458632835074);
INSERT INTO `sys_role_menu` VALUES (1856560334172684289, 1828617675955326978, 1830177810913792002);
INSERT INTO `sys_role_menu` VALUES (1856560334172684290, 1828617675955326978, 1835668263881719809);
INSERT INTO `sys_role_menu` VALUES (1856560334172684291, 1828617675955326978, 1839864664042868738);
INSERT INTO `sys_role_menu` VALUES (1856560334181072898, 1828617675955326978, 1852714017730387970);
INSERT INTO `sys_role_menu` VALUES (1856560334181072899, 1828617675955326978, 1855116043795660801);
INSERT INTO `sys_role_menu` VALUES (1856560334181072900, 1828617675955326978, 1855125216440623106);
INSERT INTO `sys_role_menu` VALUES (1856560334181072901, 1828617675955326978, 1855809972139515906);
INSERT INTO `sys_role_menu` VALUES (1856560334181072902, 1828617675955326978, 1855869732599746562);
INSERT INTO `sys_role_menu` VALUES (1856560334181072903, 1828617675955326978, 1828986836215676930);
INSERT INTO `sys_role_menu` VALUES (1856560334181072904, 1828617675955326978, 1828640150114119681);
INSERT INTO `sys_role_menu` VALUES (1856560334181072905, 1828617675955326978, 1839864384060493825);
INSERT INTO `sys_role_menu` VALUES (1856560334181072906, 1828617675955326978, 1856560152005672962);
INSERT INTO `sys_role_menu` VALUES (1856560334181072907, 1828617675955326978, 1855115766317285378);

-- ----------------------------
-- Table structure for sys_tenant
-- ----------------------------
DROP TABLE IF EXISTS `sys_tenant`;
CREATE TABLE `sys_tenant`  (
  `id` bigint(0) NOT NULL COMMENT '主键，自增',
  `create_user` bigint(0) NULL DEFAULT NULL COMMENT '创建人id',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_user` bigint(0) NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `create_department` bigint(0) NULL DEFAULT NULL COMMENT '创建部门id',
  `update_department` bigint(0) NULL DEFAULT NULL COMMENT '更新部门id',
  `status` int(0) NULL DEFAULT NULL COMMENT '业务状态',
  `is_deleted` int(0) NULL DEFAULT 0 COMMENT '删除状态：0-未删除，1-已删除',
  `tenant_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '租户id',
  `tenant_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '租户编号',
  `tenant_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '租户名称',
  `expire_time` datetime(0) NULL DEFAULT NULL COMMENT '到期时间',
  `contacts_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系人姓名',
  `contacts_phone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系人电话',
  `contacts_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系人地址',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统租户' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_tenant
-- ----------------------------
INSERT INTO `sys_tenant` VALUES (1852727984544612354, 1827994143199465474, '2025-01-01 00:00:00', 1827994143199465474, '2025-01-01 00:00:00', NULL, NULL, 1, 0, '0000000000', 'host', '主租户', '2099-12-31 00:00:00', NULL, NULL, NULL);

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint(0) NOT NULL COMMENT '主键，自增',
  `create_user` bigint(0) NULL DEFAULT NULL COMMENT '创建人id',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_user` bigint(0) NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `status` int(0) NULL DEFAULT NULL COMMENT '业务状态',
  `is_deleted` int(0) NULL DEFAULT 0 COMMENT '删除状态：0-未删除，1-已删除',
  `create_department` bigint(0) NULL DEFAULT NULL COMMENT '创建部门id',
  `update_department` bigint(0) NULL DEFAULT NULL COMMENT '更新部门id',
  `tenant_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '租户id',
  `code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户编号',
  `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户姓名',
  `phone` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '手机',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '邮箱',
  `department_id` bigint(0) NULL DEFAULT NULL COMMENT '所属部门id',
  `role_id` bigint(0) NULL DEFAULT NULL COMMENT '所属角色id',
  `login_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '登录账号',
  `login_password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '登录密码',
  `salt` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '密码加盐',
  `avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '头像',
  `password_strength` int(0) NULL DEFAULT 1 COMMENT '密码复杂度：1~5',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1827994143199465474, 1827994143199465474, '2025-01-01 00:00:00', 1827994143199465474, '2025-01-06 16:28:37', 1, 0, NULL, 1828330175358709761, '0000000000', 'admin', '管理员', '', 'mydata_work@163.com', 1828330175358709761, 1828617675955326978, 'admin', 'c064fffeac941c218ca503932b00e2b60740aa64dc2978e6a3dd2906c6ef15f2', 'WLJOy7eqJu97ClmPpzc0Rg82MqkabN0w', NULL, 1);

SET FOREIGN_KEY_CHECKS = 1;
