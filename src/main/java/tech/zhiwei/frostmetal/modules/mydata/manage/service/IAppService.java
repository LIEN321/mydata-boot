package tech.zhiwei.frostmetal.modules.mydata.manage.service;

import tech.zhiwei.frostmetal.core.base.service.IBaseService;
import tech.zhiwei.frostmetal.modules.mydata.manage.dto.AppDTO;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.App;

/**
 * 应用 Service接口
 *
 * @author LIEN
 * @since 2024/11/11
 */
public interface IAppService extends IBaseService<App> {
    /**
     * 保存应用
     * @param appDTO 应用
     * @return id
     */
    Long saveApp(AppDTO appDTO);
}
