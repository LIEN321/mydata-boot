package tech.zhiwei.frostmetal.modules.mydata.manage.service;

import tech.zhiwei.frostmetal.core.base.service.IBaseService;
import tech.zhiwei.frostmetal.modules.mydata.manage.dto.AppApiDTO;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.AppApi;

/**
 * 应用接口 Service接口
 *
 * @author LIEN
 * @since 2024/11/11
 */
public interface IAppApiService extends IBaseService<AppApi> {
    /**
     * 保存应用接口
     * @param appApiDTO 应用接口
     * @return id
     */
    Long saveAppApi(AppApiDTO appApiDTO);
}
