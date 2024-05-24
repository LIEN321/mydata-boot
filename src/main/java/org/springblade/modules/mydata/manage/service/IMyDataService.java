package org.springblade.modules.mydata.manage.service;

/**
 * mydata 通用业务
 *
 * @author LIEN
 * @since 2024/5/13
 */
public interface IMyDataService {
    /**
     * 新用户初始mydata示例数据
     */
    void initData(String tenantId);
}
