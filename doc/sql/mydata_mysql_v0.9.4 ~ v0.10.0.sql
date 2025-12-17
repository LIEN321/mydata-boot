ALTER TABLE `md_pipeline`
    ADD COLUMN `retry` int NULL DEFAULT 0 COMMENT '失败重试次数，默认0：不重试';