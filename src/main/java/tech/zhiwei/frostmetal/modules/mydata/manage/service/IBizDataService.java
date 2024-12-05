package tech.zhiwei.frostmetal.modules.mydata.manage.service;

import tech.zhiwei.frostmetal.core.base.service.IBaseService;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.Data;

/**
 * 业务数据 服务类
 *
 * @author LIEN
 * @since 2024/12/05
 */
public interface IBizDataService extends IBaseService<Data> {
    /**
     * 更新指定标准数据的业务数据总量
     *
     * @param dataId 标准数据id
     */
    void updateDataCount(Long dataId);
}
