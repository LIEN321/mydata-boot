ALTER TABLE `md_task`
    ADD COLUMN `consume_mode` int NULL DEFAULT 1 COMMENT '消费数据模式，默认1，1-API、2-发邮件' ,
    ADD COLUMN `consume_email` varchar(64) NULL COMMENT '消费数据模式的收件人邮件';