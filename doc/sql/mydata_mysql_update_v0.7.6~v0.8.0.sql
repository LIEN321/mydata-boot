ALTER TABLE `md_task`
    ADD COLUMN `consume_mode` int NULL DEFAULT 1 COMMENT '消费数据模式，默认1，1-API、2-发邮件' ,
    ADD COLUMN `consume_email` varchar(64) NULL COMMENT '消费数据模式的收件人邮件',
    ADD COLUMN `skip_error` int NULL DEFAULT 0 COMMENT '跳过特殊情况，0-不跳过，1-跳过相同数据异常';