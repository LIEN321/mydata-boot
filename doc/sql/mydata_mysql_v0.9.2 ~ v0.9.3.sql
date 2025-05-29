ALTER TABLE `md_pipeline`
    MODIFY COLUMN `email_strategy` varchar (255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '邮件通知策略';